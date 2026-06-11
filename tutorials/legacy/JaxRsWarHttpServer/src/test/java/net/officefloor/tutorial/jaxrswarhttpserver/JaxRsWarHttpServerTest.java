package net.officefloor.tutorial.jaxrswarhttpserver;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import net.officefloor.frame.test.FileTestSupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.tutorial.jaxrsapp.JsonRequest;
import net.officefloor.tutorial.jaxrsapp.JsonResponse;
import net.officefloor.webapp.OfficeFloorWar;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the JAX-RS HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsWarHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public static final MockWoofServerExtension server = new MockWoofServerExtension()
			.property(OfficeFloorWar.PROPERTY_WAR_PATH, getWarPath());

	@Test
	public void inject() {
		MockWoofResponse response = server.send(MockWoofServer.mockRequest("/jaxrs/inject"));
		response.assertResponse(200, "Inject Dependency");
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
		final String WAR_APP_NAME = "JaxRsApp";
		File tutorialsDir = new File(".", "../../../tutorials");
		File warAppDir = new FileTestSupport().findDirectoryRecursive(tutorialsDir, WAR_APP_NAME);
		if (warAppDir == null) {
			fail("INVALID TEST: can not find " + WAR_APP_NAME + " project directory under " + tutorialsDir.getAbsolutePath());
			return null;
		}
		for (File file : new File(warAppDir, "target").listFiles()) {
			String fileName = file.getName();
			if (fileName.startsWith(WAR_APP_NAME) && fileName.toLowerCase().endsWith(".war")) {
				return file.getAbsolutePath();
			}
		}
		fail("INVALID TEST: can not find " + WAR_APP_NAME + " war file");
		return null; // should fail
	}
}