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

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.autofiling.model.AutofilingRule;
import org.alfresco.autofiling.service.AutofilingRuleService;
import org.alfresco.autofiling.service.AutofilingService;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class RuleRunWebScript extends AbstractAutofilingWebScript {

    private static final Log LOG = LogFactory.getLog(RuleRunWebScript.class);

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
            LOG.error("POST run — rule not found: " + nodeRef, e);
            writeError(res, 404, "Rule not found: " + e.getMessage());
            return;
        }

        LOG.info("POST run — manually triggering rule '" + rule.getName() + "'");
        autofilingService.processRule(rule);
        LOG.info("POST run — rule '" + rule.getName() + "' completed");
        writeJson(res, "{\"status\":\"completed\",\"rule\":" + jsonString(rule.getName()) + "}");
    }
}
