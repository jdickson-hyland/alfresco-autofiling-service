<#-- Autofiling Rule Manager — Share Admin Console page -->
<style>
  #autofiling-admin * { box-sizing: border-box; }

  #autofiling-admin {
    font-family: Arial, sans-serif;
    font-size: 13px;
    color: #333;
    padding: 16px 24px;
    max-width: 1100px;
  }

  #autofiling-admin h1 {
    font-size: 18px;
    font-weight: bold;
    margin: 0 0 4px;
    color: #1a1a1a;
  }

  #autofiling-admin .subtitle {
    color: #666;
    margin: 0 0 20px;
    font-size: 12px;
  }

  /* ── toolbar ─────────────────────────────────────────── */
  .afi-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  /* ── buttons ─────────────────────────────────────────── */
  .afi-btn {
    display: inline-block;
    padding: 6px 14px;
    font-size: 12px;
    cursor: pointer;
    border-radius: 3px;
    border: 1px solid #ccc;
    background: #f5f5f5;
    color: #333;
    text-decoration: none;
    line-height: 1.4;
  }
  .afi-btn:hover { background: #e8e8e8; }
  .afi-btn.primary { background: #2a6eb5; color: #fff; border-color: #1e5a9c; }
  .afi-btn.primary:hover { background: #1e5a9c; }
  .afi-btn.danger { background: #c0392b; color: #fff; border-color: #a93226; }
  .afi-btn.danger:hover { background: #a93226; }
  .afi-btn.success { background: #27ae60; color: #fff; border-color: #1e8449; }
  .afi-btn.success:hover { background: #1e8449; }
  .afi-btn.small { padding: 3px 9px; font-size: 11px; }

  /* ── table ───────────────────────────────────────────── */
  .afi-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 12px;
  }
  .afi-table th {
    background: #f0f0f0;
    border: 1px solid #ddd;
    padding: 7px 10px;
    text-align: left;
    font-weight: bold;
    white-space: nowrap;
  }
  .afi-table td {
    border: 1px solid #ddd;
    padding: 7px 10px;
    vertical-align: middle;
  }
  .afi-table tr:nth-child(even) td { background: #fafafa; }
  .afi-table tr:hover td { background: #f0f6ff; }
  .afi-table .actions { white-space: nowrap; }
  .afi-table .actions .afi-btn { margin-right: 4px; }

  /* ── badges ──────────────────────────────────────────── */
  .badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: bold;
  }
  .badge.enabled  { background: #d4efdf; color: #1e8449; }
  .badge.disabled { background: #f2f2f2; color: #888; }

  /* ── empty state ─────────────────────────────────────── */
  .afi-empty {
    text-align: center;
    padding: 40px;
    color: #888;
    font-size: 13px;
    background: #fafafa;
    border: 1px dashed #ddd;
    border-radius: 4px;
    margin-top: 8px;
  }

  /* ── notifications ───────────────────────────────────── */
  .afi-msg {
    padding: 9px 14px;
    border-radius: 3px;
    margin-bottom: 12px;
    font-size: 12px;
    display: none;
  }
  .afi-msg.error   { background: #fdecea; color: #922b21; border: 1px solid #f1948a; }
  .afi-msg.success { background: #d4efdf; color: #1a5e35; border: 1px solid #82e0aa; }
  .afi-msg.info    { background: #d6eaf8; color: #1a5276; border: 1px solid #85c1e9; }

  /* ── editor view ─────────────────────────────────────── */
  #afi-editor-view { display: none; }

  .afi-back-link {
    color: #2a6eb5;
    cursor: pointer;
    font-size: 12px;
    margin-bottom: 16px;
    display: inline-block;
    text-decoration: none;
  }
  .afi-back-link:hover { text-decoration: underline; }

  .afi-form-title {
    font-size: 16px;
    font-weight: bold;
    margin: 0 0 16px;
    color: #1a1a1a;
  }

  .afi-form {
    background: #fff;
    border: 1px solid #ddd;
    border-radius: 4px;
    padding: 20px 24px;
  }

  .afi-field {
    margin-bottom: 14px;
  }
  .afi-field label {
    display: block;
    font-weight: bold;
    font-size: 12px;
    margin-bottom: 4px;
    color: #444;
  }
  .afi-field label .required { color: #c0392b; margin-left: 2px; }
  .afi-field label .hint {
    font-weight: normal;
    color: #888;
    font-size: 11px;
    margin-left: 6px;
  }

  .afi-input, .afi-textarea, .afi-select {
    width: 100%;
    padding: 6px 8px;
    border: 1px solid #ccc;
    border-radius: 3px;
    font-size: 12px;
    font-family: Arial, sans-serif;
    color: #333;
  }
  .afi-input:focus, .afi-textarea:focus { border-color: #2a6eb5; outline: none; }
  .afi-textarea { resize: vertical; min-height: 60px; }

  .afi-checkbox-row {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  .afi-checkbox-row input { margin: 0; cursor: pointer; }
  .afi-checkbox-row label { font-weight: normal; cursor: pointer; margin: 0; }

  /* content type tags */
  .afi-tag-input-wrap {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
    padding: 5px 6px;
    border: 1px solid #ccc;
    border-radius: 3px;
    min-height: 34px;
    cursor: text;
    background: #fff;
  }
  .afi-tag-input-wrap:focus-within { border-color: #2a6eb5; }
  .afi-tag {
    display: inline-flex;
    align-items: center;
    background: #d6eaf8;
    color: #1a5276;
    border-radius: 3px;
    padding: 2px 7px;
    font-size: 11px;
    gap: 4px;
  }
  .afi-tag .remove {
    cursor: pointer;
    color: #7fb3d3;
    font-size: 13px;
    line-height: 1;
    background: none;
    border: none;
    padding: 0;
  }
  .afi-tag .remove:hover { color: #1a5276; }
  .afi-tag-input {
    border: none;
    outline: none;
    font-size: 12px;
    min-width: 160px;
    flex: 1;
    padding: 1px 2px;
    font-family: Arial, sans-serif;
  }

  /* template field + helper panel side by side */
  .afi-template-row {
    display: flex;
    gap: 12px;
    align-items: flex-start;
  }
  .afi-template-row .afi-textarea { flex: 1; min-height: 80px; }

  /* token helper */
  .afi-token-helper {
    width: 280px;
    flex-shrink: 0;
    border: 1px solid #ddd;
    border-radius: 3px;
    font-size: 11px;
    background: #fafafa;
  }
  .afi-token-header {
    padding: 6px 10px;
    background: #f0f0f0;
    border-bottom: 1px solid #ddd;
    font-weight: bold;
    font-size: 11px;
    cursor: pointer;
    user-select: none;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .afi-token-body { padding: 10px; }
  .afi-token-body table { width: 100%; border-collapse: collapse; margin-bottom: 10px; }
  .afi-token-body table th {
    text-align: left;
    font-weight: bold;
    font-size: 10px;
    color: #666;
    padding: 2px 4px;
    border-bottom: 1px solid #eee;
  }
  .afi-token-body table td { padding: 3px 4px; font-size: 10px; vertical-align: top; }
  .afi-token-body table td code { background: #e8f0fe; padding: 1px 3px; border-radius: 2px; font-family: monospace; font-size: 10px; }
  .afi-token-body .section-label { font-weight: bold; color: #444; margin: 8px 0 4px; font-size: 11px; }
  .afi-token-btn {
    display: inline-block;
    margin: 2px;
    padding: 2px 7px;
    background: #e8f0fe;
    color: #1a5276;
    border: 1px solid #85c1e9;
    border-radius: 3px;
    font-size: 10px;
    font-family: monospace;
    cursor: pointer;
  }
  .afi-token-btn:hover { background: #d6eaf8; }

  /* form actions */
  .afi-form-actions {
    margin-top: 18px;
    padding-top: 14px;
    border-top: 1px solid #eee;
    display: flex;
    gap: 8px;
  }

  /* number input width */
  .afi-input.small-num { width: 100px; }

  /* two-col layout for checkbox fields */
  .afi-field-row {
    display: flex;
    gap: 24px;
  }
  .afi-field-row .afi-field { flex: 1; }
</style>

<div id="autofiling-admin">

  <h1>Autofiling Rule Manager</h1>
  <p class="subtitle">Configure rules to automatically file documents from inbox folders to metadata-driven destination paths.</p>

  <div id="afi-msg-global" class="afi-msg"></div>

  <!-- ══════════════════════════════════════════════════════
       LIST VIEW
       ══════════════════════════════════════════════════════ -->
  <div id="afi-list-view">

    <#if error?has_content>
      <div class="afi-msg error" style="display:block">${error?html}</div>
    </#if>

    <div class="afi-toolbar">
      <span id="afi-rule-count" style="color:#666;font-size:12px;"></span>
      <button class="afi-btn primary" onclick="afiShowCreateForm()">+ Create New Rule</button>
    </div>

    <div id="afi-list-container"></div>

  </div>

  <!-- ══════════════════════════════════════════════════════
       EDITOR VIEW
       ══════════════════════════════════════════════════════ -->
  <div id="afi-editor-view">

    <a class="afi-back-link" onclick="afiShowListView()">&#8592; Back to rules</a>
    <div id="afi-form-title" class="afi-form-title">Create New Rule</div>

    <div id="afi-editor-msg" class="afi-msg"></div>

    <form class="afi-form" onsubmit="return false;">

      <div class="afi-field-row">
        <div class="afi-field">
          <label>Name <span class="required">*</span></label>
          <input type="text" id="afi-f-name" class="afi-input" placeholder="e.g. Invoice Autofiling"/>
        </div>
        <div class="afi-field" style="flex:0 0 120px;">
          <label>Priority <span class="hint">(lower = higher)</span></label>
          <input type="number" id="afi-f-priority" class="afi-input small-num" value="10" min="1" max="999"/>
        </div>
      </div>

      <div class="afi-field">
        <label>Description</label>
        <input type="text" id="afi-f-description" class="afi-input" placeholder="Optional description"/>
      </div>

      <div class="afi-field">
        <label>Inbox Path <span class="required">*</span> <span class="hint">Absolute path to the folder to monitor</span></label>
        <input type="text" id="afi-f-inboxPath" class="afi-input" placeholder="/Company Home/Inbox"/>
      </div>

      <div class="afi-field">
        <label>
          Path Template <span class="required">*</span>
          <span class="hint">Destination path with {prefix:localName} tokens</span>
        </label>
        <div class="afi-template-row">
          <textarea id="afi-f-pathTemplate" class="afi-textarea"
                    placeholder="/Company Home/Finance/{cm:created|yyyy}/{cm:created|MM}/{acme:vendorName|default:Unknown}"></textarea>
          <div class="afi-token-helper">
            <div class="afi-token-header" onclick="afiToggleTokenHelper(this)">
              Token Helper <span id="afi-token-toggle">&#9660;</span>
            </div>
            <div class="afi-token-body" id="afi-token-body">
              <table>
                <tr>
                  <th>Token</th><th>Description</th>
                </tr>
                <tr><td><code>{cm:name}</code></td><td>Property value as text</td></tr>
                <tr><td><code>{cm:created|yyyy/MM}</code></td><td>Date with format — slashes create sub-folders</td></tr>
                <tr><td><code>{acme:vendor|default:Unknown}</code></td><td>Fallback when property is null/blank</td></tr>
              </table>
              <div class="section-label">Common cm: tokens</div>
              <button type="button" class="afi-token-btn" onclick="afiInsertToken('{cm:name}')">cm:name</button>
              <button type="button" class="afi-token-btn" onclick="afiInsertToken('{cm:title}')">cm:title</button>
              <button type="button" class="afi-token-btn" onclick="afiInsertToken('{cm:created|yyyy}')">cm:created year</button>
              <button type="button" class="afi-token-btn" onclick="afiInsertToken('{cm:created|MM}')">cm:created month</button>
              <button type="button" class="afi-token-btn" onclick="afiInsertToken('{cm:created|yyyy/MM}')">cm:created yyyy/MM</button>
              <button type="button" class="afi-token-btn" onclick="afiInsertToken('{cm:modified|yyyy/MM}')">cm:modified yyyy/MM</button>
              <button type="button" class="afi-token-btn" onclick="afiInsertToken('{cm:creator}')">cm:creator</button>
              <button type="button" class="afi-token-btn" onclick="afiInsertToken('{cm:modifier}')">cm:modifier</button>
            </div>
          </div>
        </div>
      </div>

      <div class="afi-field">
        <label>Content Types <span class="hint">Prefixed QNames to match — leave empty to match all content</span></label>
        <div class="afi-tag-input-wrap" id="afi-tag-wrap" onclick="document.getElementById('afi-tag-input').focus()">
          <input type="text" id="afi-tag-input" class="afi-tag-input"
                 placeholder="e.g. acme:invoice — press Enter or comma to add"/>
        </div>
        <div id="afi-tag-data"></div>
      </div>

      <div class="afi-field-row">
        <div class="afi-field">
          <div class="afi-checkbox-row">
            <input type="checkbox" id="afi-f-enabled" checked/>
            <label for="afi-f-enabled">Enabled</label>
          </div>
        </div>
        <div class="afi-field">
          <div class="afi-checkbox-row">
            <input type="checkbox" id="afi-f-createMissingFolders" checked/>
            <label for="afi-f-createMissingFolders">Create missing destination folders automatically</label>
          </div>
        </div>
      </div>

      <div class="afi-form-actions">
        <button type="button" class="afi-btn primary" onclick="afiSaveRule()">Save Rule</button>
        <button type="button" id="afi-run-btn" class="afi-btn success" onclick="afiRunCurrentRule()" style="display:none">Run Now</button>
        <button type="button" class="afi-btn" onclick="afiShowListView()">Cancel</button>
      </div>

    </form>
  </div><!-- /editor view -->

</div><!-- /autofiling-admin -->

<script>
(function () {

  // ── state ─────────────────────────────────────────────────────────────
  var _rules        = ${rulesJson!"[]"};
  var _editNodeRef  = null;   // null = create mode
  var _contentTypes = [];     // current tag list in editor
  var _tokenHidden  = false;

  // ── proxy base ────────────────────────────────────────────────────────
  var BASE = "/share/proxy/alfresco/hyland/autofiling/rules";

  function nodeRefToPath(nr) {
    return nr.replace("://", "/");
  }

  // ── XHR helper ────────────────────────────────────────────────────────
  function apiCall(method, url, body, cb) {
    var xhr = new XMLHttpRequest();
    xhr.open(method, url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.setRequestHeader("Accept", "application/json");
    xhr.onreadystatechange = function () {
      if (xhr.readyState !== 4) return;
      var ok = xhr.status >= 200 && xhr.status < 300;
      var data = null;
      try { data = JSON.parse(xhr.responseText); } catch (e) { data = xhr.responseText; }
      cb(ok, data, xhr.status);
    };
    xhr.send(body ? JSON.stringify(body) : null);
  }

  // ── view switching ────────────────────────────────────────────────────
  function afiShowListView() {
    document.getElementById("afi-list-view").style.display = "";
    document.getElementById("afi-editor-view").style.display = "none";
    clearMsg("afi-editor-msg");
    clearMsg("afi-msg-global");
    loadRules();
  }
  window.afiShowListView = afiShowListView;

  function afiShowCreateForm() {
    _editNodeRef  = null;
    _contentTypes = [];
    document.getElementById("afi-form-title").textContent = "Create New Rule";
    document.getElementById("afi-run-btn").style.display = "none";
    resetForm();
    document.getElementById("afi-list-view").style.display = "none";
    document.getElementById("afi-editor-view").style.display = "block";
    clearMsg("afi-editor-msg");
  }
  window.afiShowCreateForm = afiShowCreateForm;

  // ── list ──────────────────────────────────────────────────────────────
  function loadRules() {
    apiCall("GET", BASE, null, function (ok, data) {
      if (ok) {
        _rules = Array.isArray(data) ? data : [];
      }
      renderList();
    });
  }

  function renderList() {
    var container = document.getElementById("afi-list-container");
    var count     = document.getElementById("afi-rule-count");

    if (!_rules || _rules.length === 0) {
      count.textContent = "No rules configured";
      container.innerHTML =
        '<div class="afi-empty">No autofiling rules found. Click <strong>Create New Rule</strong> to get started.</div>';
      return;
    }

    count.textContent = _rules.length + " rule" + (_rules.length === 1 ? "" : "s");

    var rows = _rules.map(function (r) {
      var types = (r.contentTypes && r.contentTypes.length > 0)
        ? r.contentTypes.map(esc).join(", ")
        : '<em style="color:#aaa">any</em>';
      var badge = r.enabled
        ? '<span class="badge enabled">Enabled</span>'
        : '<span class="badge disabled">Disabled</span>';
      return '<tr>' +
        '<td><strong>' + esc(r.name) + '</strong></td>' +
        '<td>' + esc(r.description || "") + '</td>' +
        '<td>' + badge + '</td>' +
        '<td>' + types + '</td>' +
        '<td><code style="font-size:11px">' + esc(r.inboxPath || "") + '</code></td>' +
        '<td style="text-align:center">' + esc(String(r.priority)) + '</td>' +
        '<td class="actions">' +
          '<button class="afi-btn small" onclick="afiEditRule(' + esc(JSON.stringify(r.nodeRef)) + ')">Edit</button>' +
          '<button class="afi-btn small success" onclick="afiRunRule(' + esc(JSON.stringify(r.nodeRef)) + ', ' + esc(JSON.stringify(r.name)) + ')">Run Now</button>' +
          '<button class="afi-btn small danger" onclick="afiDeleteRule(' + esc(JSON.stringify(r.nodeRef)) + ', ' + esc(JSON.stringify(r.name)) + ')">Delete</button>' +
        '</td>' +
        '</tr>';
    }).join("");

    container.innerHTML =
      '<table class="afi-table">' +
        '<thead><tr>' +
          '<th>Name</th><th>Description</th><th>Status</th>' +
          '<th>Content Types</th><th>Inbox Path</th><th>Priority</th><th>Actions</th>' +
        '</tr></thead>' +
        '<tbody>' + rows + '</tbody>' +
      '</table>';
  }

  // ── edit ──────────────────────────────────────────────────────────────
  function afiEditRule(nodeRef) {
    // Find rule in cache first; if not found fetch from server
    var rule = null;
    for (var i = 0; i < _rules.length; i++) {
      if (_rules[i].nodeRef === nodeRef) { rule = _rules[i]; break; }
    }
    if (rule) {
      openEditor(rule);
    } else {
      apiCall("GET", BASE + "/" + nodeRefToPath(nodeRef), null, function (ok, data) {
        if (ok) openEditor(data);
        else showGlobalError("Failed to load rule");
      });
    }
  }
  window.afiEditRule = afiEditRule;

  function openEditor(rule) {
    _editNodeRef  = rule.nodeRef;
    _contentTypes = rule.contentTypes ? rule.contentTypes.slice() : [];

    document.getElementById("afi-form-title").textContent = "Edit Rule: " + rule.name;
    document.getElementById("afi-run-btn").style.display = "";
    document.getElementById("afi-run-btn").dataset.nodeRef = rule.nodeRef;
    document.getElementById("afi-run-btn").dataset.name   = rule.name;

    document.getElementById("afi-f-name").value               = rule.name || "";
    document.getElementById("afi-f-description").value        = rule.description || "";
    document.getElementById("afi-f-enabled").checked          = rule.enabled !== false;
    document.getElementById("afi-f-inboxPath").value          = rule.inboxPath || "";
    document.getElementById("afi-f-pathTemplate").value       = rule.pathTemplate || "";
    document.getElementById("afi-f-createMissingFolders").checked = rule.createMissingFolders !== false;
    document.getElementById("afi-f-priority").value           = rule.priority != null ? rule.priority : 10;

    renderTags();

    document.getElementById("afi-list-view").style.display   = "none";
    document.getElementById("afi-editor-view").style.display = "block";
    clearMsg("afi-editor-msg");
  }

  // ── save ──────────────────────────────────────────────────────────────
  function afiSaveRule() {
    var name        = trim(document.getElementById("afi-f-name").value);
    var inboxPath   = trim(document.getElementById("afi-f-inboxPath").value);
    var pathTemplate = trim(document.getElementById("afi-f-pathTemplate").value);

    if (!name)         { showEditorError("Name is required."); return; }
    if (!inboxPath)    { showEditorError("Inbox Path is required."); return; }
    if (!pathTemplate) { showEditorError("Path Template is required."); return; }

    flushTagInput();

    var payload = {
      name:                name,
      description:         trim(document.getElementById("afi-f-description").value),
      enabled:             document.getElementById("afi-f-enabled").checked,
      inboxPath:           inboxPath,
      pathTemplate:        pathTemplate,
      contentTypes:        _contentTypes.slice(),
      createMissingFolders: document.getElementById("afi-f-createMissingFolders").checked,
      priority:            parseInt(document.getElementById("afi-f-priority").value, 10) || 10
    };

    var isCreate = (_editNodeRef === null);
    var method   = isCreate ? "POST" : "PUT";
    var url      = isCreate ? BASE : (BASE + "/" + nodeRefToPath(_editNodeRef));

    apiCall(method, url, payload, function (ok, data, status) {
      if (ok) {
        afiShowListView();
        showGlobalSuccess(isCreate ? "Rule created successfully." : "Rule updated successfully.");
      } else {
        var msg = (data && data.message) ? data.message : ("Save failed (HTTP " + status + ")");
        showEditorError(msg);
      }
    });
  }
  window.afiSaveRule = afiSaveRule;

  // ── delete ────────────────────────────────────────────────────────────
  function afiDeleteRule(nodeRef, name) {
    if (!confirm('Delete rule "' + name + '"? This cannot be undone.')) return;
    apiCall("DELETE", BASE + "/" + nodeRefToPath(nodeRef), null, function (ok, data, status) {
      if (ok) {
        showGlobalSuccess('Rule "' + name + '" deleted.');
        loadRules();
      } else {
        showGlobalError("Delete failed (HTTP " + status + ")");
      }
    });
  }
  window.afiDeleteRule = afiDeleteRule;

  // ── run ───────────────────────────────────────────────────────────────
  function afiRunRule(nodeRef, name) {
    apiCall("POST", BASE + "/" + nodeRefToPath(nodeRef) + "/run", null, function (ok, data, status) {
      if (ok) {
        showGlobalSuccess('Rule "' + name + '" executed. Check repository for moved documents.');
      } else {
        showGlobalError('Run failed for "' + name + '" (HTTP ' + status + ')');
      }
    });
  }
  window.afiRunRule = afiRunRule;

  function afiRunCurrentRule() {
    var btn = document.getElementById("afi-run-btn");
    afiRunRule(btn.dataset.nodeRef, btn.dataset.name);
  }
  window.afiRunCurrentRule = afiRunCurrentRule;

  // ── content type tags ─────────────────────────────────────────────────
  document.addEventListener("DOMContentLoaded", function () {
    var input = document.getElementById("afi-tag-input");
    input.addEventListener("keydown", function (e) {
      if (e.key === "Enter" || e.key === ",") {
        e.preventDefault();
        addTagFromInput();
      } else if (e.key === "Backspace" && input.value === "" && _contentTypes.length > 0) {
        removeTag(_contentTypes.length - 1);
      }
    });
    input.addEventListener("blur", addTagFromInput);
    renderList();
  });

  function addTagFromInput() {
    var input = document.getElementById("afi-tag-input");
    var val   = input.value.replace(/,/g, "").trim();
    if (val && _contentTypes.indexOf(val) === -1) {
      _contentTypes.push(val);
      renderTags();
    }
    input.value = "";
  }

  function flushTagInput() {
    addTagFromInput();
  }

  function removeTag(idx) {
    _contentTypes.splice(idx, 1);
    renderTags();
  }
  window.afiRemoveTag = removeTag;

  function renderTags() {
    var wrap = document.getElementById("afi-tag-wrap");
    var existingTags = wrap.querySelectorAll(".afi-tag");
    existingTags.forEach(function (t) { wrap.removeChild(t); });

    _contentTypes.forEach(function (type, i) {
      var tag = document.createElement("span");
      tag.className = "afi-tag";
      tag.innerHTML = esc(type) +
        '<button type="button" class="remove" onclick="afiRemoveTag(' + i + ')">&#x2715;</button>';
      wrap.insertBefore(tag, document.getElementById("afi-tag-input"));
    });
  }

  // ── token helper ──────────────────────────────────────────────────────
  function afiToggleTokenHelper(header) {
    var body = document.getElementById("afi-token-body");
    var icon = document.getElementById("afi-token-toggle");
    _tokenHidden = !_tokenHidden;
    body.style.display = _tokenHidden ? "none" : "";
    icon.innerHTML     = _tokenHidden ? "&#9658;" : "&#9660;";
  }
  window.afiToggleTokenHelper = afiToggleTokenHelper;

  function afiInsertToken(token) {
    var ta  = document.getElementById("afi-f-pathTemplate");
    var s   = ta.selectionStart;
    var e   = ta.selectionEnd;
    var val = ta.value;
    ta.value = val.substring(0, s) + token + val.substring(e);
    ta.selectionStart = ta.selectionEnd = s + token.length;
    ta.focus();
  }
  window.afiInsertToken = afiInsertToken;

  // ── helpers ───────────────────────────────────────────────────────────
  function resetForm() {
    document.getElementById("afi-f-name").value               = "";
    document.getElementById("afi-f-description").value        = "";
    document.getElementById("afi-f-enabled").checked          = true;
    document.getElementById("afi-f-inboxPath").value          = "";
    document.getElementById("afi-f-pathTemplate").value       = "";
    document.getElementById("afi-f-createMissingFolders").checked = true;
    document.getElementById("afi-f-priority").value           = "10";
    _contentTypes = [];
    renderTags();
  }

  function esc(str) {
    if (str == null) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function trim(s) { return (s || "").replace(/^\s+|\s+$/g, ""); }

  function showGlobalError(msg) {
    var el = document.getElementById("afi-msg-global");
    el.className = "afi-msg error";
    el.textContent = msg;
    el.style.display = "";
  }

  function showGlobalSuccess(msg) {
    var el = document.getElementById("afi-msg-global");
    el.className = "afi-msg success";
    el.textContent = msg;
    el.style.display = "";
  }

  function showEditorError(msg) {
    var el = document.getElementById("afi-editor-msg");
    el.className = "afi-msg error";
    el.textContent = msg;
    el.style.display = "";
  }

  function clearMsg(id) {
    var el = document.getElementById(id);
    el.style.display = "none";
    el.textContent = "";
  }

}());
</script>
