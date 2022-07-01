# Twitter Clone

Simple Twitter clone to demo how to build backend services using the [Typelevel](https://typelevel.org/) stack.

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
| TweetEndpoints              | yes         | no     |
| LocalCommentRepository      | yes         | yes    |
| PostgresCommentRepository   | no          | no     |
| CommentService              | no          | yes    |
| CommentEndpoints            | no          | no     |

## Mob programming sessions

### Session 1

Implement the `CommentService`. You can use `TweetService` as an example of how to implement a service and rely on `CommentServiceSpec` to verify the 
correctness of your implementation.

### Future sessions

Potential tasks for future sessions:
  * Implement `CommentEndpoints`
  * Implement `PostgresCommentsRespository`
  * Implement proper authentication
