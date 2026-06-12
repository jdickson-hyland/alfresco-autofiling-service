package org.hyland.com.autofiling.webscript;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.util.Map;

abstract class AbstractAutofilingWebScript extends AbstractWebScript {

    private static final Log LOG = LogFactory.getLog(AbstractAutofilingWebScript.class);

    protected void writeJson(WebScriptResponse res, String json) throws IOException {
        res.setContentType("application/json");
        res.setContentEncoding("UTF-8");
        res.getWriter().write(json);
    }

    protected void writeError(WebScriptResponse res, int status, String message) throws IOException {
        LOG.warn(getClass().getSimpleName() + " — HTTP " + status + ": " + message);
        res.setContentType("application/json");
        res.setContentEncoding("UTF-8");
        res.setStatus(status);
        res.getWriter().write("{\"error\":" + jsonString(message) + "}");
    }

    protected NodeRef extractNodeRef(WebScriptRequest req) {
        Map<String, String> vars = req.getServiceMatch().getTemplateVars();
        String storeType = vars.get("storeType");
        String storeId   = vars.get("storeId");
        String nodeId    = vars.get("nodeId");
        if (storeType == null || storeId == null || nodeId == null) {
            return null;
        }
        return new NodeRef(storeType + "://" + storeId + "/" + nodeId);
    }

    protected String readBody(WebScriptRequest req) throws IOException {
        return req.getContent().getContent();
    }

    static String jsonString(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
                       .replace("\t", "\\t") + "\"";
    }
}
