/*-
 * #%L
 * ZIO Tutorial
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.tutorial.ziohttpserver

import net.officefloor.scalatest.WoofRules
import org.scalatest.FlatSpec

/**
 * Tests the ZIO HTTP Server.
 */
// START SNIPPET: tutorial
class ZioHttpServerTest extends FlatSpec with WoofRules {

  it should "get message" in {
    withMockWoofServer { server =>
      val request = mockRequest("/")
        .method(httpMethod("POST"))
        .header("Content-Type", "application/json")
        .entity(jsonEntity(new ZioRequest(1)))
      val response = server.send(request)
      response.assertResponse(200, jsonEntity(new ZioResponse("Hi via ZIO")))
    }
  }

}
// END SNIPPET: tutorial
