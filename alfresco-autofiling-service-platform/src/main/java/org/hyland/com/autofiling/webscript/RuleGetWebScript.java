package org.hyland.com.autofiling.webscript;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyland.com.autofiling.model.AutofilingRule;
import org.hyland.com.autofiling.service.AutofilingRuleService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class RuleGetWebScript extends AbstractAutofilingWebScript {

    private static final Log LOG = LogFactory.getLog(RuleGetWebScript.class);

    private AutofilingRuleService ruleService;

    public void setRuleService(AutofilingRuleService ruleService) { this.ruleService = ruleService; }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef nodeRef = extractNodeRef(req);
        if (nodeRef == null) {
            writeError(res, 400, "Invalid or missing NodeRef in URL");
            return;
        }

        LOG.debug("GET rule: " + nodeRef);
        AutofilingRule rule;
        try {
            rule = ruleService.getRule(nodeRef);
        } catch (Exception e) {
            LOG.error("GET rule " + nodeRef + " — not found", e);
            writeError(res, 404, "Rule not found: " + e.getMessage());
            return;
        }

        writeJson(res, rule.toJson().toString(2));
    }
}
