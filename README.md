scisearch is a basic implementation of a server capable of searching arxiv.org papers by keywords and managing bookmark. The server is multi-tenant, although it was initially designed for the author's personal usage.

The projects uses scala in purely functional style. The only runtime dependency is `PostgreSQL`. Tested on scala 2.13 (Java 17) and PostgreSQL 16.

## Usage

scisearch exposes RESTFul HTTP API with search-, subscription- and bookmarks-related endpoints: see `docs/openapi.yml` OpenAPI specification for details.

## Configuration

The server should be configured with environment variables:

- `SCISEARCH_BIND` (default: `0.0.0.0`)
- `SCISEARCH_PORT` (default: 3000)
- `SCISEARCH_POSTGRESQL_HOST`
- `SCISEARCH_POSTGRESQL_PORT`
- `SCISEARCH_POSTGRESQL_DATABASE`
- `SCISEARCH_POSTGRESQL_USERNAME`
- `SCISEARCH_POSTGRESQL_PASSWORD`
- `SCISEARCH_POSTGRESQL_POOL_SIZE` (default: `4`)

scisearch uses `pbkdf2` to hash user passwords:

- `SCISEARCH_TOKEN_SALT`
- `SCISEARCH_TOKEN_SECRET`

# Build

You can either build the project manually:

```shell
sbt clean compile run
```

or use predefined docker image:

```shell
docker build --tag scisearch:1.0.0 .
# or `sbt docker:publishLocal`
docker compose up
```

## Testing

To run unit tests:

```shell
sbt test
```

There are some basic E2E tests:

```shell
sbt IntegrationTest/test
```
