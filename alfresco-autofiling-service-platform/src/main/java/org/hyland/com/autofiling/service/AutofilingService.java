package org.hyland.com.autofiling.service;

import org.alfresco.service.cmr.repository.NodeRef;
import org.hyland.com.autofiling.model.AutofilingRule;

public interface AutofilingService {

    /** Loads all enabled rules and processes each inbox in priority order. */
    void processAllRules();

    /** Scans the inbox defined in the rule and files any matching documents. */
    void processRule(AutofilingRule rule);

    /** Moves a single document to the path resolved from the rule's template. */
    void fileDocument(NodeRef nodeRef, AutofilingRule rule);
}
