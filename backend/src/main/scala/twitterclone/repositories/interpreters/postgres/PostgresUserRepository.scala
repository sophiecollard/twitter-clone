package twitterclone.repositories.interpreters.postgres

import doobie.{ConnectionIO, Query0, Update, Update0}
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.refined.implicits._
import twitterclone.model.Id
import twitterclone.model.user.Handle
import twitterclone.model.user.Name.meta
import twitterclone.model.user.User
import twitterclone.repositories.domain.UserRepository

object PostgresUserRepository {

  def create: UserRepository[ConnectionIO] =
    new UserRepository[ConnectionIO] {
      override def create(user: User): ConnectionIO[Int] =
        createUpdate.run(user)

      override def delete(id: Id[User]): ConnectionIO[Int] =
        deleteUpdate(id).run

      override def get(id: Id[User]): ConnectionIO[Option[User]] =
        getQuery(id).option

      override def getByHandle(handle: Handle.Value): ConnectionIO[Option[User]] =
        getByHandleQuery(handle).option

      override def exists(handle: Handle.Value): ConnectionIO[Boolean] =
        existsQuery(handle).unique
    }

  private val createUpdate: Update[User] =
    Update(
      s"""INSERT INTO users (id, handle, name, status)
         |VALUES (?, ?, ?, ?)
         |ON CONFLICT DO NOTHING;
         |""".stripMargin
    )

  private def deleteUpdate(id: Id[User]): Update0 =
    sql"""DELETE
         |FROM users
         |WHERE id = $id
         |""".stripMargin.update

  private def getQuery(id: Id[User]): Query0[User] =
    sql"""SELECT id, handle, name, status
         |FROM users
         |WHERE id = $id
         |""".stripMargin.query[User]

  private def getByHandleQuery(handle: Handle.Value): Query0[User] =
    sql"""SELECT id, handle, name, status
         |FROM users
         |WHERE handle = $handle
         |""".stripMargin.query[User]

  private def existsQuery(handle: Handle.Value): Query0[Boolean] =
    sql"""SELECT EXISTS (
         |  SELECT country_id
         |  FROM countries_search
         |  WHERE handle = $handle
         |)
         |""".stripMargin.query[Boolean]

}
