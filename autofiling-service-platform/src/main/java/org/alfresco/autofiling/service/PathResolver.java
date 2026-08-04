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
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves path templates containing {prefix:localName} tokens against a node's properties.
 *
 * Token syntax:
 *   {prefix:localName}               - plain property value
 *   {cm:created|yyyy/MM}             - Date property formatted with SimpleDateFormat pattern
 *   {acme:vendor|default:Unknown}    - use "Unknown" when the property is null or blank
 *
 * Slashes inside formatted Date values (e.g. "2024/03") become real path separators —
 * this is intentional: the resolved template is later split on "/" to build folder segments.
 *
 * Invalid folder-name characters ( \ : * ? " < > | ) are stripped from raw property values
 * but NOT from the template's static text or date-produced slashes.
 */
public class PathResolver {

    private static final Log LOG = LogFactory.getLog(PathResolver.class);

    // Matches {prop} or {prop|modifier}
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\{([^|}]+)(?:\\|([^}]*))?\\}");
    private static final String INVALID_CHARS_REGEX = "[\\\\:*?\"<>|]";

    private NodeService nodeService;
    private NamespaceService namespaceService;

    public void setNodeService(NodeService nodeService) { this.nodeService = nodeService; }
    public void setNamespaceService(NamespaceService namespaceService) { this.namespaceService = namespaceService; }

    /**
     * Resolves all tokens in {@code template} using property values from {@code nodeRef}.
     * Returns the resolved path string (which still contains "/" as segment separator).
     */
    public String resolve(String template, NodeRef nodeRef) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        Matcher matcher = TOKEN_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String propPart = matcher.group(1).trim();
            String modifier = matcher.group(2) != null ? matcher.group(2).trim() : null;
            String replacement = resolveToken(propPart, modifier, nodeRef);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        String resolved = result.toString();
        LOG.debug("Resolved template '" + template + "' → '" + resolved + "' for " + nodeRef);
        return resolved;
    }

    private String resolveToken(String propRef, String modifier, NodeRef nodeRef) {
        QName propQName;
        try {
            propQName = QName.createQName(propRef, namespaceService);
        } catch (Exception e) {
            LOG.warn("Cannot resolve QName from token: " + propRef, e);
            return "";
        }

        Serializable value = nodeService.getProperty(nodeRef, propQName);

        // Handle default: modifier
        if (modifier != null && modifier.startsWith("default:")) {
            String defaultValue = modifier.substring("default:".length());
            if (value == null || value.toString().trim().isEmpty()) {
                return sanitise(defaultValue);
            }
        }

        if (value == null) {
            return "";
        }

        // Date formatting — slashes in the format pattern intentionally become path separators
        if (value instanceof Date) {
            String fmt = (modifier != null && !modifier.startsWith("default:")) ? modifier : "yyyy-MM-dd";
            return new SimpleDateFormat(fmt).format((Date) value);
        }

        return sanitise(value.toString());
    }

    private String sanitise(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll(INVALID_CHARS_REGEX, "").trim();
    }
}
