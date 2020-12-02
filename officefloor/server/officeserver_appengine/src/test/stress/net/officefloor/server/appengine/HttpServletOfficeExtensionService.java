package net.officefloor.server.appengine;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.server.http.HttpServer;

/**
 * {@link OfficeExtensionService} to mock {@link HttpServer} setup.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletOfficeExtensionService implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/**
	 * {@link OfficeExtensionService}.
	 */
	public static OfficeExtensionService officeExtensionService;

	/*
	 * ========================== OfficeExtensionService ========================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
		if (officeExtensionService != null) {
			officeExtensionService.extendOffice(officeArchitect, context);
		}
	}

}