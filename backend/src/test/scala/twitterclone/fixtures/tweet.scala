package twitterclone.fixtures

import eu.timepit.refined.auto._
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet}
import twitterclone.repositories.domain.TweetRepository.TweetData

import java.time.{LocalDate, LocalDateTime, LocalTime}

object tweet {

  val tweetData: TweetData = TweetData(
    id = Id.random[Tweet],
    authorId = Id.random[User],
    contents =
      "Mieux vaut mobiliser son intelligence sur des betises que mobiliser sa betise sur des choses intelligentes.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 30),
      LocalTime.of(19, 30)
    )
  )

  val tweet: Tweet =
    tweetData.constructTweet(0, None)

  val earlierTweetFromSameAuthorData: TweetData = TweetData(
    id = Id.random[Tweet],
    authorId = tweet.authorId,
    contents = "Je dis des choses tellement intelligentes que souvent, je ne comprends pas ce que je dis.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 4, 29),
      LocalTime.of(19, 30)
    )
  )

  val earlierTweetFromSameAuthor: Tweet =
    earlierTweetFromSameAuthorData.constructTweet(0, None)

  val tweetFromAnotherAuthorData: TweetData = TweetData(
    id = Id.random[Tweet],
    authorId = Id.random[User],
    contents = "S'il n'a a pas de solution, c'est qu'il n'y a pas de problème.",
    postedOn = LocalDateTime.of(
      LocalDate.of(1968, 1, 1),
      LocalTime.of(19, 30)
    )
  )

  val tweetFromAnotherAuthor: Tweet =
    tweetFromAnotherAuthorData.constructTweet(0, None)

}
