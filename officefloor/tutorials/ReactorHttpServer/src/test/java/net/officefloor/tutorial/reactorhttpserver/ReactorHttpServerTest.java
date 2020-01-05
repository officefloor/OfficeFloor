package net.officefloor.tutorial.reactorhttpserver;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.HttpClientRule;
import net.officefloor.test.OfficeFloorRule;

/**
 * Tests the {@link ReactorLogic}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ReactorHttpServerTest {

	@Rule
	public OfficeFloorRule officeFloor = new OfficeFloorRule();

	@Rule
	public HttpClientRule httpClient = new HttpClientRule();

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void retreiveResult() throws Exception {
		HttpResponse response = this.httpClient.execute(new HttpGet("http://localhost:7878/reactive"));
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());
		assertEquals("Incorrect response", mapper.writeValueAsString(new ServerResponse("TEST")),
				EntityUtils.toString(response.getEntity()));
	}
}
// END SNIPPET: tutorial