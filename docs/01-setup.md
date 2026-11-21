# Local development setup

[Back to the file directory](../README.md)

**Status:** checked on 2026-11-21. Java, Node.js/npm, Maven, the MySQL client, and Git are installed. Authenticated access through MySQL Workbench is verified: server 8.0.37 on port 3306. Application/CLI credentials and application builds remain pending. The user reports VS Code is installed.

## Tools to verify

| Tool | Purpose | Check | Status |
| --- | --- | --- | --- |
| VS Code | Editing and debugging | Open the workspace | Reported installed |
| Java JDK | Compile and run Spring Boot | `java -version` and `javac -version` | Runtime and compiler: 25.0.4 |
| Node.js and npm | Run and build React | `node --version` and `npm --version` | Node.js 22.18.0; npm 10.9.3 |
| Maven | Build Java projects | `mvn --version` | 3.9.16; runs with Java 25.0.4 |
| MySQL | Store application data | Client version plus read-only Workbench query | Homebrew client 9.6.0; authenticated server 8.0.37 on port 3306 |
| Git | Version control and collaboration | `git --version` | 2.55.0 |

A MySQL client version check does not confirm that the database server is running. Select compatible Java, Spring Boot, and frontend-tool versions when scaffolding the project; record the actual versions here.

## Verified database session and remaining application configuration

Initial CLI checks reached MySQL but returned error 1045 because no password was supplied. Subsequently, the user opened MySQL Workbench with an authenticated connection, and the assistant ran this read-only query in a new SQL tab:

```sql
SELECT VERSION() AS server_version,
       CURRENT_USER() AS authenticated_account,
       @@port AS server_port;
```

The visible result was one row: server `8.0.37`, authenticated account `root@localhost`, port `3306`. Workbench reported “Query Completed.” No application data, accounts, or server settings were changed, and no password was requested or captured.

Next, configure a dedicated application database/user and verify the Spring Boot connection when the project is scaffolded. An authenticated Workbench session does not provide credentials automatically to CLI tools or the backend. Keep any credential entry local and out of chat.

The `code` command was not found in the shell PATH; this does not mean the VS Code app is missing. Project dependency downloads and an application build have not been tested yet.

## Additional MySQL findings

- The existing server process uses `/usr/local/mysql/bin/mysqld`. Both its executable and the authenticated server report version 8.0.37.
- The Homebrew client reports 9.6.0, but its server executable cannot currently start because an Abseil library is missing. Do not restart or replace the existing server based on the Homebrew client version.
- No saved credentials were found in the standard client configuration/login-profile files checked. Clean-defaults logins for root and the local macOS username were rejected without a password.
- No passwords, accounts, databases, or running services were changed. A separate project instance was considered but not created; the user offered to enter existing credentials instead.
- Computer control denied Terminal access. Initial Workbench access attempts timed out; after the user opened it, the assistant successfully accessed the connected session and verified it with a read-only query. Passwords must stay out of chat.

## Run instructions to complete after scaffolding

1. Start MySQL and create the local application database/user.
2. Configure the backend connection using local environment variables.
3. Run database migrations and load repeatable demo data.
4. Start the backend using the generated project wrapper.
5. Install frontend dependencies using its lockfile and start the frontend.
6. Open the application and verify one request reaches the database.

Record exact commands, working directories, local URLs, ports, and required environment-variable names here after they are verified. Do not document guessed commands as working instructions.

## What to explain

The frontend and backend run as separate processes. The backend connects to MySQL. The browser accesses application data through the backend API.
