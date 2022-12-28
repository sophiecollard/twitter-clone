package twitterclone.model.graphql

import cats.implicits._
import io.circe.{Decoder, DecodingFailure, Encoder, Json}
import io.circe.syntax._
import sangria.ast.Document
import sangria.parser.QueryParser

final case class GraphQLQuery(ast: Document)

object GraphQLQuery {

  implicit val decoder: Decoder[GraphQLQuery] =
    Decoder.instance { cursor =>
      for {
        queryString <- cursor.get[String]("query")
        queryAst <- QueryParser
          .parse(queryString)
          .toEither
          .leftMap(t => DecodingFailure(t.getMessage, cursor.downField("query").history))
      } yield GraphQLQuery(queryAst)
    }

  implicit val encoder: Encoder[GraphQLQuery] =
    Encoder.instance { query =>
      Json.obj(fields =
        "query" := query.ast.source
      )
    }

}
