package net.officefloor.tutorial.ziohttpserver

import zio.{Task, ZIO}

/**
 * Message service.
 */
// START SNIPPET: tutorial
object MessageService {

  def getMessage(id: Int): ZIO[MessageRepository, Throwable, Message] =
    ZIO.serviceWithZIO[MessageRepository](repository => ZIO.attempt(repository findById id orElseThrow(() => new NoSuchElementException(s"No message by id $id"))))

}
// END SNIPPET: tutorial