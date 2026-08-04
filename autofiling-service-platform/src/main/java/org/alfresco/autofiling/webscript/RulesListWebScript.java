/*
 * Copyright 2026 Hyland Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.autofiling.webscript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.alfresco.autofiling.model.AutofilingRule;
import org.alfresco.autofiling.service.AutofilingRuleService;

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
