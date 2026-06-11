package org.hyland.com.autofiling.bootstrap;

import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.hyland.com.autofiling.service.AutofilingRuleService;

public class AutofilingBootstrap extends AbstractModuleComponent {

    private AutofilingRuleService ruleService;

    public void setRuleService(AutofilingRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Override
    protected void executeInternal() throws Throwable {
        AuthenticationUtil.runAsSystem(() -> {
            ruleService.ensureRulesFolder();
            return null;
        });
    }
}
