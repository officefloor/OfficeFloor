package net.officefloor.tutorial.catshttpserver

import doobie._
import doobie.implicits._

/**
 * Message repository.
 */
object MessageRepository {

  def findById(id: Int): ConnectionIO[Message] =
    sql"SELECT id, content FROM message WHERE id = $id".query[Message].unique

}