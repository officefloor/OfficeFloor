package net.officefloor.tutorial.ziohttpserver

import net.officefloor.plugin.section.clazz.Parameter
import net.officefloor.web.ObjectResponse
import zio.{ZIO, ZLayer}

/**
 * Logic to service request.
 */
class ServiceLogic {

  // START SNIPPET: service
  def service(request: ZioRequest, repository: MessageRepository): ZIO[Any, Throwable, Message] = {

    // Service Logic
    val zio = for {
      m <- MessageService.getMessage(request.id)
      // possible further logic
    } yield m

    // Provide environment from dependency injection
    zio.provide(ZLayer.succeed(repository))
  }
  // END SNIPPET: service

  // START SNIPPET: send
  def send(@Parameter message: Message, response: ObjectResponse[ZioResponse]): Unit =
    response.send(new ZioResponse(message.getContent))
  // END SNIPPET: send
}