package net.officefloor.server.google.function.maven;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.google.function.OfficeFloorHttpFunction;
import net.officefloor.server.google.function.wrap.AbstractSetupGoogleHttpFunctionJUnit;

/**
 * {@link OfficeExtension} to run Google Function.
 * 
 * @author Daniel Sagenschneider
 */
public class MavenGoogleFunctionOfficeExtensionService implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/*
	 * =================== OfficeExtensionServiceFactory =================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== OfficeExtensionService ======================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Extend for HTTP Function wrapping
		AbstractSetupGoogleHttpFunctionJUnit.getHttpFunctionOfficeExtension(OfficeFloorHttpFunction.class)
				.extendOffice(officeArchitect, context);
	}

}
