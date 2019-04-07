package net.officefloor.tutorial.scalahttpserver

import net.officefloor.web.ObjectResponse

object ScalaService {

  def service(request: ScalaRequest, response: ObjectResponse[ScalaResponse]) {
    response.send(new ScalaResponse(s"Hello ${request.message}"))
  }

}