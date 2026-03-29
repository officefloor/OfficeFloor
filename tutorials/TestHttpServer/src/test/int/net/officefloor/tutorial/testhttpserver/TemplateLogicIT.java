package net.officefloor.tutorial.testhttpserver;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogicIT {

	// START SNIPPET: integration
	@RegisterExtension
	public HttpClientExtension client = new HttpClientExtension();

	@Test
	public void integrationTest() throws Exception {

		// Send request to add
		HttpPost request = new HttpPost("http://localhost:7878/template+add?a=1&b=2");
		HttpResponse response = this.client.execute(request);

		// Ensure added the values
		String entity = EntityUtils.toString(response.getEntity());
		assertTrue(entity.contains("= 3"), "Should have added the values: " + entity);
	}
	// END SNIPPET: integration

}