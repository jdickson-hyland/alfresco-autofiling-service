package org.hyland.com.autofiling.service;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FolderPathServiceImpl implements FolderPathService {

    private static final Log LOG = LogFactory.getLog(FolderPathServiceImpl.class);
    private static final String COMPANY_HOME = "Company Home";
    private static final String PATH_SEPARATOR = "/";

    private NodeService nodeService;
    private NodeLocatorService nodeLocatorService;

    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
    public void setNodeLocatorService(NodeLocatorService nodeLocatorService) { this.nodeLocatorService = nodeLocatorService; }

    @Override
    public NodeRef getOrCreatePath(String absolutePath, boolean createMissing) {
        if (absolutePath == null || absolutePath.trim().isEmpty()) {
            return null;
        }

        String normalised = absolutePath.trim();
        // Strip leading slash so split produces clean segments
        if (normalised.startsWith(PATH_SEPARATOR)) {
            normalised = normalised.substring(1);
        }

        String[] segments = normalised.split(PATH_SEPARATOR, -1);
        if (segments.length == 0) {
            return null;
        }

        // Resolve Company Home via NodeLocatorService
        NodeRef current = nodeLocatorService.getNode("companyhome", null, null);
        if (current == null) {
            LOG.error("Could not locate Company Home");
            return null;
        }

        // The first segment must be "Company Home" — skip it, we already have its NodeRef
        int startIndex = 0;
        if (COMPANY_HOME.equalsIgnoreCase(segments[0])) {
            startIndex = 1;
        }

        for (int i = startIndex; i < segments.length; i++) {
            String segment = segments[i].trim();
            if (segment.isEmpty()) {
                continue;
            }
            NodeRef child = nodeService.getChildByName(current, ContentModel.ASSOC_CONTAINS, segment);
            if (child == null) {
                if (!createMissing) {
                    LOG.warn("Path segment not found and createMissing=false: " + segment);
                    return null;
                }
                child = createFolder(current, segment);
            }
            current = child;
        }

        return current;
    }

    private NodeRef createFolder(NodeRef parent, String name) {
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, name);

        QName assocQName = QName.createQName(
            NamespaceService.CONTENT_MODEL_1_0_URI,
            QName.createValidLocalName(name)
        );
        ChildAssociationRef assoc = nodeService.createNode(
            parent,
            ContentModel.ASSOC_CONTAINS,
            assocQName,
            ContentModel.TYPE_FOLDER,
            props
        );
        LOG.debug("Created folder: " + name + " under " + parent);
        return assoc.getChildRef();
    }
}
