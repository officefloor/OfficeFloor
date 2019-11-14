package net.officefloor.tutorial.kotlinhttpserver

import net.officefloor.woof.mock.MockWoofServerRule
import org.junit.Rule
import org.junit.Test
import net.officefloor.server.http.mock.MockHttpServer
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Tests the Kotlin HTTP server.
 */
// START SNIPPET: tutorial
class KotlinHttpServerTest {

	val mapper = ObjectMapper()

	@Rule
	@JvmField
	public val server = MockWoofServerRule()

	@Test
	fun service() {
		val response = this.server.send(
			MockHttpServer.mockRequest().header(
				"Content-Type",
				"application/json"
			).entity(mapper.writeValueAsString(KotlinRequest("Daniel")))
		);
		response.assertResponse(200, mapper.writeValueAsString(KotlinResponse("Hello Daniel from Kotlin")));
	}
}
// END SNIPPET: tutorial