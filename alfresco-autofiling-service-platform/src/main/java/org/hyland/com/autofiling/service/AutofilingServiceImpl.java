package org.hyland.com.autofiling.service;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyland.com.autofiling.model.AutofilingRule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AutofilingServiceImpl implements AutofilingService {

    private static final Log LOG = LogFactory.getLog(AutofilingServiceImpl.class);

    private static final long DEFAULT_MINIMUM_AGE_MS = TimeUnit.SECONDS.toMillis(10);

    private AutofilingRuleService ruleService;
    private PathResolver pathResolver;
    private FolderPathService folderPathService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private TransactionService transactionService;
    private long minimumAgeMs = DEFAULT_MINIMUM_AGE_MS;

    public void setRuleService(AutofilingRuleService ruleService) { this.ruleService = ruleService; }
    public void setPathResolver(PathResolver pathResolver) { this.pathResolver = pathResolver; }
    public void setFolderPathService(FolderPathService folderPathService) { this.folderPathService = folderPathService; }
    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
    public void setFileFolderService(FileFolderService fileFolderService) { this.fileFolderService = fileFolderService; }
    public void setDictionaryService(DictionaryService dictionaryService) { this.dictionaryService = dictionaryService; }
    public void setNamespaceService(NamespaceService namespaceService) { this.namespaceService = namespaceService; }
    public void setTransactionService(TransactionService transactionService) { this.transactionService = transactionService; }
    public void setMinimumAgeMs(long minimumAgeMs) { this.minimumAgeMs = minimumAgeMs; }

    @Override
    public void processAllRules() {
        List<AutofilingRule> rules = ruleService.listEnabledRules();
        if (rules.isEmpty()) {
            LOG.debug("No enabled autofiling rules found");
            return;
        }

        // Track nodes already moved this run so a node is not filed twice by overlapping rules
        Set<NodeRef> movedNodes = new HashSet<>();
        for (AutofilingRule rule : rules) {
            try {
                processRuleInternal(rule, movedNodes);
            } catch (Exception e) {
                LOG.error("Error processing autofiling rule: " + rule.getName(), e);
            }
        }
    }

    @Override
    public void processRule(AutofilingRule rule) {
        processRuleInternal(rule, new HashSet<>());
    }

    private void processRuleInternal(AutofilingRule rule, Set<NodeRef> movedNodes) {
        AuthenticationUtil.runAsSystem(() -> {
            NodeRef inboxRef = folderPathService.getOrCreatePath(rule.getInboxPath(), false);
            if (inboxRef == null) {
                LOG.warn("Inbox folder not found for rule '" + rule.getName() + "': " + rule.getInboxPath());
                return null;
            }

            List<ChildAssociationRef> children = nodeService.getChildAssocs(
                inboxRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

            for (ChildAssociationRef child : children) {
                NodeRef childRef = child.getChildRef();
                if (movedNodes.contains(childRef)) {
                    continue;
                }
                if (!nodeService.exists(childRef)) {
                    continue;
                }
                if (!isContent(childRef)) {
                    continue;
                }
                if (!contentTypesMatch(childRef, rule)) {
                    continue;
                }
                if (!isOldEnough(childRef)) {
                    LOG.debug("Skipping " + childRef + " — modified less than " + minimumAgeMs + " ms ago");
                    continue;
                }

                final NodeRef docRef = childRef;
                try {
                    transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                        fileDocument(docRef, rule);
                        movedNodes.add(docRef);
                        return null;
                    }, false, true);
                } catch (Exception e) {
                    LOG.error("Failed to file document " + docRef + " with rule '" + rule.getName() + "'", e);
                }
            }
            return null;
        });
    }

    @Override
    public void fileDocument(NodeRef nodeRef, AutofilingRule rule) {
        String resolvedPath = pathResolver.resolve(rule.getPathTemplate(), nodeRef);
        if (resolvedPath == null || resolvedPath.trim().isEmpty()) {
            LOG.warn("Path template resolved to empty string for node " + nodeRef + ", rule: " + rule.getName());
            return;
        }

        NodeRef targetFolder = folderPathService.getOrCreatePath(resolvedPath, rule.isCreateMissingFolders());
        if (targetFolder == null) {
            LOG.warn("Target folder could not be resolved for path '" + resolvedPath + "', rule: " + rule.getName());
            return;
        }

        NodeRef currentParent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        if (currentParent != null && currentParent.equals(targetFolder)) {
            LOG.debug("Document " + nodeRef + " is already in the target folder — no move needed");
            return;
        }

        try {
            fileFolderService.move(nodeRef, targetFolder, null);
            LOG.info("Filed document " + nodeRef + " to path: " + resolvedPath);
        } catch (FileExistsException e) {
            String originalName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            String dedupedName = buildDedupedName(originalName);
            LOG.warn("Name collision at destination for '" + originalName + "' — retrying as '" + dedupedName + "'");
            try {
                fileFolderService.move(nodeRef, targetFolder, dedupedName);
                LOG.info("Filed document (deduped) " + nodeRef + " to path: " + resolvedPath);
            } catch (FileExistsException | FileNotFoundException ex) {
                LOG.error("Failed to file document " + nodeRef + " even with deduped name", ex);
            }
        } catch (FileNotFoundException e) {
            LOG.error("Move failed — node or target folder not found for " + nodeRef, e);
        }
    }

    private boolean isOldEnough(NodeRef nodeRef) {
        Date modified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        if (modified == null) {
            return true;
        }
        return System.currentTimeMillis() - modified.getTime() >= minimumAgeMs;
    }

    private boolean isContent(NodeRef nodeRef) {
        QName type = nodeService.getType(nodeRef);
        return dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT);
    }

    private boolean contentTypesMatch(NodeRef nodeRef, AutofilingRule rule) {
        List<String> contentTypes = rule.getContentTypes();
        if (contentTypes == null || contentTypes.isEmpty()) {
            return true; // wildcard — matches any content type
        }
        QName nodeType = nodeService.getType(nodeRef);
        for (String typeStr : contentTypes) {
            try {
                QName ruleType = QName.createQName(typeStr, namespaceService);
                if (nodeType.equals(ruleType) || dictionaryService.isSubClass(nodeType, ruleType)) {
                    return true;
                }
            } catch (Exception e) {
                LOG.warn("Could not resolve content type QName from rule: " + typeStr, e);
            }
        }
        return false;
    }

    private String buildDedupedName(String originalName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            return originalName.substring(0, dotIndex) + "_" + timestamp + originalName.substring(dotIndex);
        }
        return originalName + "_" + timestamp;
    }
}
