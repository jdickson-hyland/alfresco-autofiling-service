package org.hyland.com.autofiling.webscript;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyland.com.autofiling.model.AutofilingRule;
import org.hyland.com.autofiling.service.AutofilingRuleService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class RuleCreateWebScript extends AbstractAutofilingWebScript {

    private static final Log LOG = LogFactory.getLog(RuleCreateWebScript.class);

    private AutofilingRuleService ruleService;

    public void setRuleService(AutofilingRuleService ruleService) { this.ruleService = ruleService; }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        LOG.debug("POST /rules — received create request");
        String body = readBody(req);
        if (body == null || body.trim().isEmpty()) {
            writeError(res, 400, "Request body is required");
            return;
        }

        AutofilingRule rule;
        try {
            rule = AutofilingRule.fromJson(null, body);
        } catch (Exception e) {
            LOG.error("POST /rules — failed to parse request body", e);
            writeError(res, 400, "Invalid JSON: " + e.getMessage());
            return;
        }

        if (rule.getName() == null || rule.getName().trim().isEmpty()) {
            writeError(res, 400, "Rule name is required");
            return;
        }

        NodeRef nodeRef = ruleService.createRule(rule);
        rule.setNodeRef(nodeRef.toString());
        LOG.info("POST /rules — created rule '" + rule.getName() + "' (" + nodeRef + ")");
        res.setStatus(201);
        writeJson(res, rule.toJson().toString(2));
    }
}
