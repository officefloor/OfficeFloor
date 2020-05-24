package net.officefloor.tutorial.jaxrswarhttpserver;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.ClassRule;
import org.junit.Test;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.tutorial.jaxrsapp.JsonRequest;
import net.officefloor.tutorial.jaxrsapp.JsonResponse;
import net.officefloor.webapp.OfficeFloorWar;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the JAX-RS HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsWarHttpServerTest {

	// START SNIPPET: tutorial
	@ClassRule
	public static final MockWoofServerRule server = new MockWoofServerRule().property(OfficeFloorWar.PROPERTY_WAR_PATH,
			getWarPath());

	@Test
	public void get() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/jaxrs"));
		response.assertResponse(200, "GET");
	}

	@Test
	public void pathParam() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/jaxrs/path/parameter"));
		response.assertResponse(200, "parameter");
	}

	@Test
	public void post() {
		MockWoofResponse response = server
				.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/jaxrs/json", new JsonRequest("INPUT")));
		response.assertJson(200, new JsonResponse("INPUT"));
	}
	// END SNIPPET: tutorial

	private static String getWarPath() {
		File currentDir = new File(".");
		final String WAR_APP_NAME = "JaxRsApp";
		File warHttpServerProjectDir = new File(currentDir, "../../tutorials/" + WAR_APP_NAME);
		for (File file : new File(warHttpServerProjectDir, "target").listFiles()) {
			String fileName = file.getName();
			if (fileName.startsWith(WAR_APP_NAME) && fileName.toLowerCase().endsWith(".war")) {

				// Found WAR file
				return file.getAbsolutePath();
			}
		}
		fail("INVALID TEST: can not find " + WAR_APP_NAME + " war file");
		return null; // should fail
	}
}