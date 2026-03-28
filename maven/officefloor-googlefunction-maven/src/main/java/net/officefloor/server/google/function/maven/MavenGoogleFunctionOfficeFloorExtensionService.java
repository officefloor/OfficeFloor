package net.officefloor.server.google.function.maven;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.google.function.test.AbstractGoogleHttpFunctionJUnit;
import net.officefloor.test.OfficeFloorExtension;

/**
 * {@link OfficeFloorExtension} to run Google Function.
 * 
 * @author Daniel Sagenschneider
 */
public class MavenGoogleFunctionOfficeFloorExtensionService
		implements OfficeFloorExtensionService, OfficeFloorExtensionServiceFactory {

	/**
	 * {@link Property} name for the HTTP port.
	 */
	public static final String HTTP_PORT_NAME = "officefloor.google.function.http.port";

	/**
	 * {@link Property} name for the HTTPS port.
	 */
	public static final String HTTPS_PORT_NAME = "officefloor.google.function.https.port";

	/*
	 * ================ OfficeFloorExtensionServiceFactory ================
	 */

	@Override
	public OfficeFloorExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== OfficeFloorExtensionService ====================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Extend with HTTP server socket for Google HTTP Function
		String httpPort = System.getProperty(HTTP_PORT_NAME);
		String httpsPort = System.getProperty(HTTPS_PORT_NAME);
		OfficeFloorExtensionService officeFloorExtension = AbstractGoogleHttpFunctionJUnit
				.getHttpServerSocketOfficeFloorExtensionService(Integer.parseInt(httpPort),
						Integer.parseInt(httpsPort));
		officeFloorExtension.extendOfficeFloor(officeFloorDeployer, context);
	}

}
