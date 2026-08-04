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

public interface FolderPathService {

    /**
     * Resolves an absolute path (e.g. "/Company Home/Finance/Invoices") to a NodeRef.
     * If {@code createMissing} is true, any missing intermediate folders are created.
     * Returns null if the path cannot be resolved and {@code createMissing} is false.
     * Path must start with "/Company Home/".
     */
    NodeRef getOrCreatePath(String absolutePath, boolean createMissing);
}
