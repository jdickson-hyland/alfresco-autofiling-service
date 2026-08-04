/*
 * Copyright 2026 Hyland Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.autofiling.service;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.autofiling.model.AutofilingModel;
import org.alfresco.autofiling.model.AutofilingRule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Focuses on the read path degrading gracefully when the Autofiling Rules folder does not
 * exist yet. Listing runs in a read-only transaction, so it must never attempt to create the
 * folder (which would fail the transaction and surface as an HTTP 500) — it should simply
 * return an empty list.
 */
public class AutofilingRuleServiceImplTest {

    private static final NodeRef ROOT_REF     = new NodeRef("workspace://SpacesStore/root");
    private static final NodeRef COMPANY_HOME = new NodeRef("workspace://SpacesStore/companyHome");
    private static final NodeRef DATA_DICT    = new NodeRef("workspace://SpacesStore/dataDictionary");

    @Mock private NodeService nodeService;
    @Mock private ContentService contentService;
    @Mock private NamespaceService namespaceService;
    @Mock private TransactionService transactionService;
    @Mock private RetryingTransactionHelper retryingTransactionHelper;

    private AutofilingRuleServiceImpl service;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AutofilingRuleServiceImpl();
        service.setNodeService(nodeService);
        service.setContentService(contentService);
        service.setNamespaceService(namespaceService);
        service.setTransactionService(transactionService);

        // Execute retrying-transaction callbacks inline (both 2-arg and 3-arg forms).
        when(transactionService.getRetryingTransactionHelper()).thenReturn(retryingTransactionHelper);
        when(retryingTransactionHelper.doInTransaction(any(), anyBoolean()))
            .thenAnswer(inv -> {
                RetryingTransactionHelper.RetryingTransactionCallback<?> cb = inv.getArgument(0);
                return cb.execute();
            });
        when(retryingTransactionHelper.doInTransaction(any(), anyBoolean(), anyBoolean()))
            .thenAnswer(inv -> {
                RetryingTransactionHelper.RetryingTransactionCallback<?> cb = inv.getArgument(0);
                return cb.execute();
            });

        // Navigate root -> Company Home -> Data Dictionary.
        when(nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)).thenReturn(ROOT_REF);
        ChildAssociationRef chAssoc =
            new ChildAssociationRef(ContentModel.ASSOC_CHILDREN, ROOT_REF, null, COMPANY_HOME);
        when(nodeService.getChildAssocs(ROOT_REF)).thenReturn(Collections.singletonList(chAssoc));
        when(nodeService.getProperty(COMPANY_HOME, ContentModel.PROP_NAME)).thenReturn("Company Home");
        when(nodeService.getChildByName(COMPANY_HOME, ContentModel.ASSOC_CONTAINS, "Data Dictionary"))
            .thenReturn(DATA_DICT);
    }

    @Test
    public void listRulesReturnsEmptyWhenFolderMissingAndDoesNotCreateIt() {
        // Rules folder does not exist yet.
        when(nodeService.getChildByName(DATA_DICT, ContentModel.ASSOC_CONTAINS,
            AutofilingModel.AUTOFILING_RULES_FOLDER)).thenReturn(null);

        List<AutofilingRule> rules = service.listRules();

        assertNotNull("listRules must never return null", rules);
        assertTrue("expected an empty rule list when the folder is absent", rules.isEmpty());
        // Critical: the read path must not write inside its read-only transaction.
        verify(nodeService, never()).createNode(any(), any(), any(), any(), anyMap());
    }

    @Test
    public void listEnabledRulesReturnsEmptyWhenFolderMissing() {
        when(nodeService.getChildByName(DATA_DICT, ContentModel.ASSOC_CONTAINS,
            AutofilingModel.AUTOFILING_RULES_FOLDER)).thenReturn(null);

        List<AutofilingRule> enabled = service.listEnabledRules();

        assertNotNull(enabled);
        assertTrue(enabled.isEmpty());
        verify(nodeService, never()).createNode(any(), any(), any(), any(), anyMap());
    }

    @Test
    public void listRulesReturnsEmptyWhenFolderExistsButHasNoRuleChildren() {
        NodeRef rulesFolder = new NodeRef("workspace://SpacesStore/rulesFolder");
        when(nodeService.getChildByName(DATA_DICT, ContentModel.ASSOC_CONTAINS,
            AutofilingModel.AUTOFILING_RULES_FOLDER)).thenReturn(rulesFolder);
        when(nodeService.getChildAssocs(rulesFolder)).thenReturn(Collections.emptyList());

        List<AutofilingRule> rules = service.listRules();

        assertNotNull(rules);
        assertTrue(rules.isEmpty());
        verify(nodeService, never()).createNode(any(), any(), any(), any(), anyMap());
    }
}
