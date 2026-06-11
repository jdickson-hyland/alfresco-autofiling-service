package org.hyland.com.autofiling.service;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyland.com.autofiling.model.AutofilingModel;
import org.hyland.com.autofiling.model.AutofilingRule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutofilingRuleServiceImpl implements AutofilingRuleService {

    private static final Log LOG = LogFactory.getLog(AutofilingRuleServiceImpl.class);

    private NodeService nodeService;
    private ContentService contentService;
    private NamespaceService namespaceService;

    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }
    public void setNamespaceService(NamespaceService namespaceService) { this.namespaceService = namespaceService; }

    @Override
    public NodeRef createRule(AutofilingRule rule) {
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef rulesFolder = getOrCreateRulesFolder();

            Map<QName, Serializable> props = new HashMap<>();
            props.put(ContentModel.PROP_NAME, rule.getName());
            props.put(ContentModel.PROP_TITLE, rule.getName());
            props.put(ContentModel.PROP_DESCRIPTION, rule.getDescription() != null ? rule.getDescription() : "");

            QName assocQName = QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName(rule.getName())
            );
            ChildAssociationRef assoc = nodeService.createNode(
                rulesFolder,
                ContentModel.ASSOC_CONTAINS,
                assocQName,
                ContentModel.TYPE_CONTENT,
                props
            );
            NodeRef nodeRef = assoc.getChildRef();

            applyAspect(nodeRef, rule);
            writeJsonContent(nodeRef, rule);

            return nodeRef;
        });
    }

    @Override
    public void updateRule(NodeRef nodeRef, AutofilingRule rule) {
        AuthenticationUtil.runAsSystem(() -> {
            nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, rule.getName());
            nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, rule.getName());
            nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION,
                rule.getDescription() != null ? rule.getDescription() : "");
            applyAspect(nodeRef, rule);
            writeJsonContent(nodeRef, rule);
            return null;
        });
    }

    @Override
    public AutofilingRule getRule(NodeRef nodeRef) {
        return AuthenticationUtil.runAsSystem(() -> buildRule(nodeRef));
    }

    @Override
    public List<AutofilingRule> listRules() {
        return AuthenticationUtil.runAsSystem(() -> {
            NodeRef folder = getOrCreateRulesFolder();
            List<ChildAssociationRef> children = nodeService.getChildAssocs(folder);
            List<AutofilingRule> result = new ArrayList<>();
            for (ChildAssociationRef child : children) {
                NodeRef childRef = child.getChildRef();
                if (nodeService.hasAspect(childRef, AutofilingModel.ASPECT_AUTOFILING_RULE)) {
                    result.add(buildRule(childRef));
                }
            }
            result.sort((a, b) -> Integer.compare(a.getPriority(), b.getPriority()));
            return result;
        });
    }

    @Override
    public List<AutofilingRule> listEnabledRules() {
        return AuthenticationUtil.runAsSystem(() -> {
            List<AutofilingRule> all = listRules();
            List<AutofilingRule> enabled = new ArrayList<>();
            for (AutofilingRule rule : all) {
                if (rule.isEnabled()) {
                    enabled.add(rule);
                }
            }
            return enabled;
        });
    }

    @Override
    public void deleteRule(NodeRef nodeRef) {
        AuthenticationUtil.runAsSystem(() -> {
            nodeService.deleteNode(nodeRef);
            return null;
        });
    }

    @Override
    public void ensureRulesFolder() {
        AuthenticationUtil.runAsSystem(() -> {
            getOrCreateRulesFolder();
            return null;
        });
    }

    NodeRef getOrCreateRulesFolder() {
        NodeRef rootRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        NodeRef companyHome = findCompanyHome(rootRef);
        NodeRef dataDictionary = nodeService.getChildByName(
            companyHome, ContentModel.ASSOC_CONTAINS, "Data Dictionary");
        if (dataDictionary == null) {
            throw new IllegalStateException("Cannot find Data Dictionary under Company Home");
        }

        NodeRef rulesFolder = nodeService.getChildByName(
            dataDictionary, ContentModel.ASSOC_CONTAINS, AutofilingModel.AUTOFILING_RULES_FOLDER);

        if (rulesFolder == null) {
            Map<QName, Serializable> props = new HashMap<>();
            props.put(ContentModel.PROP_NAME, AutofilingModel.AUTOFILING_RULES_FOLDER);

            QName assocQName = QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName(AutofilingModel.AUTOFILING_RULES_FOLDER)
            );
            ChildAssociationRef assoc = nodeService.createNode(
                dataDictionary,
                ContentModel.ASSOC_CONTAINS,
                assocQName,
                ContentModel.TYPE_FOLDER,
                props
            );
            rulesFolder = assoc.getChildRef();
            LOG.info("Created Autofiling Rules folder in Data Dictionary");
        }

        return rulesFolder;
    }

    private NodeRef findCompanyHome(NodeRef storeRoot) {
        for (ChildAssociationRef assoc : nodeService.getChildAssocs(storeRoot)) {
            String name = (String) nodeService.getProperty(assoc.getChildRef(), ContentModel.PROP_NAME);
            if ("Company Home".equals(name)) {
                return assoc.getChildRef();
            }
        }
        throw new IllegalStateException("Cannot find Company Home under store root: " + storeRoot);
    }

    private void applyAspect(NodeRef nodeRef, AutofilingRule rule) {
        Map<QName, Serializable> aspectProps = new HashMap<>();
        aspectProps.put(AutofilingModel.PROP_ENABLED, rule.isEnabled());
        aspectProps.put(AutofilingModel.PROP_INBOX_PATH, rule.getInboxPath() != null ? rule.getInboxPath() : "");
        aspectProps.put(AutofilingModel.PROP_PRIORITY, rule.getPriority());

        ArrayList<String> types = rule.getContentTypes() != null
            ? new ArrayList<>(rule.getContentTypes())
            : new ArrayList<>();
        aspectProps.put(AutofilingModel.PROP_CONTENT_TYPE, types);

        if (!nodeService.hasAspect(nodeRef, AutofilingModel.ASPECT_AUTOFILING_RULE)) {
            nodeService.addAspect(nodeRef, AutofilingModel.ASPECT_AUTOFILING_RULE, aspectProps);
        } else {
            nodeService.setProperty(nodeRef, AutofilingModel.PROP_ENABLED, rule.isEnabled());
            nodeService.setProperty(nodeRef, AutofilingModel.PROP_INBOX_PATH,
                rule.getInboxPath() != null ? rule.getInboxPath() : "");
            nodeService.setProperty(nodeRef, AutofilingModel.PROP_PRIORITY, rule.getPriority());
            nodeService.setProperty(nodeRef, AutofilingModel.PROP_CONTENT_TYPE, types);
        }
    }

    private void writeJsonContent(NodeRef nodeRef, AutofilingRule rule) {
        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        writer.setMimetype("application/json");
        writer.setEncoding("UTF-8");
        writer.putContent(rule.toJson().toString(2));
    }

    private AutofilingRule buildRule(NodeRef nodeRef) {
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        String json = "";
        if (reader != null && reader.exists()) {
            json = reader.getContentString();
        }
        AutofilingRule rule = AutofilingRule.fromJson(nodeRef.toString(), json);

        // Sync enabled flag from the aspect property (authoritative for runtime)
        Boolean enabled = (Boolean) nodeService.getProperty(nodeRef, AutofilingModel.PROP_ENABLED);
        if (enabled != null) {
            rule.setEnabled(enabled);
        }
        return rule;
    }
}
