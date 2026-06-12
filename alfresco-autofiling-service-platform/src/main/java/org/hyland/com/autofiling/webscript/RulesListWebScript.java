package org.hyland.com.autofiling.webscript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.hyland.com.autofiling.model.AutofilingRule;
import org.hyland.com.autofiling.service.AutofilingRuleService;

import java.io.IOException;
import java.util.List;

public class RulesListWebScript extends AbstractAutofilingWebScript {

    private static final Log LOG = LogFactory.getLog(RulesListWebScript.class);

    private AutofilingRuleService ruleService;

    public void setRuleService(AutofilingRuleService ruleService) { this.ruleService = ruleService; }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        LOG.debug("GET /rules — listing all autofiling rules");
        List<AutofilingRule> rules = ruleService.listRules();
        JSONArray arr = new JSONArray();
        for (AutofilingRule rule : rules) {
            arr.put(rule.toJson());
        }
        LOG.debug("GET /rules — returned " + rules.size() + " rule(s)");
        writeJson(res, arr.toString(2));
    }
}
