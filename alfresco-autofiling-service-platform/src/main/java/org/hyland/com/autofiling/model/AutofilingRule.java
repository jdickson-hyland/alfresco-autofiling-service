package org.hyland.com.autofiling.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AutofilingRule {

    private String nodeRef;
    private String name;
    private String description;
    private boolean enabled;
    private String inboxPath;
    private List<String> contentTypes;
    private String pathTemplate;
    private boolean createMissingFolders;
    private int priority;

    public AutofilingRule() {
        this.contentTypes = new ArrayList<>();
        this.createMissingFolders = true;
        this.priority = 10;
        this.enabled = true;
    }

    public static AutofilingRule fromJson(String nodeRefStr, String jsonContent) {
        JSONObject obj = new JSONObject(jsonContent);
        AutofilingRule rule = new AutofilingRule();
        rule.nodeRef = nodeRefStr;
        rule.name = obj.optString("name", "");
        rule.description = obj.optString("description", "");
        rule.enabled = obj.optBoolean("enabled", true);
        rule.inboxPath = obj.optString("inboxPath", "");
        rule.pathTemplate = obj.optString("pathTemplate", "");
        rule.createMissingFolders = obj.optBoolean("createMissingFolders", true);
        rule.priority = obj.optInt("priority", 10);
        rule.contentTypes = new ArrayList<>();
        JSONArray types = obj.optJSONArray("contentTypes");
        if (types != null) {
            for (int i = 0; i < types.length(); i++) {
                rule.contentTypes.add(types.getString(i));
            }
        }
        return rule;
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("nodeRef", nodeRef != null ? nodeRef : "");
        obj.put("name", name != null ? name : "");
        obj.put("description", description != null ? description : "");
        obj.put("enabled", enabled);
        obj.put("inboxPath", inboxPath != null ? inboxPath : "");
        obj.put("pathTemplate", pathTemplate != null ? pathTemplate : "");
        obj.put("createMissingFolders", createMissingFolders);
        obj.put("priority", priority);
        JSONArray types = new JSONArray();
        if (contentTypes != null) {
            for (String t : contentTypes) {
                types.put(t);
            }
        }
        obj.put("contentTypes", types);
        return obj;
    }

    public String getNodeRef() { return nodeRef; }
    public void setNodeRef(String nodeRef) { this.nodeRef = nodeRef; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getInboxPath() { return inboxPath; }
    public void setInboxPath(String inboxPath) { this.inboxPath = inboxPath; }

    public List<String> getContentTypes() { return contentTypes; }
    public void setContentTypes(List<String> contentTypes) { this.contentTypes = contentTypes; }

    public String getPathTemplate() { return pathTemplate; }
    public void setPathTemplate(String pathTemplate) { this.pathTemplate = pathTemplate; }

    public boolean isCreateMissingFolders() { return createMissingFolders; }
    public void setCreateMissingFolders(boolean createMissingFolders) { this.createMissingFolders = createMissingFolders; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
