package net.officefloor.tutorial.testhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.HttpClientRule;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.test.OfficeFloorRule;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link TemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateLogicJUnit4Test {

	// START SNIPPET: system
	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule(this);

	@Test
	public void systemTest() throws Exception {

		// Send request to add
		MockHttpResponse response = this.server
				.sendFollowRedirect(MockHttpServer.mockRequest("/template+add?a=1&b=2").method(HttpMethod.POST));
		assertEquals("Should be successful", 200, response.getStatus().getStatusCode());

		// Ensure added the values
		String entity = response.getEntity(null);
		assertTrue("Should have added the values", entity.contains("= 3"));
	}
	// END SNIPPET: system

	// START SNIPPET: full-system
	@Rule
	public final OfficeFloorRule officeFloor = new OfficeFloorRule(this);

	@Rule
	public final HttpClientRule client = new HttpClientRule();

	@Test
	public void callingSystemTest() throws Exception {

		// Send request to add
		HttpResponse response = this.client.execute(new HttpPost(this.client.url("/template+add?a=1&b=2")));
		assertEquals("Should be successful", 200, response.getStatusLine().getStatusCode());

		// Ensure added the values
		String entity = EntityUtils.toString(response.getEntity());
		assertTrue("Should have added the values: " + entity, entity.contains("= 3"));
	}
	// END SNIPPET: full-system

	// START SNIPPET: inject-dependency
	private @Dependency Calculator calculator;

	@Test
	public void injectDependency() {
		int result = calculator.plus(1, 2);
		assertEquals("Should calculate correct result", 3, result);
	}
	// END SNIPPET: inject-dependency

}