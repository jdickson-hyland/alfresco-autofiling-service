package org.hyland.com.autofiling.webscript;

import org.alfresco.service.cmr.repository.NodeRef;
import org.hyland.com.autofiling.model.AutofilingRule;
import org.hyland.com.autofiling.service.AutofilingRuleService;
import org.hyland.com.autofiling.service.AutofilingService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class RuleRunWebScript extends AbstractAutofilingWebScript {

    private AutofilingRuleService ruleService;
    private AutofilingService autofilingService;

    public void setRuleService(AutofilingRuleService ruleService) { this.ruleService = ruleService; }
    public void setAutofilingService(AutofilingService autofilingService) { this.autofilingService = autofilingService; }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef nodeRef = extractNodeRef(req);
        if (nodeRef == null) {
            writeError(res, 400, "Invalid or missing NodeRef in URL");
            return;
        }

        AutofilingRule rule;
        try {
            rule = ruleService.getRule(nodeRef);
        } catch (Exception e) {
            writeError(res, 404, "Rule not found: " + e.getMessage());
            return;
        }

        autofilingService.processRule(rule);
        writeJson(res, "{\"status\":\"completed\",\"rule\":" + jsonString(rule.getName()) + "}");
    }
}
