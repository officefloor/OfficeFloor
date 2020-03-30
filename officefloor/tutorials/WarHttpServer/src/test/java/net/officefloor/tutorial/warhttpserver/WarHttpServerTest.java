package net.officefloor.tutorial.warhttpserver;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.webapp.OfficeFloorWar;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests integration with WAR.
 * 
 * @author Daniel Sagenschneider
 */
public class WarHttpServerTest {

	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule().property(OfficeFloorWar.PROPERTY_WAR_PATH,
			getWarPath());

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