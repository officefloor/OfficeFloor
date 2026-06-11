package net.officefloor.tutorial.kotlinhttpserver

import com.fasterxml.jackson.databind.ObjectMapper
import net.officefloor.server.http.HttpMethod
import net.officefloor.woof.mock.MockWoofServer
import net.officefloor.woof.mock.MockWoofServerRule
import org.junit.Rule
import org.junit.Test

/**
 * Tests the Kotlin HTTP server.
 */
class KotlinHttpServerJUnit4Test {

    // START SNIPPET: tutorial
    @Rule
    @JvmField
    public val server = MockWoofServerRule()

    @Test
    fun service() {
        val response = this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/", KotlinRequest("Daniel")));
        response.assertJson(200, KotlinResponse("Hello Daniel from Kotlin"));
    }
    // END SNIPPET: tutorial
}
