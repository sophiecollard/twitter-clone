package twitterclone.api.v1.shared

import org.http4s.dsl.impl.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import instances.{idQueryParamDecoder, localDateTimeQueryParamDecoder}
import twitterclone.model.user.User
import twitterclone.model.{Id, Tweet}

import java.time.LocalDateTime

object matchers {

  object AuthorQueryParamMatcher
    extends QueryParamDecoderMatcher[Id[User]]("author")

  object PageSizeOptionalQueryParamMatcher
    extends OptionalQueryParamDecoderMatcher[Int]("page_size")

  object PostedBeforeOptionalQueryParamMatcher
    extends OptionalQueryParamDecoderMatcher[LocalDateTime]("posted_before")

  object TweetIdQueryParamMatcher
    extends QueryParamDecoderMatcher[Id[Tweet]]("tweet-id")

}
