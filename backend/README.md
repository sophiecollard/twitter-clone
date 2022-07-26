# Twitter clone backend

Simple Twitter clone to demo building HTTP APIs with Scala 2.13 and the [Typelevel](https://typelevel.org/) stack.

## Usage

### Environment configuration

Configure the local environment with:

```sh
export ENVIRONMENT="local"
export SERVER_HOST="0.0.0.0"
export SERVER_PORT=8080
export ALLOWED_ORIGINS="[http://localhost:8000]"
```

When `ENVIRONMENT` is set to `"prod"`, the following env vars must also be set:

```sh
export POSTGRES_DB="postgres"
export POSTGRES_USER="postgres"
export POSTGRES_PASSWORD="some-random-password"
```

### PostgreSQL container

Before starting the server when `ENVIRONMENT` is set to `"prod"`, start the PostgreSQL container with:

```sh
docker run --name postgres-db \
-e POSTGRES_DB=$POSTGRES_DB \
-e POSTGRES_USER=$POSTGRES_USER \
-e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
-p $POSTGRES_PORT:5432 \
-d postgres:14.4
```

At then end of a work session, stop the container with:

```sh
docker rm postgres-db
```

### Running the tests:

Run the tests with:

```
sbt test
```

### Running the server:

Start the server on `localhost:8080` with:

```
sbt run
```

Use the provided [Postman collection](postman/TwitterClone.postman_collection.json) to interact with the server.

## Mob programming sessions

### Session 1: Services

Held on July 7th. See resulting [PR](https://github.com/sophiecollard/twitter-clone/pull/1).

#### Instructions

Implement the `CommentService`. You can use the `TweetService` as an example of how to implement a service and rely on
the `CommentServiceSpec` to verify the correctness of your implementation.

### Session 2: Endpoints

Held on July 14th. See resulting [PR](https://github.com/sophiecollard/twitter-clone/pull/2).

#### Instructions

Implement the `CommentEndpoints`. You can use the `TweetEndpoints` as an example of how to implement endpoints and rely 
on the `CommentEndpointsSpec` to verify the correctness of your implementation.

Once you are done, connect your endpoints to the `Server` router. The routes of your `CommentEndpoints` should all be
prefixed with `/v1/comments`. Make sure you address any compilation errors resulting from changes made to `Server`.

Start the server on `localhost:8080` with `sbt run` and try issuing some requests using [curl](https://curl.se/),
[requests](https://pypi.org/project/requests/), [Postman](https://www.postman.com) or another tool of your choice.

### Session 3: TDD

Held on July 21st. See resulting [PR](https://github.com/sophiecollard/twitter-clone/pull/4).

#### Instructions: Part 1

In `UserServiceSpec`, provide an implementation for the following test:

> The create method,
> when the specified handle does not exist,
> should create and return a new user with status 'PendingActivation'

Run the test to verify that it fails.

Then, implement the `create` method in `UserService`. Run the test again to verify that is passes.

#### Instructions: Part 2

Back in `UserServiceSpec`, provide an implementation for the following test:

> The create method,
> when the specified handle already exists,
> should return an error

Run the test. Unless you thought about checking for the handle uniqueness when you first implemented the `create` method
in part 1, the test should fail.

In `UserService`, update your implementation of the `create` method to make the test pass. You may want to make use of
the `exists` method on `UserRepository` and of the `UserHandleAlreadyExists` service error.

### Session 4: PostgreSQL Repositories

TBD

### Future sessions

Potential topics for future sessions:
  * Logging and metrics
  * Authentication
