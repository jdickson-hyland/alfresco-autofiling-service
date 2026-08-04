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
package org.alfresco.autofiling.job;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.autofiling.service.AutofilingService;

public class AutofilingJobWorker {

    private static final Log LOG = LogFactory.getLog(AutofilingJobWorker.class);

    private AutofilingService autofilingService;

    public void setAutofilingService(AutofilingService autofilingService) {
        this.autofilingService = autofilingService;
    }

    public void execute() {
        LOG.info("Autofiling job starting");
        AuthenticationUtil.runAsSystem(() -> {
            autofilingService.processAllRules();
            return null;
        });
        LOG.info("Autofiling job complete");
    }
}
