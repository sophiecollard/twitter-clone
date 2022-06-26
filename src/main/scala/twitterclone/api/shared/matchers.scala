package twitterclone.api.shared

import org.http4s.dsl.impl.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import twitterclone.api.shared.instances.{idQueryParamDecoder, localDateTimeQueryParamDecoder}
import twitterclone.model.{Id, User}

import java.time.LocalDateTime

object matchers {

  object AuthorQueryParamMatcher
    extends QueryParamDecoderMatcher[Id[User]]("author")

  object PageSizeQueryQueryParamMatcher
    extends QueryParamDecoderMatcher[Int]("page_size")

  object PostedAfterOptionalQueryParamMatcher
    extends OptionalQueryParamDecoderMatcher[LocalDateTime]("posted_after")

}
