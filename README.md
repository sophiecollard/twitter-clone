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

| Component                  | Implemented | Tested |
|----------------------------|-------------|--------|
| TweetRepository (local)    | yes         | yes    |
| TweetRepository (SQL DB)   | yes         | no     | 
| TweetService               | yes         | no     |
| TweetEndpoints             | yes         | no     |
| CommentRepository (local)  | yes         | yes    |
| CommentRepository (SQL DB) | no          | no     |
| CommentService             | no          | no     |
| CommentEndpoints           | no          | no     |
