package net.officefloor.servlet.supply;

import javax.servlet.Servlet;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.inject.InjectionRegistry;
import net.officefloor.servlet.tomcat.TomcatServletManager;
import net.officefloor.woof.WoofContext;
import net.officefloor.woof.WoofExtensionService;
import net.officefloor.woof.WoofExtensionServiceFactory;

/**
 * {@link WoofExtensionService} to provide a {@link Servlet} container.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletWoofExtensionService implements WoofExtensionServiceFactory, WoofExtensionService {

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

		// Create the injector registry
		InjectionRegistry injectionRegistry = new InjectionRegistry();

		// Create the embedded servlet container
		ClassLoader classLoader = context.getOfficeExtensionContext().getClassLoader();
		TomcatServletManager servletContainer = new TomcatServletManager("/", injectionRegistry, classLoader);

		// Register the Servlet Supplier (to capture required inject thread locals)
		office.addSupplier("SERVLET", new ServletSupplierSource(servletContainer, injectionRegistry));
	}

}