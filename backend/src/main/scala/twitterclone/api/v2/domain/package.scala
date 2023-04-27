package twitterclone.api.v2

import sttp.tapir.{Codec, CodecFormat, EndpointInput, Mapping, query}
import twitterclone.model.{Comment, Id, Pagination, Tweet}

import java.time.LocalDateTime

package object domain {

  lazy val paginationInput: EndpointInput[Pagination] =
    (query[Option[Int]]("pageSize") and query[Option[LocalDateTime]]("postedBefore")).map(
      Mapping.from[(Option[Int], Option[LocalDateTime]), Pagination] {
        case (maybePage, postedBefore) =>
          Pagination(pageSize = maybePage.getOrElse(10), postedBefore)
      } (pagination => (Some(pagination.pageSize), pagination.postedBefore))
    )

  implicit val commentIdCodec: Codec[String, Id[Comment], CodecFormat.TextPlain] =
    Codec.uuid.map(Id.apply[Comment](_))(_.value)

  implicit val tweetIdCodec: Codec[String, Id[Tweet], CodecFormat.TextPlain] =
    Codec.uuid.map(Id.apply[Tweet](_))(_.value)

}
