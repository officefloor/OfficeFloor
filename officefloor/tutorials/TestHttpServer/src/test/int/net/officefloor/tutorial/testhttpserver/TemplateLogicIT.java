package net.officefloor.tutorial.testhttpserver;

import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.server.http.HttpClientRule;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogicIT {

	// START SNIPPET: integration
	@Rule
	public HttpClientRule client = new HttpClientRule();

	@Test
	public void integrationTest() throws Exception {

		// Send request to add
		HttpPost request = new HttpPost("http://localhost:7878/template+add?a=1&b=2");
		HttpResponse response = client.execute(request);

		// Ensure added the values
		String entity = EntityUtils.toString(response.getEntity());
		assertTrue("Should have added the values: " + entity, entity.contains("= 3"));
	}
	// END SNIPPET: integration

}