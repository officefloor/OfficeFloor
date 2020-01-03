package net.officefloor.server.http;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Validate that the {@link HttpServer} is correctly using
 * {@link OfficeFloorHttpServerImplementation} as the default
 * {@link HttpServerImplementation}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValidateDefaultHttpServerTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct default {@link HttpServerImplementation}.
	 */
	public void testDefaultHttpServerImplementation() {
		assertEquals("Incorrect default HTTP server implementation",
				OfficeFloorHttpServerImplementation.class.getName(),
				HttpServer.DEFAULT_HTTP_SERVER_IMPLEMENTATION_CLASS_NAME);
	}

}