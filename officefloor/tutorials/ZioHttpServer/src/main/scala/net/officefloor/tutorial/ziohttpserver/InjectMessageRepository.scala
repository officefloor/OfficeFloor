package net.officefloor.tutorial.ziohttpserver

import zio.Task

/**
 * ZIO injection of {@link MessageRepository}.
 */
trait InjectMessageRepository {
  val messageRepository: MessageRepository
}