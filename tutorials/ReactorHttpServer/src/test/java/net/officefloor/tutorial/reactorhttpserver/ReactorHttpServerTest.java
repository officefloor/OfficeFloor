package net.officefloor.tutorial.reactorhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Tests the {@link ReactorLogic}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
@ExtendWith(OfficeFloorExtension.class)
public class ReactorHttpServerTest {

	@RegisterExtension
	public HttpClientExtension httpClient = new HttpClientExtension();

	@Test
	public void retreiveResult() throws Exception {
		HttpResponse response = this.httpClient.execute(new HttpGet("http://localhost:7878/reactive"));
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful");
		assertEquals("{\"message\":\"TEST\"}", EntityUtils.toString(response.getEntity()), "Incorrect response");
	}
}
// END SNIPPET: tutorial