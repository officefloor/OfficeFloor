package net.officefloor.scala

import net.officefloor.web.ObjectResponse

/**
 * Services the web request for testing.
 */
object ScalaRequestService {

  def service(request: ScalaRequest, response: ObjectResponse[ScalaRequest]): Unit =
    response.send(new ScalaRequest(request.identifier + 1, "Serviced " + request.message))
}
