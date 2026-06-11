// Server-side controller: pre-load rule list for initial render
var connector = remote.connect("alfresco");
var result = connector.get("/hyland/autofiling/rules");

if (result.status.code == 200) {
    model.rulesJson = result.toString();
    model.error = null;
} else {
    model.rulesJson = "[]";
    model.error = "Failed to load autofiling rules (HTTP " + result.status.code + ")";
}
