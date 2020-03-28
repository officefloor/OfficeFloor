package net.officefloor.webapp;

import javax.servlet.Servlet;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.woof.WoofContext;
import net.officefloor.woof.WoofExtensionService;
import net.officefloor.woof.WoofExtensionServiceFactory;

/**
 * {@link WoofExtensionService} to provide a {@link Servlet} container.
 * 
 * @author Daniel Sagenschneider
 */
public class WebAppWoofExtensionService implements WoofExtensionServiceFactory, WoofExtensionService {

	/*
	 * ================ WoofExtensionServiceFactory ===================
	 */

	@Override
	public WoofExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== WoofExtensionService =========================
	 */

	@Override
	public void extend(WoofContext context) throws Exception {

		OfficeArchitect office = context.getOfficeArchitect();

		// Hook in WebApp servicing
		OfficeSection servicer = office.addOfficeSection("SERVLET_SERVICER_SECTION",
				ServletSectionSource.class.getName(), null);
		context.getWebArchitect().chainServicer(servicer.getOfficeSectionInput(ServletSectionSource.INPUT),
				servicer.getOfficeSectionOutput(ServletSectionSource.OUTPUT));
	}

}