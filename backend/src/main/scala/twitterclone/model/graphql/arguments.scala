package twitterclone.model.graphql

import sangria.schema.Argument

import java.util.UUID

object arguments {

  val UUIDArg: Argument[UUID] = Argument("id", types.UUIDType, description = "UUID")

}
