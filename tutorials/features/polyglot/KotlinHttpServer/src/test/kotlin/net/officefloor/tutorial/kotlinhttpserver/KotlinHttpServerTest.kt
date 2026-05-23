package net.officefloor.tutorial.kotlinhttpserver

import net.officefloor.server.http.HttpMethod
import net.officefloor.woof.mock.MockWoofServer
import net.officefloor.woof.mock.MockWoofServerExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * Tests the Kotlin HTTP server.
 */
class KotlinHttpServerTest {

    // START SNIPPET: tutorial
    @RegisterExtension
    @JvmField
    public val server = MockWoofServerExtension()

    @Test
    fun service() {
        val response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/", KotlinRequest("Daniel")));
        response.assertJson(200, KotlinResponse("Hello Daniel from Kotlin"));
    }
    // END SNIPPET: tutorial
}
