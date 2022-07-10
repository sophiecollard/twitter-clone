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

## Components

| Component                   | Implemented | Tested |
|-----------------------------|-------------|--------|
| LocalTweetRepository        | yes         | yes    |
| PostgresTweetRepository     | no          | no     | 
| TweetService                | yes         | yes    |
| TweetEndpoints              | yes         | yes    |
| LocalCommentRepository      | yes         | yes    |
| PostgresCommentRepository   | no          | no     |
| CommentService              | yes         | yes    |
| CommentEndpoints            | no          | yes    |

## Mob programming sessions

### Session 1

Implement the `CommentService`. You can use the `TweetService` as an example of how to implement a service and rely on
the `CommentServiceSpec` to verify the correctness of your implementation.

### Session 2

Implement the `CommentEndpoints`. You can use the `TweetEndpoints` as an example of how to implement endpoints and rely 
on the `CommentEndpointsSpec` to verify the correctness of your implementation.

Once you are done, connect your endpoints to the `Server` router. The routes of your `CommentEndpoints` should all be
prefixed with `/v1/comments`.

Start the server on `localhost:8080` with `sbt run` and try issuing some requests using [curl](https://curl.se/),
[requests](https://pypi.org/project/requests/), [Postman](https://www.postman.com) or another tool of your choice.

### Future sessions

Potential tasks for future sessions:
  * Implement `PostgresCommentsRespository`
  * Add configuration
  * Add logging and metrics
  * Implement proper authentication
