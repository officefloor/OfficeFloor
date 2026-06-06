package net.officefloor.tutorial.catshttpserver

import cats.effect.IO
import doobie.util.transactor.Transactor
import net.officefloor.plugin.section.clazz.Parameter
import net.officefloor.web.ObjectResponse
import org.springframework.web.bind.annotation.RequestBody

class ServiceLogic {

  // START SNIPPET: service
  def service(@RequestBody request: CatsRequest)(implicit xa: Transactor[IO]): IO[CatsResponse] =
    for {
      message <- MessageRepository.findById(request.id)
      response = CatsResponse(message.content + " and Cats")
    } yield response
  // END SNIPPET: service

  // START SNIPPET: send
  def send(@Parameter message: CatsResponse, response: ObjectResponse[CatsResponse]): Unit =
    response.send(message)
  // END SNIPPET: send
}
