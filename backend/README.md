# Twitter clone backend

Simple Twitter clone to demo building HTTP APIs with Scala 2.13 and the [Typelevel](https://typelevel.org/) stack.

## Usage

Run tests with:

```
sbt test
```

Start the server on `localhost:8080` with:

```
sbt run
```

Use the provided [Postman collection](postman/TwitterClone.postman_collection.json) to interact with the server.

## Mob programming sessions

### Session 1: Services

Implement the `CommentService`. You can use the `TweetService` as an example of how to implement a service and rely on
the `CommentServiceSpec` to verify the correctness of your implementation.

Held on July 7th. See resulting [PR](https://github.com/sophiecollard/twitter-clone/pull/1).

### Session 2: Endpoints

Implement the `CommentEndpoints`. You can use the `TweetEndpoints` as an example of how to implement endpoints and rely 
on the `CommentEndpointsSpec` to verify the correctness of your implementation.

Once you are done, connect your endpoints to the `Server` router. The routes of your `CommentEndpoints` should all be
prefixed with `/v1/comments`. Make sure you address any compilation errors resulting from changes made to `Server`.

Start the server on `localhost:8080` with `sbt run` and try issuing some requests using [curl](https://curl.se/),
[requests](https://pypi.org/project/requests/), [Postman](https://www.postman.com) or another tool of your choice.

Held on July 14th. See resulting [PR](https://github.com/sophiecollard/twitter-clone/pull/2).

### Session 3: TDD

#### Part 1

In `UserServiceSpec`, implement the following test:

> The create method,
> when the specified handle does not exist,
> should create and return a new user with status 'PendingActivation'

Execute the test to verify that it fails.

Now, in `UserService`, implement the `create` method. Execute the test again to verify that is passes.

#### Part 2

Back in `UserServiceSpec`, implement the following test:

> The create method,
> when the specified handle already exists,
> should return an error

Execute the test to verify that it fails.

In `UserService`, update your implementation of the `create` method to make the test pass. You may want to make use of
the `exists` method on `UserRepository` and of the `UserHandleAlreadyExists` service error.

### Future sessions

Potential tasks for future sessions:
  * Implement PostgreSQL repositories
  * Add configuration
  * Add logging and metrics
  * Implement proper authentication
