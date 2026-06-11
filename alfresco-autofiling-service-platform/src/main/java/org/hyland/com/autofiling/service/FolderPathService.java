package org.hyland.com.autofiling.service;

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
