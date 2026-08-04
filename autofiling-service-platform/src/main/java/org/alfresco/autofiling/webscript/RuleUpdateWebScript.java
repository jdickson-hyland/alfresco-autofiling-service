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
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

public class RuleUpdateWebScript extends AbstractAutofilingWebScript {

    private static final Log LOG = LogFactory.getLog(RuleUpdateWebScript.class);

    private AutofilingRuleService ruleService;

    public void setRuleService(AutofilingRuleService ruleService) { this.ruleService = ruleService; }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        NodeRef nodeRef = extractNodeRef(req);
        if (nodeRef == null) {
            writeError(res, 400, "Invalid or missing NodeRef in URL");
            return;
        }

        LOG.debug("PUT rule: " + nodeRef);
        String body = readBody(req);
        if (body == null || body.trim().isEmpty()) {
            writeError(res, 400, "Request body is required");
            return;
        }

        AutofilingRule rule;
        try {
            rule = AutofilingRule.fromJson(nodeRef.toString(), body);
        } catch (Exception e) {
            LOG.error("PUT rule " + nodeRef + " — failed to parse request body", e);
            writeError(res, 400, "Invalid JSON: " + e.getMessage());
            return;
        }

        ruleService.updateRule(nodeRef, rule);
        LOG.info("PUT rule — updated '" + rule.getName() + "' (" + nodeRef + ")");
        writeJson(res, ruleService.getRule(nodeRef).toJson().toString(2));
    }
}
