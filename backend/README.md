# Twitter clone backend

Simple Twitter clone to demo building HTTP APIs with Scala 2.13 and the [Typelevel](https://typelevel.org/) stack.

## Contents

  * [Usage](#usage)
  * [Mob-programming sessions](#mob-programming-sessions)

## Usage

### Basic configuration

In order to start the server, the following environment variables must be set:

```sh
export ENVIRONMENT="local"   # or "prod"
export SERVER_HOST="0.0.0.0"
export SERVER_PORT=8080      # or any other available port number
export ALLOWED_ORIGINS="[http://localhost:8000]"
```

When `ENVIRONMENT` is set to `"local"`, the application uses in-memory repositories. These are great for unit testing
and for running a single instance of the application on a laptop, but can't be used in production because:
  * The data is not persisted to disk and disappears when the application is stopped.
  * The application can't be scaled beyond a single instance since there is no mechanism for sharing data between multiple instances.

Instead, set `ENVIRONMENT` to `"prod"` to use repositories that rely on a PostgresSQL database for storage.

### PostgreSQL container

To run the full test suite, or to start the server with `ENVIRONMENT` set to `"prod"`, set the following environment
variables:

```sh
export POSTGRES_DB="postgres"
export POSTGRES_USER="postgres"
export POSTGRES_PASSWORD="some-random-password" # obviously replace this for deployments
export POSTGRES_PORT=5432
```

Then, start the PostgreSQL container with:

```sh
docker run --name postgres-db \
-e POSTGRES_DB=$POSTGRES_DB \
-e POSTGRES_USER=$POSTGRES_USER \
-e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
-p $POSTGRES_PORT:5432 \
-d postgres:14.4
```

At then end of a work session, stop and remove the container with:

```sh
docker stop postgres-db && docker rm postgres-db
```

### Running the tests:

Run the full test suite with:

```
sbt test
```

### Starting the server:

Start the server on `localhost:8080` (or whatever port number was [configured via env vars](#basic-configuration)) with:

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

### Session 3: Test-driven Development

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

Held on July 28th. See resulting [PR](https://github.com/sophiecollard/twitter-clone/pull/6).

#### Instructions: Part 1

Following the instructions in this README, [configure environment variables](#basic-configuration) and start the
[PostgreSQL Docker container](#postgresql-container).

#### Instructions: Part 2

In `PostgresCommentRepositorySpec`, provide an implementation for the following test:

> The get method should get a comment

Run the test to verify that it fails.

Then, implement the `getQuery` method in `PostgresCommentRepository`. Run the test again to verify that it passes.

#### Instructions: Part 3

In `PostgresCommentRepositorySpec`, provide an implementation for the following test:

> The list method should list comments for a given tweet by decreasing 'postedOn' timestamp

Run the test to verify that it fails.

Then, implement the `listQuery` method in `PostgresCommentRepository`. Run the test again to verify that it passes.

### Session 5: Typeclasses

#### Instructions: Part 1

Try to compile the project with `sbt compile`. You should get a compilation error with a message about a missing
implicit instance of `EntityDecoder[F, NewUserRequestBody]` in the `UserEndpoints` object.

Take a look at the `org.http4s.EntityDecoder[F[_], T]` trait and try to get a sense for what it does.

We've defined a new endpoint for creating users in `UserEndpoints`, which accepts POST requests with a payload that we
attempt to decode to an instance of the `NewUserRequestBody` case class. But how exactly should the raw JSON in a
request payload be decoded into an instance of that case class? Well, that's what's missing from our code, hence the
compiler error.

Provide a companion object for the `NewUserRequestBody` case class, and in it define an implicit instance of the
`Decoder` typeclass from the Circe library:

```scala
import io.circe.Decoder

object NewUserRequestBody {
  implicit val decoder: Decoder[NewUserRequestBody] =
    ???
}
```

Then, return to `UserEndpoints` and add the following import at the top of the file:

```scala
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
```

The code should now compile. Can you work out how our implicit `Decoder[NewUserRequestBody]` instance and the
`circeEntityDecoder` import work together to provide us with an implicit instance of
`EntityDecoder[F, NewUserRequestBody]`?

#### Instructions: Part 2

TBC

### Future sessions

Potential topics for future sessions:
  * Logging and metrics
  * Authentication
