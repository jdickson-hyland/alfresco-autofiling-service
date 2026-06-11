package org.hyland.com.autofiling.webscript;

import org.alfresco.service.cmr.repository.NodeRef;
import org.hyland.com.autofiling.model.AutofilingRule;
import org.hyland.com.autofiling.service.AutofilingRuleService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class RuleCreateWebScript extends AbstractAutofilingWebScript {

    private AutofilingRuleService ruleService;

    public void setRuleService(AutofilingRuleService ruleService) { this.ruleService = ruleService; }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        String body = readBody(req);
        if (body == null || body.trim().isEmpty()) {
            writeError(res, 400, "Request body is required");
            return;
        }

        AutofilingRule rule;
        try {
            rule = AutofilingRule.fromJson(null, body);
        } catch (Exception e) {
            writeError(res, 400, "Invalid JSON: " + e.getMessage());
            return;
        }

        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            writeError(res, 400, "Rule name is required");
            return;
        }

        NodeRef nodeRef = ruleService.createRule(rule);
        rule.setNodeRef(nodeRef.toString());
        res.setStatus(201);
        writeJson(res, rule.toJson().toString(2));
    }
}
