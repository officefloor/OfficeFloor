package net.officefloor.tutorial.catshttpserver

import cats.effect.IO
import doobie._
import doobie.implicits._

/**
 * Message repository.
 */
// START SNIPPET: tutorial
object MessageRepository {

  def findById(id: Int)(implicit xa: Transactor[IO]): IO[Message] =
    sql"SELECT id, content FROM message WHERE id = $id".query[Message].unique.transact(xa)
}
// END SNIPPET: tutorial