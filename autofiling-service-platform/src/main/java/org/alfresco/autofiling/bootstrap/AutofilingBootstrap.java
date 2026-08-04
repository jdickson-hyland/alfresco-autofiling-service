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
package org.alfresco.autofiling.bootstrap;

import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.autofiling.service.AutofilingRuleService;

public class AutofilingBootstrap extends AbstractModuleComponent {

    private static final Log LOG = LogFactory.getLog(AutofilingBootstrap.class);

    private AutofilingRuleService ruleService;

    public void setRuleService(AutofilingRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Override
    protected void executeInternal() throws Throwable {
        LOG.info("AutofilingBootstrap: ensuring Autofiling Rules folder exists");
        AuthenticationUtil.runAsSystem(() -> {
            ruleService.ensureRulesFolder();
            return null;
        });
        LOG.info("AutofilingBootstrap: bootstrap complete");
    }
}
