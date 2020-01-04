package net.officefloor.tutorial.ziohttpserver

import zio.Task

/**
 * ZIO injection of {@link MessageRepository}.
 */
// START SNIPPET: tutorial
trait InjectMessageRepository {
  val messageRepository: MessageRepository
}
// END SNIPPET: tutorial