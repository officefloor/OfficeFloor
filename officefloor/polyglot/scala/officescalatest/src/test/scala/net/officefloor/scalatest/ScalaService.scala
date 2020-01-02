package net.officefloor.scalatest

import net.officefloor.web.ObjectResponse

object ScalaService {

  def service(request: ScalaRequest, response: ObjectResponse[ScalaResponse]) {
    response.send(new ScalaResponse(s"REQUEST = ${request.message}"))
  }

}