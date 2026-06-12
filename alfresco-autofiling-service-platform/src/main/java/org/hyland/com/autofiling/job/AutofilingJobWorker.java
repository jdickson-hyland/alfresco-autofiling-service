package org.hyland.com.autofiling.job;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyland.com.autofiling.service.AutofilingService;

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
