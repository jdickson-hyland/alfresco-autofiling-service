package org.hyland.com.autofiling.bootstrap;

import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyland.com.autofiling.service.AutofilingRuleService;

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
