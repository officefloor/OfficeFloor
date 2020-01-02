package net.officefloor.tutorial.ziohttpserver

import zio.ZIO

/**
 * Message service.
 */
object MessageService {

  def getMessage(id: Int): ZIO[InjectMessageRepository, Throwable, Message] =
    ZIO.accessM(env => ZIO.effect(env.messageRepository findById id orElseThrow))

}