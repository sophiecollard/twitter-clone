package twitterclone.fixtures

import twitterclone.model.{Comment, Id, Tweet, User}

import java.time.{LocalDate, LocalDateTime, LocalTime}

object comment {

  val comment: Comment = Comment(
    id = Id.random[Comment],
    author = Id.random[User],
    tweetId = Id.random[Tweet],
    contents =
      "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 30),
      LocalTime.of(19, 30)
    )
  )

  val earlierCommentOnSameTweet: Comment = Comment(
    id = Id.random[Comment],
    author = Id.random[User],
    tweetId = comment.tweetId,
    contents = "Je dis des choses tellement intelligentes que souvent, je ne comprends pas ce que je dis.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 29),
      LocalTime.of(19, 30)
    )
  )

  val commentOnAnotherTweet: Comment = Comment(
    id = Id.random[Comment],
    author = comment.author,
    tweetId = Id.random[Tweet],
    contents = "S'il n'a a pas de solution, c'est qu'il n'y a pas de problème.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 1, 1),
      LocalTime.of(19, 30)
    )
  )

}
