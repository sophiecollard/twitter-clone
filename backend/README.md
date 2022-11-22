# Twitter clone backend

Simple Twitter clone to demo building REST APIs with Scala 2.13 and the [Typelevel](https://typelevel.org/) stack.

## Contents

  * [Usage](#usage)
  * [Summer 2002 mob programming sessions](#summer-2022-mob-programming-sessions)
  * [Autumn 2022 mob programming sessions](#autumn-2022-mob-programming-sessions)

## Usage

### Running the test suite

Run the unit tests with:

```
sbt test
```

Run the integration tests with:

```
sbt it:test
```

Note that the latter require access to a [PostgreSQL instance](#postgresql-container).

### Starting the application

Start the server on `localhost:8080` (or any other port number [configured via the SERVER_PORT env var](#configuration))
with:

```
sbt run
```

Use the provided [Postman collection](postman/TwitterCloneV1.postman_collection.json) to interact with the server.


### Configuration

The application can be configured via the following environment variables:

| name              | type   | possible values               | default        |
|-------------------|--------|-------------------------------|----------------|
| `ENVIRONMENT`     | string | `local`, `prod`               | `local`        |
| `SERVER_HOST`     | string | Any valid host                | `0.0.0.0`      |
| `SERVER_PORT`     | int    | Any valid port number         | 8080           |
| `ALLOWED_ORIGINS` | string | Comma-separated list of hosts | `[localhost]`  |

The environment can be set to either `local` or `prod`.

When set to `local`, the application will use an in-memory database. This is great for running a single instance of the
application on a laptop during development, but can't be used in production because:
* The data is not persisted to disk and is lost when the application is stopped.
* The application can't be scaled beyond a single instance since there is no mechanism in place for sharing data between multiple instances.

If the environment is set to `prod` instead, the application will attempt to connect to a [PostgreSQL instance](#postgresql-container).

### PostgreSQL container

To run the integration tests or to start the server with `ENVIRONMENT` set to `prod`, you must provide a PostgreSQL
instance for the application to use.

Start by setting the following env vars:

```sh
export POSTGRES_DB="postgres"
export POSTGRES_USER="postgres"
export POSTGRES_PASSWORD="<change-me-please>"
export POSTGRES_PORT=5432
```

Then, start a PostgreSQL instance in a Docker container with:

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

## Summer 2022 mob programming sessions

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

Held on August 4th. See resulting [PR](https://github.com/sophiecollard/twitter-clone/pull/7).

#### Instructions: Part 1

Try to compile the project with `sbt compile`. You should get a compilation error with a message about a missing
implicit instance of `EntityDecoder[F, NewUserRequestBody]` in the `UserEndpoints` object.

Take a look at the `org.http4s.EntityDecoder[F[_], T]` trait and try to get a sense for what it does.

We've defined a new endpoint for creating users in `UserEndpoints`, which accepts POST requests with a payload that we
attempt to decode to an instance of the `NewUserRequestBody` case class. But how exactly should the raw JSON in a
request payload be decoded into an instance of that case class? Well, that's what's missing from our code, hence the
compiler error.

Provide a companion object for the `NewUserRequestBody` case class, and in it define an implicit instance of the
`Decoder` typeclass from the [Circe library](https://circe.github.io/circe/):

```scala
import io.circe.Decoder

object NewUserRequestBody {
  implicit val decoder: Decoder[NewUserRequestBody] =
    Decoder.instance { hCursor =>
      ???
    }
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

Are we really going to manually define instances of the `Decoder` typeclass for every class we may wish to serialize to
JSON?

No! We'd much rather leave tedious work like this to the compiler. And it turns out the Scala compiler (with the help of
some clever libraries such as [Shapeless](https://github.com/milessabin/shapeless) or
[Magnolia](https://github.com/softwaremill/magnolia)) is pretty good at this, but it needs a few building blocks to get
started.

Replace our previous implementation of a `Decoder` instance for `NewUserRequestBody` with the following:

```scala
import io.circe.Decoder
import io.circe.generic.semiauto

object NewUserRequestBody {
  implicit val decoder: Decoder[NewUserRequestBody] =
    semiauto.deriveDecoder
}
```

Try to compile the project again. At this point, the error message the compiler will provide you won't help much. But
you should know that in order to derive an instance of `Decoder[NewUserRequestBody]`, the compiler must have access to
instances of the `Decoder` typeclass for the type of every attribute in `NewUserRequestBody`.

Try and add implicit `Decoder` instances for the `Handle` and `Name` case classes:

```scala
implicit val handleDecoder: Decoder[Handle] =
  ???

implicit val nameDecoder: Decoder[Name] =
  ???
```

**Hint:** For simple case classes such as `Handle` and `Name` that wrap around a single value of type `String` or some
other primitive, a `Decoder` instance can be constructed starting from the `Decoder[String]` (or `Decoder[Int]`,
`Decoder[Boolean]`, etc) instance provided by Circe and methods such as `map`, `emap` or `emapTry`.

Verify that the code now compiles without any error.

If you're curious about how the Scala compiler is able to derive instances of `SomeTypeclass[A]` for a case class `A`
given instances of `SomeTypeclass` for the type of every attribute of `A`,
[this book](https://underscore.io/books/shapeless-guide/) provides an excellent introduction to automatic typeclass
derivation using [the Shapeless library](https://github.com/milessabin/shapeless).

## Autumn 2022 mob programming sessions

### Nov 17: Tapir

Defined a v2 API using [tapir](https://tapir.softwaremill.com/en/latest/index.html).
