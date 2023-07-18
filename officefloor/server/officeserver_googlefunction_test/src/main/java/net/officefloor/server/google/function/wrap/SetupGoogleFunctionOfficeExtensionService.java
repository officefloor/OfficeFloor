package net.officefloor.server.google.function.wrap;

import com.google.cloud.functions.HttpFunction;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link OfficeExtensionService} to setup an testing of Google
 * {@link HttpFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class SetupGoogleFunctionOfficeExtensionService
		implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/*
	 * ========================== OfficeExtensionService ========================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
		OfficeExtensionService extension = AbstractSetupGoogleHttpFunctionJUnit.officeExtensionService.get();
		if (extension != null) {
			extension.extendOffice(officeArchitect, context);
		}
	}

}
