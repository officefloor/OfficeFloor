package net.officefloor.tutorial.scalahttpserver

import net.officefloor.web.ObjectResponse
import org.springframework.web.bind.annotation.RequestBody

// START SNIPPET: tutorial
object ScalaService {

  def service(@RequestBody request: ScalaRequest, response: ObjectResponse[ScalaResponse]): Unit = {
    response.send(new ScalaResponse(s"Hello ${request.message}"))
  }
}
// END SNIPPET: tutorial
