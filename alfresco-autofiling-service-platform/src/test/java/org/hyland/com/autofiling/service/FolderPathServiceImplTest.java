package org.hyland.com.autofiling.service;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.Serializable;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class FolderPathServiceImplTest {

    private static final NodeRef COMPANY_HOME = new NodeRef("workspace://SpacesStore/company-home");
    private static final NodeRef FINANCE_REF   = new NodeRef("workspace://SpacesStore/finance");
    private static final NodeRef INVOICES_REF  = new NodeRef("workspace://SpacesStore/invoices");
    private static final NodeRef NEW_FOLDER    = new NodeRef("workspace://SpacesStore/new-folder");

    @Mock private NodeService nodeService;
    @Mock private NodeLocatorService nodeLocatorService;

    private FolderPathServiceImpl service;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new FolderPathServiceImpl();
        service.setNodeService(nodeService);
        service.setNodeLocatorService(nodeLocatorService);

        when(nodeLocatorService.getNode("companyhome", null, null)).thenReturn(COMPANY_HOME);
    }

    @Test
    public void resolvesTwoLevelExistingPath() {
        when(nodeService.getChildByName(COMPANY_HOME, ContentModel.ASSOC_CONTAINS, "Finance"))
            .thenReturn(FINANCE_REF);
        when(nodeService.getChildByName(FINANCE_REF, ContentModel.ASSOC_CONTAINS, "Invoices"))
            .thenReturn(INVOICES_REF);

        NodeRef result = service.getOrCreatePath("/Company Home/Finance/Invoices", false);

        assertEquals(INVOICES_REF, result);
        verify(nodeService, never()).createNode(any(), any(), any(), any(), any());
    }

    @Test
    public void createsOneMissingSegment() {
        when(nodeService.getChildByName(COMPANY_HOME, ContentModel.ASSOC_CONTAINS, "Finance"))
            .thenReturn(FINANCE_REF);
        when(nodeService.getChildByName(FINANCE_REF, ContentModel.ASSOC_CONTAINS, "Invoices"))
            .thenReturn(null); // missing

        ChildAssociationRef assoc = mock(ChildAssociationRef.class);
        when(assoc.getChildRef()).thenReturn(NEW_FOLDER);
        when(nodeService.createNode(eq(FINANCE_REF), eq(ContentModel.ASSOC_CONTAINS),
            any(QName.class), eq(ContentModel.TYPE_FOLDER), any())).thenReturn(assoc);

        NodeRef result = service.getOrCreatePath("/Company Home/Finance/Invoices", true);

        assertEquals(NEW_FOLDER, result);
        verify(nodeService, times(1)).createNode(eq(FINANCE_REF), eq(ContentModel.ASSOC_CONTAINS),
            any(QName.class), eq(ContentModel.TYPE_FOLDER), any());
    }

    @Test
    public void returnsNullWhenSegmentMissingAndCreateFalse() {
        when(nodeService.getChildByName(COMPANY_HOME, ContentModel.ASSOC_CONTAINS, "Finance"))
            .thenReturn(null);

        NodeRef result = service.getOrCreatePath("/Company Home/Finance/Invoices", false);

        assertNull(result);
        verify(nodeService, never()).createNode(any(), any(), any(), any(), any());
    }

    @Test
    public void returnsCompanyHomeForPathWithOnlyCompanyHome() {
        NodeRef result = service.getOrCreatePath("/Company Home", false);
        assertEquals(COMPANY_HOME, result);
    }

    @Test
    public void returnsNullForNullPath() {
        NodeRef result = service.getOrCreatePath(null, true);
        assertNull(result);
    }

    @Test
    public void returnsNullForEmptyPath() {
        NodeRef result = service.getOrCreatePath("  ", true);
        assertNull(result);
    }

    @Test
    public void createdFolderHasCorrectName() {
        when(nodeService.getChildByName(COMPANY_HOME, ContentModel.ASSOC_CONTAINS, "NewFolder"))
            .thenReturn(null);

        ChildAssociationRef assoc = mock(ChildAssociationRef.class);
        when(assoc.getChildRef()).thenReturn(NEW_FOLDER);
        when(nodeService.createNode(any(), any(), any(), any(), any())).thenReturn(assoc);

        service.getOrCreatePath("/Company Home/NewFolder", true);

        ArgumentCaptor<Map<QName, Serializable>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(nodeService).createNode(eq(COMPANY_HOME), eq(ContentModel.ASSOC_CONTAINS),
            any(QName.class), eq(ContentModel.TYPE_FOLDER), propsCaptor.capture());

        assertEquals("NewFolder", propsCaptor.getValue().get(ContentModel.PROP_NAME));
    }

    @Test
    public void handlesPathWithoutLeadingSlash() {
        when(nodeService.getChildByName(COMPANY_HOME, ContentModel.ASSOC_CONTAINS, "Finance"))
            .thenReturn(FINANCE_REF);

        NodeRef result = service.getOrCreatePath("Company Home/Finance", false);

        assertEquals(FINANCE_REF, result);
    }

    @Test
    public void handlesConsecutiveSlashesGracefully() {
        when(nodeService.getChildByName(COMPANY_HOME, ContentModel.ASSOC_CONTAINS, "Finance"))
            .thenReturn(FINANCE_REF);

        // "/Company Home//Finance" — the empty segment between slashes is skipped
        NodeRef result = service.getOrCreatePath("/Company Home//Finance", false);

        assertEquals(FINANCE_REF, result);
    }

    @Test
    public void createsMultipleMissingSegments() {
        when(nodeService.getChildByName(COMPANY_HOME, ContentModel.ASSOC_CONTAINS, "A"))
            .thenReturn(null);

        NodeRef aRef = new NodeRef("workspace://SpacesStore/a");
        NodeRef bRef = new NodeRef("workspace://SpacesStore/b");

        ChildAssociationRef aAssoc = mock(ChildAssociationRef.class);
        when(aAssoc.getChildRef()).thenReturn(aRef);
        ChildAssociationRef bAssoc = mock(ChildAssociationRef.class);
        when(bAssoc.getChildRef()).thenReturn(bRef);

        when(nodeService.createNode(eq(COMPANY_HOME), any(), any(), any(), any())).thenReturn(aAssoc);
        when(nodeService.getChildByName(aRef, ContentModel.ASSOC_CONTAINS, "B")).thenReturn(null);
        when(nodeService.createNode(eq(aRef), any(), any(), any(), any())).thenReturn(bAssoc);

        NodeRef result = service.getOrCreatePath("/Company Home/A/B", true);

        assertEquals(bRef, result);
        verify(nodeService, times(2)).createNode(any(), any(), any(), any(), any());
    }
}
