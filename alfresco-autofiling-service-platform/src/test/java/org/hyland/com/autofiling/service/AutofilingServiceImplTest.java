package org.hyland.com.autofiling.service;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.hyland.com.autofiling.model.AutofilingRule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AutofilingServiceImplTest {

    private static final String CM_NS   = "http://www.alfresco.org/model/content/1.0";
    private static final String ACME_NS = "http://www.acme.org/model/1.0";

    private static final NodeRef DOC_REF    = new NodeRef("workspace://SpacesStore/doc");
    private static final NodeRef INBOX_REF  = new NodeRef("workspace://SpacesStore/inbox");
    private static final NodeRef TARGET_REF = new NodeRef("workspace://SpacesStore/target");
    private static final NodeRef PARENT_REF = new NodeRef("workspace://SpacesStore/parent");

    @Mock private AutofilingRuleService ruleService;
    @Mock private PathResolver pathResolver;
    @Mock private FolderPathService folderPathService;
    @Mock private NodeService nodeService;
    @Mock private FileFolderService fileFolderService;
    @Mock private DictionaryService dictionaryService;
    @Mock private NamespaceService namespaceService;
    @Mock private TransactionService transactionService;
    @Mock private RetryingTransactionHelper retryingTransactionHelper;

    private AutofilingServiceImpl service;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AutofilingServiceImpl();
        service.setRuleService(ruleService);
        service.setPathResolver(pathResolver);
        service.setFolderPathService(folderPathService);
        service.setNodeService(nodeService);
        service.setFileFolderService(fileFolderService);
        service.setDictionaryService(dictionaryService);
        service.setNamespaceService(namespaceService);
        service.setTransactionService(transactionService);

        // Wire retrying transaction helper to execute callbacks inline
        when(transactionService.getRetryingTransactionHelper()).thenReturn(retryingTransactionHelper);
        when(retryingTransactionHelper.doInTransaction(any(), eq(false), eq(true)))
            .thenAnswer(inv -> {
                RetryingTransactionHelper.RetryingTransactionCallback<?> cb = inv.getArgument(0);
                return cb.execute();
            });

        // Document is cm:content
        when(nodeService.getType(DOC_REF)).thenReturn(ContentModel.TYPE_CONTENT);
        when(dictionaryService.isSubClass(ContentModel.TYPE_CONTENT, ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(dictionaryService.isSubClass(ContentModel.TYPE_CONTENT, ContentModel.TYPE_FOLDER)).thenReturn(false);

        // Document's current parent is PARENT_REF (not the target)
        ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
        when(parentAssoc.getParentRef()).thenReturn(PARENT_REF);
        when(nodeService.getPrimaryParent(DOC_REF)).thenReturn(parentAssoc);

        when(nodeService.exists(DOC_REF)).thenReturn(true);
        when(nodeService.getProperty(DOC_REF, ContentModel.PROP_NAME)).thenReturn("invoice.pdf");
    }

    // ───────────────────────────────────────────────────────────────────────
    // fileDocument
    // ───────────────────────────────────────────────────────────────────────

    @Test
    public void fileDocumentMovesToResolvedPath() throws Exception {
        AutofilingRule rule = ruleWithTemplate("/Company Home/Finance");
        when(pathResolver.resolve("/Company Home/Finance", DOC_REF)).thenReturn("/Company Home/Finance");
        when(folderPathService.getOrCreatePath("/Company Home/Finance", true)).thenReturn(TARGET_REF);

        service.fileDocument(DOC_REF, rule);

        verify(fileFolderService).move(DOC_REF, TARGET_REF, null);
    }

    @Test
    public void fileDocumentSkipsWhenAlreadyInTarget() throws Exception {
        AutofilingRule rule = ruleWithTemplate("/Company Home/Finance");
        when(pathResolver.resolve("/Company Home/Finance", DOC_REF)).thenReturn("/Company Home/Finance");
        when(folderPathService.getOrCreatePath("/Company Home/Finance", true)).thenReturn(PARENT_REF);

        service.fileDocument(DOC_REF, rule);

        verify(fileFolderService, never()).move(any(), any(), any());
    }

    @Test
    public void fileDocumentSkipsWhenResolvedPathIsEmpty() throws Exception {
        AutofilingRule rule = ruleWithTemplate("{cm:name}");
        when(pathResolver.resolve("{cm:name}", DOC_REF)).thenReturn("");

        service.fileDocument(DOC_REF, rule);

        verify(fileFolderService, never()).move(any(), any(), any());
        verify(folderPathService, never()).getOrCreatePath(any(), anyBoolean());
    }

    @Test
    public void fileDocumentSkipsWhenTargetFolderNotResolvable() throws Exception {
        AutofilingRule rule = ruleWithTemplate("/Company Home/Finance");
        when(pathResolver.resolve("/Company Home/Finance", DOC_REF)).thenReturn("/Company Home/Finance");
        when(folderPathService.getOrCreatePath("/Company Home/Finance", true)).thenReturn(null);

        service.fileDocument(DOC_REF, rule);

        verify(fileFolderService, never()).move(any(), any(), any());
    }

    @Test
    public void fileDocumentHandlesNameCollisionWithDedupedName() throws Exception {
        AutofilingRule rule = ruleWithTemplate("/Company Home/Finance");
        when(pathResolver.resolve("/Company Home/Finance", DOC_REF)).thenReturn("/Company Home/Finance");
        when(folderPathService.getOrCreatePath("/Company Home/Finance", true)).thenReturn(TARGET_REF);

        // First move throws FileExistsException, second (with deduped name) succeeds
        FileExistsException fex = mock(FileExistsException.class);
        doThrow(fex).when(fileFolderService).move(DOC_REF, TARGET_REF, null);

        service.fileDocument(DOC_REF, rule);

        // Should have retried with a deduped name
        verify(fileFolderService).move(eq(DOC_REF), eq(TARGET_REF), argThat(name ->
            name != null && name.startsWith("invoice_") && name.endsWith(".pdf")));
    }

    // ───────────────────────────────────────────────────────────────────────
    // contentTypesMatch (tested via processRule by inspecting move calls)
    // ───────────────────────────────────────────────────────────────────────

    @Test
    public void emptyContentTypesListMatchesAnyContent() throws Exception {
        AutofilingRule rule = ruleWithTemplate("/Company Home/Finance");
        rule.setContentTypes(Collections.emptyList()); // wildcard
        when(pathResolver.resolve(any(), eq(DOC_REF))).thenReturn("/Company Home/Finance");
        when(folderPathService.getOrCreatePath(any(), anyBoolean())).thenReturn(TARGET_REF);

        ChildAssociationRef childAssoc = mockChildAssoc(DOC_REF);
        when(folderPathService.getOrCreatePath(rule.getInboxPath(), false)).thenReturn(INBOX_REF);
        when(nodeService.getChildAssocs(eq(INBOX_REF), any(), any()))
            .thenReturn(Collections.singletonList(childAssoc));

        service.processRule(rule);

        verify(fileFolderService).move(eq(DOC_REF), eq(TARGET_REF), isNull());
    }

    @Test
    public void matchingContentTypeAllowsMove() throws Exception {
        QName acmeInvoice = QName.createQName(ACME_NS, "invoice");
        when(nodeService.getType(DOC_REF)).thenReturn(acmeInvoice);
        when(dictionaryService.isSubClass(acmeInvoice, ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(dictionaryService.isSubClass(acmeInvoice, acmeInvoice)).thenReturn(true);
        when(namespaceService.getNamespaceURI("acme")).thenReturn(ACME_NS);

        AutofilingRule rule = ruleWithTemplate("/Company Home/Finance");
        rule.setContentTypes(Collections.singletonList("acme:invoice"));

        ChildAssociationRef childAssoc = mockChildAssoc(DOC_REF);
        ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
        when(parentAssoc.getParentRef()).thenReturn(PARENT_REF);
        when(nodeService.getPrimaryParent(DOC_REF)).thenReturn(parentAssoc);
        when(pathResolver.resolve(any(), eq(DOC_REF))).thenReturn("/Company Home/Finance");
        when(folderPathService.getOrCreatePath(rule.getInboxPath(), false)).thenReturn(INBOX_REF);
        when(folderPathService.getOrCreatePath("/Company Home/Finance", true)).thenReturn(TARGET_REF);
        when(nodeService.getChildAssocs(eq(INBOX_REF), any(), any()))
            .thenReturn(Collections.singletonList(childAssoc));

        service.processRule(rule);

        verify(fileFolderService).move(eq(DOC_REF), eq(TARGET_REF), isNull());
    }

    @Test
    public void nonMatchingContentTypeSkipsDocument() throws Exception {
        QName acmeInvoice = QName.createQName(ACME_NS, "invoice");
        QName acmeLetter  = QName.createQName(ACME_NS, "letter");
        when(nodeService.getType(DOC_REF)).thenReturn(acmeLetter);
        when(dictionaryService.isSubClass(acmeLetter, ContentModel.TYPE_CONTENT)).thenReturn(true);
        when(dictionaryService.isSubClass(acmeLetter, acmeInvoice)).thenReturn(false);
        when(dictionaryService.isSubClass(acmeLetter, acmeLetter)).thenReturn(true);
        when(namespaceService.getNamespaceURI("acme")).thenReturn(ACME_NS);

        AutofilingRule rule = ruleWithTemplate("/Company Home/Finance");
        rule.setContentTypes(Collections.singletonList("acme:invoice")); // doc is acme:letter

        ChildAssociationRef childAssoc = mockChildAssoc(DOC_REF);
        when(folderPathService.getOrCreatePath(rule.getInboxPath(), false)).thenReturn(INBOX_REF);
        when(nodeService.getChildAssocs(eq(INBOX_REF), any(), any()))
            .thenReturn(Collections.singletonList(childAssoc));

        service.processRule(rule);

        verify(fileFolderService, never()).move(any(), any(), any());
    }

    // ───────────────────────────────────────────────────────────────────────
    // processAllRules
    // ───────────────────────────────────────────────────────────────────────

    @Test
    public void processAllRulesDoesNothingWhenNoRules() throws Exception {
        when(ruleService.listEnabledRules()).thenReturn(Collections.emptyList());

        service.processAllRules();

        verify(folderPathService, never()).getOrCreatePath(any(), anyBoolean());
    }

    @Test
    public void processAllRulesSkipsInboxThatDoesNotExist() throws Exception {
        AutofilingRule rule = ruleWithTemplate("/Company Home/Finance");
        when(ruleService.listEnabledRules()).thenReturn(Collections.singletonList(rule));
        when(folderPathService.getOrCreatePath(rule.getInboxPath(), false)).thenReturn(null);

        service.processAllRules();

        verify(fileFolderService, never()).move(any(), any(), any());
    }

    @Test
    public void processAllRulesDoesNotFileSameDocumentTwice() throws Exception {
        // Two rules both matching the same document in the same inbox
        AutofilingRule rule1 = ruleWithTemplate("/Company Home/Finance");
        rule1.setName("Rule1");
        rule1.setPriority(1);

        AutofilingRule rule2 = ruleWithTemplate("/Company Home/Archive");
        rule2.setName("Rule2");
        rule2.setPriority(2);

        ChildAssociationRef childAssoc = mockChildAssoc(DOC_REF);
        when(ruleService.listEnabledRules()).thenReturn(Arrays.asList(rule1, rule2));
        when(folderPathService.getOrCreatePath(rule1.getInboxPath(), false)).thenReturn(INBOX_REF);
        when(folderPathService.getOrCreatePath(rule2.getInboxPath(), false)).thenReturn(INBOX_REF);
        when(nodeService.getChildAssocs(eq(INBOX_REF), any(), any()))
            .thenReturn(Collections.singletonList(childAssoc));

        NodeRef target1 = new NodeRef("workspace://SpacesStore/target1");
        NodeRef target2 = new NodeRef("workspace://SpacesStore/target2");
        when(pathResolver.resolve("/Company Home/Finance", DOC_REF)).thenReturn("/Company Home/Finance");
        when(pathResolver.resolve("/Company Home/Archive", DOC_REF)).thenReturn("/Company Home/Archive");
        when(folderPathService.getOrCreatePath("/Company Home/Finance", true)).thenReturn(target1);
        when(folderPathService.getOrCreatePath("/Company Home/Archive", true)).thenReturn(target2);

        service.processAllRules();

        // Document moved exactly once (by rule1, the higher-priority one)
        verify(fileFolderService, times(1)).move(eq(DOC_REF), any(), isNull());
    }

    // ───────────────────────────────────────────────────────────────────────
    // helpers
    // ───────────────────────────────────────────────────────────────────────

    private AutofilingRule ruleWithTemplate(String template) {
        AutofilingRule rule = new AutofilingRule();
        rule.setName("Test Rule");
        rule.setInboxPath("/Company Home/Inbox");
        rule.setPathTemplate(template);
        rule.setCreateMissingFolders(true);
        rule.setEnabled(true);
        rule.setPriority(10);
        return rule;
    }

    private ChildAssociationRef mockChildAssoc(NodeRef child) {
        ChildAssociationRef assoc = mock(ChildAssociationRef.class);
        when(assoc.getChildRef()).thenReturn(child);
        return assoc;
    }
}
