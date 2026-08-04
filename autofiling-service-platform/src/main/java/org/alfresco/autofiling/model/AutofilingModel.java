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
package org.alfresco.autofiling.model;

import org.alfresco.service.namespace.QName;

public interface AutofilingModel {

    String NAMESPACE = "http://www.alfresco.org/model/autofiling/1.0";
    String AUTOFILING_RULES_FOLDER = "Autofiling Rules";

    QName ASPECT_AUTOFILING_RULE = QName.createQName(NAMESPACE, "autofilingRule");
    QName PROP_ENABLED           = QName.createQName(NAMESPACE, "enabled");
    QName PROP_INBOX_PATH        = QName.createQName(NAMESPACE, "inboxPath");
    QName PROP_CONTENT_TYPE      = QName.createQName(NAMESPACE, "contentType");
    QName PROP_PRIORITY          = QName.createQName(NAMESPACE, "priority");
}
