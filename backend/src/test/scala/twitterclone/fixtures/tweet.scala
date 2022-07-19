package twitterclone.fixtures

import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet}

import java.time.{LocalDate, LocalDateTime, LocalTime}

object tweet {

  val tweet: Tweet = Tweet(
    id = Id.random[Tweet],
    author = Id.random[User],
    contents =
      "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 30),
      LocalTime.of(19, 30)
    )
  )

  val earlierTweetFromSameAuthor: Tweet = Tweet(
    id = Id.random[Tweet],
    author = tweet.author,
    contents = "Je dis des choses tellement intelligentes que souvent, je ne comprends pas ce que je dis.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 29),
      LocalTime.of(19, 30)
    )
  )

  val tweetFromAnotherAuthor: Tweet = Tweet(
    id = Id.random[Tweet],
    author = Id.random[User],
    contents = "S'il n'a a pas de solution, c'est qu'il n'y a pas de problème.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 1, 1),
      LocalTime.of(19, 30)
    )
  )

}
