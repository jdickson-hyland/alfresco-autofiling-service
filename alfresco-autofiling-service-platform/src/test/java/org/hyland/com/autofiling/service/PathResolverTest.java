package org.hyland.com.autofiling.service;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class PathResolverTest {

    private static final String CM_NS = "http://www.alfresco.org/model/content/1.0";
    private static final String ACME_NS = "http://www.acme.org/model/1.0";
    private static final NodeRef NODE = new NodeRef("workspace://SpacesStore/test-uuid");

    @Mock private NodeService nodeService;
    @Mock private NamespaceService namespaceService;

    private PathResolver resolver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new PathResolver();
        resolver.setNodeService(nodeService);
        resolver.setNamespaceService(namespaceService);

        // Default namespace resolution for cm: and acme: prefixes
        when(namespaceService.getNamespaceURI("cm")).thenReturn(CM_NS);
        when(namespaceService.getNamespaceURI("acme")).thenReturn(ACME_NS);
    }

    @Test
    public void plainTokenReplacedWithPropertyValue() {
        QName nameQName = QName.createQName(CM_NS, "name");
        when(nodeService.getProperty(NODE, nameQName)).thenReturn("invoice.pdf");

        String result = resolver.resolve("/Company Home/{cm:name}", NODE);

        assertEquals("/Company Home/invoice.pdf", result);
    }

    @Test
    public void dateTokenFormattedWithModifier() {
        QName createdQName = QName.createQName(CM_NS, "created");
        // 2024-03-15
        Date date = new Date(1710460800000L); // 2024-03-15 00:00:00 UTC
        when(nodeService.getProperty(NODE, createdQName)).thenReturn(date);

        String result = resolver.resolve("/Company Home/{cm:created|yyyy}", NODE);

        // Just check the year segment is a 4-digit year (timezone-safe)
        assertEquals(true, result.matches("/Company Home/20\\d{2}"));
    }

    @Test
    public void dateTokenWithSlashFormatProducesPathSegments() {
        QName createdQName = QName.createQName(CM_NS, "created");
        Date date = new Date(1710460800000L);
        when(nodeService.getProperty(NODE, createdQName)).thenReturn(date);

        String result = resolver.resolve("/Company Home/{cm:created|yyyy/MM}", NODE);

        // Should produce something like /Company Home/2024/03
        assertEquals(true, result.matches("/Company Home/20\\d{2}/\\d{2}"));
    }

    @Test
    public void defaultModifierUsedWhenPropertyIsNull() {
        QName vendorQName = QName.createQName(ACME_NS, "vendor");
        when(nodeService.getProperty(NODE, vendorQName)).thenReturn(null);

        String result = resolver.resolve("/Company Home/{acme:vendor|default:Unknown}", NODE);

        assertEquals("/Company Home/Unknown", result);
    }

    @Test
    public void defaultModifierIgnoredWhenPropertyHasValue() {
        QName vendorQName = QName.createQName(ACME_NS, "vendor");
        when(nodeService.getProperty(NODE, vendorQName)).thenReturn("Acme Corp");

        String result = resolver.resolve("/Company Home/{acme:vendor|default:Unknown}", NODE);

        assertEquals("/Company Home/Acme Corp", result);
    }

    @Test
    public void defaultModifierUsedWhenPropertyIsBlank() {
        QName vendorQName = QName.createQName(ACME_NS, "vendor");
        when(nodeService.getProperty(NODE, vendorQName)).thenReturn("   ");

        String result = resolver.resolve("/Company Home/{acme:vendor|default:Unknown}", NODE);

        assertEquals("/Company Home/Unknown", result);
    }

    @Test
    public void nullPropertyWithoutDefaultReturnsEmpty() {
        QName vendorQName = QName.createQName(ACME_NS, "vendor");
        when(nodeService.getProperty(NODE, vendorQName)).thenReturn(null);

        String result = resolver.resolve("/Company Home/{acme:vendor}/Docs", NODE);

        assertEquals("/Company Home//Docs", result);
    }

    @Test
    public void multipleTokensInTemplate() {
        QName yearQName = QName.createQName(ACME_NS, "year");
        QName vendorQName = QName.createQName(ACME_NS, "vendor");
        when(nodeService.getProperty(NODE, yearQName)).thenReturn("2024");
        when(nodeService.getProperty(NODE, vendorQName)).thenReturn("Acme Corp");

        String result = resolver.resolve("/Company Home/{acme:year}/{acme:vendor}", NODE);

        assertEquals("/Company Home/2024/Acme Corp", result);
    }

    @Test
    public void staticTemplatePassedThroughUnchanged() {
        String result = resolver.resolve("/Company Home/Finance/Invoices", NODE);
        assertEquals("/Company Home/Finance/Invoices", result);
    }

    @Test
    public void nullTemplateReturnsNull() {
        String result = resolver.resolve(null, NODE);
        assertEquals(null, result);
    }

    @Test
    public void emptyTemplateReturnsEmpty() {
        String result = resolver.resolve("", NODE);
        assertEquals("", result);
    }

    @Test
    public void invalidCharsStrippedFromPropertyValue() {
        QName nameQName = QName.createQName(CM_NS, "name");
        // Colon, asterisk, and quotes are all forbidden in folder names
        when(nodeService.getProperty(NODE, nameQName)).thenReturn("bad:name*file\"test");

        String result = resolver.resolve("/Company Home/{cm:name}", NODE);

        assertEquals("/Company Home/badnamefiletest", result);
    }

    @Test
    public void unknownPrefixYieldsEmptyToken() {
        // namespaceService returns null for unknown prefix
        when(namespaceService.getNamespaceURI("unknown")).thenReturn(null);

        // Should not throw — should log a warning and return empty for the token
        String result = resolver.resolve("/Company Home/{unknown:prop}/Docs", NODE);

        // Token replaced with empty string
        assertEquals("/Company Home//Docs", result);
    }
}
