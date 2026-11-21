# MySQL database

[Back to the file directory](../README.md)

**Status:** authenticated access through Workbench is verified: MySQL 8.0.37 on port 3306. Application credentials and a backend connection remain pending; see `01-setup.md`. No application database, tables, or migrations have been created or verified.

## How we will plan storage

1. Identify the information the main user journey needs to retain.
2. Define tables, identifiers, required fields, and relationships.
3. Add constraints for rules the database should enforce.
4. Choose one schema-management approach and document it.
5. Prepare realistic, repeatable demo records.
6. Verify that a record created through the app survives a refresh and can be read back.

Coordinate backend migrations and any `database/` files so there is one authoritative schema definition.

## Schema record to complete

| Table | Purpose | Key fields | Relationships / constraints | Schema or migration path |
| --- | --- | --- | --- | --- |
| To be decided | Awaiting brief | Pending | Pending | Not created |

## Operational details to record

- Local database name and connection-variable names:
- Migration location and command:
- Demo-data location and load command:
- How duplicate demo records are prevented:
- Verified persistence check and result:

Document configuration examples without real credentials. Use fictional demo data.

## What to explain

Describe what each table stores, how records relate, and how identifiers and constraints preserve valid data. Explain why persistent storage is useful for the chosen workflow.
