package org.hyland.com.autofiling.model;

import org.alfresco.service.namespace.QName;

public interface AutofilingModel {

    String NAMESPACE = "http://www.hyland.com/model/autofiling/1.0";
    String AUTOFILING_RULES_FOLDER = "Autofiling Rules";

    QName ASPECT_AUTOFILING_RULE = QName.createQName(NAMESPACE, "autofilingRule");
    QName PROP_ENABLED           = QName.createQName(NAMESPACE, "enabled");
    QName PROP_INBOX_PATH        = QName.createQName(NAMESPACE, "inboxPath");
    QName PROP_CONTENT_TYPE      = QName.createQName(NAMESPACE, "contentType");
    QName PROP_PRIORITY          = QName.createQName(NAMESPACE, "priority");
}
