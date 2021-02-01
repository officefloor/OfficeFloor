package net.officefloor.server.servlet.test;

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpServer;

/**
 * {@link OfficeFloorExtensionService} to mock {@link HttpServer} setup.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerOfficeFloorExtensionService
		implements OfficeFloorExtensionService, OfficeFloorExtensionServiceFactory {

	/*
	 * ======================= OfficeFloorExtensionService =======================
	 */

	@Override
	public OfficeFloorExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {
		if (MockServerSettings.officeFloorExtensionService != null) {
			MockServerSettings.officeFloorExtensionService.extendOfficeFloor(officeFloorDeployer, context);
		}
	}

}