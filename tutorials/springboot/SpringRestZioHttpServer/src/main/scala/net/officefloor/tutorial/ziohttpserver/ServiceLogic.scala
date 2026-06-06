package net.officefloor.tutorial.ziohttpserver

import net.officefloor.plugin.section.clazz.Parameter
import net.officefloor.web.ObjectResponse
import org.springframework.web.bind.annotation.RequestBody
import zio.{ZIO, ZLayer}

class ServiceLogic {

  // START SNIPPET: service
  def service(@RequestBody request: ZioRequest, repository: MessageRepository): ZIO[Any, Throwable, Message] = {
    val zio = for {
      m <- MessageService.getMessage(request.id)
    } yield m
    zio.provide(ZLayer.succeed(repository))
  }
  // END SNIPPET: service

  // START SNIPPET: send
  def send(@Parameter message: Message, response: ObjectResponse[ZioResponse]): Unit =
    response.send(ZioResponse(message.getContent))
  // END SNIPPET: send
}
