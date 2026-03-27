package net.officefloor.tutorial.warhttpserver;


import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import net.officefloor.webapp.OfficeFloorWar;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests integration with WAR.
 * 
 * @author Daniel Sagenschneider
 */
public class WarHttpServerTest {

	// START SNIPPET: tutorial
	@RegisterExtension
	public final MockWoofServerExtension server = new MockWoofServerExtension().property(OfficeFloorWar.PROPERTY_WAR_PATH,
			getWarPath()); // gets location of war file

	@Test
	public void simpleServlet() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/simple"));
		response.assertResponse(200, "SIMPLE");
	}

	@Test
	public void injectServlet() {
		MockWoofResponse response = this.server.send(MockWoofServer.mockRequest("/inject"));
		response.assertResponse(200, "INJECT");
	}
	// END SNIPPET: tutorial

	private static String getWarPath() {
		File currentDir = new File(".");
		final String WAR_APP_NAME = "WarApp";
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