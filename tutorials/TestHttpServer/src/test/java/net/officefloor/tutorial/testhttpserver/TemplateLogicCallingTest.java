package net.officefloor.tutorial.testhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpClientExtension;
import net.officefloor.test.OfficeFloorExtension;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: full-system
@ExtendWith(OfficeFloorExtension.class)
public class TemplateLogicCallingTest {

	@RegisterExtension
	public final HttpClientExtension client = new HttpClientExtension();

	@Test
	public void callingSystemTest() throws Exception {

		// Send request to add
		HttpResponse response = this.client.execute(new HttpPost(this.client.url("/template+add?a=1&b=2")));
		assertEquals(200, response.getStatusLine().getStatusCode(), "Should be successful");

		// Ensure added the values
		String entity = EntityUtils.toString(response.getEntity());
		assertTrue(entity.contains("= 3"), "Should have added the values: " + entity);
	}
	// END SNIPPET: full-system

	// START SNIPPET: inject-dependency
	@Test
	public void injectDependency(Calculator calculator) {
		int result = calculator.plus(1, 2);
		assertEquals(3, result, "Should calculate correct result");
	}
	// END SNIPPET: inject-dependency
}