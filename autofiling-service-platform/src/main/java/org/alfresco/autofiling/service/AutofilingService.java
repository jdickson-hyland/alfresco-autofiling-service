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
package org.alfresco.autofiling.service;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.autofiling.model.AutofilingRule;

public interface AutofilingService {

    /** Loads all enabled rules and processes each inbox in priority order. */
    void processAllRules();

    /** Scans the inbox defined in the rule and files any matching documents. */
    void processRule(AutofilingRule rule);

    /** Moves a single document to the path resolved from the rule's template. */
    void fileDocument(NodeRef nodeRef, AutofilingRule rule);
}
