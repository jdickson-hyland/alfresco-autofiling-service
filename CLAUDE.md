# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Full build, rebuild Docker images, start all containers
./run.sh build_start

# Rebuild and redeploy only the ACS platform module (fastest iteration)
./run.sh reload_acs

# Rebuild and redeploy only the Share module (fastest iteration for UI changes)
./run.sh reload_share

# Run all unit tests (no Docker required)
mvn test -pl alfresco-autofiling-service-platform

# Run a single test class
mvn test -pl alfresco-autofiling-service-platform -Dtest=PathResolverTest

# Run a single test method
mvn test -pl alfresco-autofiling-service-platform -Dtest=PathResolverTest#plainTokenReplacedWithPropertyValue

# Compile only (catches errors without running tests)
mvn compile -pl alfresco-autofiling-service-platform

# Stop containers / wipe persistent data
./run.sh stop
./run.sh purge
```

Services when running: ACS at `http://localhost:8080/alfresco`, Share at `http://localhost:8180/share`.

## Architecture

This is an **Alfresco SDK 4.10 AIO project** (ACS 23.4.0). The only module containing custom business logic is `alfresco-autofiling-service-platform`. The share, docker, and integration-test modules are standard SDK scaffolding.

### What the service does

Watches configured Alfresco "inbox" folders on a Quartz cron schedule. For each enabled rule it scans the inbox, matches documents by content type, resolves a path template against the document's properties, then moves the document — creating any missing intermediate folders.

### Package layout (`org.hyland.com.autofiling`)

| Package | What lives there |
|---------|-----------------|
| `model` | `AutofilingModel` (QName constants), `AutofilingRule` (POJO with `fromJson`/`toJson`) |
| `service` | Core services — see below |
| `job` | `AutofilingJob` (Quartz entry point, uses `JobLockService` for cluster safety), `AutofilingJobWorker` (Spring bean holding service refs) |
| `bootstrap` | `AutofilingBootstrap` — runs once on module startup to ensure `Data Dictionary > Autofiling Rules` folder exists |
| `webscript` | Six REST web scripts for rule CRUD + manual trigger |

Core service chain:

```
AutofilingJob → AutofilingJobWorker → AutofilingServiceImpl
                                           ├── AutofilingRuleService  (loads enabled rules from Data Dictionary)
                                           ├── PathResolver           (resolves {prefix:localName} tokens)
                                           └── FolderPathService      (navigates/creates folder paths under Company Home)
```

### Rule storage

Rules are `cm:content` nodes with the `afi:autofilingRule` aspect stored under `Data Dictionary > Autofiling Rules`. The full rule spec is JSON content on the node; `afi:enabled`, `afi:inboxPath`, `afi:contentType`, and `afi:priority` are duplicated as aspect properties for fast filtering without deserialising JSON.

### Spring wiring

All beans use **XML setter injection** (no annotations). The entry point is `module-context.xml`, which imports four context files in order:

1. `bootstrap-context.xml` — registers content models (`autofilingModel.xml`, `content-model.xml`, `workflow-model.xml`)
2. `service-context.xml` — SDK sample beans (can be deleted once sample code is removed)
3. `webscript-context.xml` — SDK sample web script beans
4. `autofiling-context.xml` — all autofiling beans (services, job, web scripts)

The `autofilingBootstrap` bean declares `depends-on="alfresco-autofiling-service-platform.dictionaryBootstrap"` to ensure models are registered before the bootstrap runs.

### Web script conventions

- Bean IDs follow the pattern `webscript.<url-segments-with-dots>.<method>`, e.g. `webscript.hyland.autofiling.rule-run.post`
- Each web script has a matching `.desc.xml` descriptor under `alfresco/extension/templates/webscripts/hyland/autofiling/`
- All extend `AbstractAutofilingWebScript` and require admin authentication

### Content model

Namespace: `http://www.hyland.com/model/autofiling/1.0` (prefix `afi`)  
Defined in: `alfresco/module/alfresco-autofiling-service-platform/model/autofilingModel.xml`

### Cron schedule

Default: `0 0/1 * * * ?` (every minute), set in `alfresco/module/.../alfresco-global.properties`.  
Docker dev override (every 2 minutes) is in `alfresco-autofiling-service-platform-docker/src/main/docker/alfresco-global.properties`.

### Test conventions

Unit tests use **JUnit 4 + Mockito 4**. All privileged Alfresco service calls (`AuthenticationUtil.runAsSystem`) are bypassed in unit tests by mocking `NodeService`, `FileFolderService`, etc. directly. Test methods that call `verify(fileFolderService).move(...)` must declare `throws Exception` because `FileFolderService.move()` is a checked-exception method.

When stubbing a mock that will be passed inside a `.thenReturn()` call, create it as a local variable first — do not call `mockMethod()` inline inside `.thenReturn(...)` as Mockito detects this as unfinished stubbing.

### Share admin console module (`alfresco-autofiling-service-share`)

The Share module provides an admin console UI at `/share/page/hdp/ws/hyland/autofiling/admin` for managing autofiling rules.

**Key files:**

| File | Purpose |
| ---- | ------- |
| `alfresco/web-extension/share-config-autofiling.xml` | Registers the tool in the Share Admin Console sidebar |
| `alfresco/web-extension/alfresco-autofiling-service-share-slingshot-application-context.xml` | Spring context — overrides `webframework.configsource` to load `share-config-autofiling.xml`, and registers the message bundle |
| `alfresco/web-extension/messages/alfresco-autofiling-service-share.properties` | i18n message bundle |
| `alfresco/web-extension/site-webscripts/com/hyland/autofiling/autofiling-admin.get.{desc.xml,js,html.ftl}` | Admin console web script (descriptor, controller, template) |

**Admin console label key derivation (critical, non-obvious):**

Share's `console.js` derives the i18n message key for a tool's label automatically from the web script URL — the `label` attribute in `share-config-autofiling.xml` must be an i18n key that matches this derived pattern:

```text
key = "tool." + (webscript-path-after-namespace, with "/" replaced by "-") + ".label"
```

Example: web script at `/hyland/autofiling/admin` → strip `/hyland/` → `autofiling/admin` → replace `/` with `-` → `autofiling-admin` → key is `tool.autofiling-admin.label`.

So `share-config-autofiling.xml` must use `label="tool.autofiling-admin.label"` and the properties file must define `tool.autofiling-admin.label=Autofiling Rule Manager`. Using a literal string or a differently-named key causes Share to display the raw key string.

Reference: see working example in `/Users/james.dickson/projects/alfresco-sdk-projects/alfresco-counters/alfresco-counters-share` (the `counter-admin` tool follows the same pattern).

**Share config loading:**

The `webframework.configsource` bean in the slingshot application context must be overridden to include `share-config-autofiling.xml`. It must list ALL default Share config sources (preserved from `slingshot-application-context.xml`) plus the module config appended last. Do NOT put admin console tool registration in `META-INF/share-config-custom.xml` — use only `share-config-autofiling.xml` via the override.

**Surf JS controller / FreeMarker template:**

- In the Surf JS controller, use `result.toString()` to get the raw JSON string from a connector call — do **not** use `JSON.parse()` and then try to serialize from FreeMarker.
- In the FTL template, emit the raw JSON directly into JS with `${rulesJson!"[]"}` (not `?json_string`, which fails on Rhino objects).
- To show a hidden div (`display: none` in CSS), use `element.style.display = "block"` — not `""` (empty string removes the inline style and the CSS rule takes over again).
- API calls from the browser use the Share Surf proxy path `/share/proxy/alfresco/hyland/autofiling/...` — Share forwards these with the current user's credentials automatically.
