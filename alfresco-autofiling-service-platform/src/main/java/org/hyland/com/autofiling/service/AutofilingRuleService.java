package org.hyland.com.autofiling.service;

import org.alfresco.service.cmr.repository.NodeRef;
import org.hyland.com.autofiling.model.AutofilingRule;

import java.util.List;

public interface AutofilingRuleService {

    NodeRef createRule(AutofilingRule rule);

    void updateRule(NodeRef nodeRef, AutofilingRule rule);

    AutofilingRule getRule(NodeRef nodeRef);

    List<AutofilingRule> listRules();

    List<AutofilingRule> listEnabledRules();

    void deleteRule(NodeRef nodeRef);

    void ensureRulesFolder();
}
