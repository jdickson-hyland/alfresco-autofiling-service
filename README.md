# Alfresco Autofiling Service

An Alfresco Content Services (ACS) platform module that automatically files uploaded documents into a destination folder path derived from their metadata. Inbox folders are monitored on a configurable schedule and documents are moved based on flexible, metadata-driven routing rules.

---

## How it works

1. An administrator creates one or more **autofiling rules** via the REST API. Each rule defines an inbox folder to watch, the content types to match, and a path template that describes where matching documents should go.
2. A **Quartz scheduled job** runs on a configurable cron schedule (default: every minute). For each enabled rule it scans the inbox folder, evaluates each document against the content type filter, resolves the destination path by substituting property values into the template, then moves the document. Missing intermediate folders are created automatically.
3. Rules are stored as JSON content nodes under **Data Dictionary > Autofiling Rules** inside Alfresco, so they persist across restarts and can be managed without redeployment.

---

## Path template syntax

Templates are strings where `{prefix:localName}` tokens are replaced with node property values at filing time.

| Token form | Description |
|------------|-------------|
| `{cm:name}` | Plain property value as a string |
| `{cm:created\|yyyy/MM}` | Date property formatted with `SimpleDateFormat` — slashes in the pattern become folder separators |
| `{acme:vendor\|default:Unknown}` | Returns `Unknown` when the property is null or empty |

Characters that are invalid in folder names (`\ : * ? " < > |`) are stripped from property values before substitution.

---

## Example rule

The following rule files documents of type `acme:invoice` that land in `/Company Home/Inbox` into a year/month/vendor folder hierarchy under `/Company Home/Finance/Invoices`.

```json
{
  "name": "Invoice Autofiling",
  "description": "Routes invoice documents by upload date and vendor name",
  "enabled": true,
  "inboxPath": "/Company Home/Inbox",
  "contentTypes": ["acme:invoice"],
  "pathTemplate": "/Company Home/Finance/Invoices/{cm:created|yyyy}/{cm:created|MM}/{acme:vendorName|default:Unknown}",
  "createMissingFolders": true,
  "priority": 10
}
```

An invoice uploaded in June 2025 with `acme:vendorName = "Acme Corp"` would be moved to:

```text
/Company Home/Finance/Invoices/2025/06/Acme Corp/
```

If the vendor property is not set, the document is placed in:

```text
/Company Home/Finance/Invoices/2025/06/Unknown/
```

A rule with an empty `contentTypes` array matches all content types — useful as a catch-all rule with a lower priority.

---

## REST API

All endpoints require **admin** authentication.

| Method | URL | Description |
|--------|-----|-------------|
| `GET` | `/alfresco/service/hyland/autofiling/rules` | List all rules |
| `POST` | `/alfresco/service/hyland/autofiling/rules` | Create a rule |
| `GET` | `/alfresco/service/hyland/autofiling/rules/{storeType}/{storeId}/{nodeId}` | Get a rule by NodeRef |
| `PUT` | `/alfresco/service/hyland/autofiling/rules/{storeType}/{storeId}/{nodeId}` | Update a rule |
| `DELETE` | `/alfresco/service/hyland/autofiling/rules/{storeType}/{storeId}/{nodeId}` | Delete a rule |
| `POST` | `/alfresco/service/hyland/autofiling/rules/{storeType}/{storeId}/{nodeId}/run` | Trigger a rule immediately |

### Create a rule

```bash
curl -u admin:admin -X POST \
  -H "Content-Type: application/json" \
  -d @rule.json \
  http://localhost:8080/alfresco/service/hyland/autofiling/rules
```

### Trigger a rule manually

```bash
curl -u admin:admin -X POST \
  http://localhost:8080/alfresco/service/hyland/autofiling/rules/workspace/SpacesStore/<nodeId>/run
```

---

## Rule fields

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `name` | string | yes | — | Unique display name stored as the node name |
| `description` | string | no | `""` | Human-readable description |
| `enabled` | boolean | no | `true` | Set to `false` to pause the rule without deleting it |
| `inboxPath` | string | yes | — | Absolute path to the folder to monitor (must start with `/Company Home/`) |
| `contentTypes` | array of strings | no | `[]` | Prefixed QName strings to match (e.g. `"acme:invoice"`). Empty array matches all content. |
| `pathTemplate` | string | yes | — | Destination path with `{prefix:localName}` tokens |
| `createMissingFolders` | boolean | no | `true` | Automatically create intermediate folders in the destination path |
| `priority` | integer | no | `10` | Lower value = higher priority. When a document matches multiple rules, the highest-priority rule fires first within a single job run. |

---

## Configuration

The cron schedule is controlled by a property in `alfresco-global.properties`:

```properties
# Default: every minute
autofiling.schedule.cron=0 0/1 * * * ?
```

The Docker development environment overrides this to every 2 minutes. To trigger filing without waiting, use the `/run` endpoint.

---

## Running

```bash
# Full build + start (first run or after code changes)
./run.sh build_start

# Start existing containers without rebuilding
./run.sh start

# Rebuild and restart only the ACS module (fastest code iteration)
./run.sh reload_acs

# Stop all containers
./run.sh stop

# Stop and delete all persistent data
./run.sh purge
```

The environment starts at:
- ACS / REST API: http://localhost:8080/alfresco
- Share: http://localhost:8180/share

---

## Running unit tests

```bash
mvn test -pl alfresco-autofiling-service-platform
```

41 unit tests cover path template resolution, folder path navigation and creation, JSON rule serialisation, content type matching, document filing, and the scheduled job orchestration logic.

---

---

# Alfresco AIO Project - SDK 4.10

This is an All-In-One (AIO) project for Alfresco SDK 4.10.

Run with `./run.sh build_start` or `./run.bat build_start` and verify that it

 * Runs Alfresco Content Service (ACS)
 * Runs Alfresco Share
 * Runs Alfresco Search Service (ASS)
 * Runs PostgreSQL database
 * Deploys the JAR assembled modules
 
All the services of the project are now run as docker containers. The run script offers the next tasks:

 * `build_start`. Build the whole project, recreate the ACS and Share docker images, start the dockerised environment composed by ACS, Share, ASS and 
 PostgreSQL and tail the logs of all the containers.
 * `build_start_it_supported`. Build the whole project including dependencies required for IT execution, recreate the ACS and Share docker images, start the 
 dockerised environment composed by ACS, Share, ASS and PostgreSQL and tail the logs of all the containers.
 * `start`. Start the dockerised environment without building the project and tail the logs of all the containers.
 * `stop`. Stop the dockerised environment.
 * `purge`. Stop the dockerised container and delete all the persistent data (docker volumes).
 * `tail`. Tail the logs of all the containers.
 * `reload_share`. Build the Share module, recreate the Share docker image and restart the Share container.
 * `reload_acs`. Build the ACS module, recreate the ACS docker image and restart the ACS container.
 * `build_test`. Build the whole project, recreate the ACS and Share docker images, start the dockerised environment, execute the integration tests from the
 `integration-tests` module and stop the environment.
 * `test`. Execute the integration tests (the environment must be already started).

# Few things to notice

 * No parent pom
 * No WAR projects, the jars are included in the custom docker images
 * No runner project - the Alfresco environment is now managed through [Docker](https://www.docker.com/)
 * Standard JAR packaging and layout
 * Works seamlessly with Eclipse and IntelliJ IDEA
 * JRebel for hot reloading, JRebel maven plugin for generating rebel.xml [JRebel integration documentation]
 * AMP as an assembly
 * Persistent test data through restart thanks to the use of Docker volumes for ACS, ASS and database data
 * Integration tests module to execute tests against the final environment (dockerised)
 * Resources loaded from META-INF
 * Web Fragment (this includes a sample servlet configured via web fragment)

# TODO

  * Abstract assembly into a dependency so we don't have to ship the assembly in the archetype
  * Functional/remote unit tests
