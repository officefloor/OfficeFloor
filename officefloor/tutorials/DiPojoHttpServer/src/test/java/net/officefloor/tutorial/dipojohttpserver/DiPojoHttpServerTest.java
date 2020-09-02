package net.officefloor.tutorial.dipojohttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.OfficeFloorMain;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Ensure correctly renders the page.
 * 
 * @author Daniel Sagenschneider
 */
public class DiPojoHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	// START SNIPPET: test
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension();

	private @Dependency Pojo testInjectedPojo;

	@Test
	public void injectIntoTest() {
		assertEquals("World", this.testInjectedPojo.getAudience(), "Dependency inject into test");
	}

	@Test
	public void ensureRenderPage() throws Exception {

		// Obtain the page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/template"));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful");

		// Ensure page contains correct rendered content
		String page = response.getEntity(null);
		assertTrue(page.contains("Hello World"), "Ensure correct page content");
	}
	// END SNIPPET: test

	@Test
	public void ensureFieldInjection() throws Exception {
		this.ensureInjection("/field");
	}

	@Test
	public void ensureSetterInjection() throws Exception {
		this.ensureInjection("/setter");
	}

	@Test
	public void ensureConstructorInjection() throws Exception {
		this.ensureInjection("/constructor");
	}

	private void ensureInjection(String path) throws Exception {

		// Obtain the page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest(path));
		assertEquals(200, response.getStatus().getStatusCode(), "Should be successful");

		// Ensure page contains correct rendered content
		String page = response.getEntity(null);
		assertTrue(page.contains("Hello World"), "Ensure correct page content");
	}

}