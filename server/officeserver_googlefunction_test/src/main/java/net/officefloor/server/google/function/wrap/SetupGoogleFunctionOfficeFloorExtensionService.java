package net.officefloor.server.google.function.wrap;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link OfficeFloorExtensionService} to setup testing a Google
 * {@link HttpFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class SetupGoogleFunctionOfficeFloorExtensionService
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
		OfficeFloorExtensionService extension = AbstractSetupGoogleHttpFunctionJUnit.officeFloorExtensionService.get();
		if (extension != null) {
			extension.extendOfficeFloor(officeFloorDeployer, context);
		}
	}

}
