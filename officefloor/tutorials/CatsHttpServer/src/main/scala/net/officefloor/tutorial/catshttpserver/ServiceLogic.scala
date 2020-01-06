package net.officefloor.tutorial.catshttpserver

import doobie.implicits._
import cats.effect.{IO, Resource}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import net.officefloor.plugin.section.clazz.Parameter
import net.officefloor.web.ObjectResponse

/**
 * Service logic.
 */
class ServiceLogic {

  def service(request: CatsRequest, xa: Transactor[IO]): IO[Message] =
      MessageRepository.findById(request.id).transact(xa)

  def send(@Parameter message: Message, response: ObjectResponse[CatsResponse]): Unit =
    response.send(new CatsResponse(message.content))
}