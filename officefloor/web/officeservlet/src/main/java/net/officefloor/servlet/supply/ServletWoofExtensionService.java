package net.officefloor.servlet.supply;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.source.ServiceContext;
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

		// Create the map for servlet injection
		Map<Class<?>, ServletInjector> injectors = new HashMap<>();

		// Create the embedded servlet container
		ClassLoader classLoader = context.getOfficeExtensionContext().getClassLoader();
		TomcatServletManager servletContainer = new TomcatServletManager("/", injectors, classLoader);

		// Register the Servlet Supplier (to capture required inject thread locals)
		office.addSupplier("SERVLET", new ServletSupplierSource(servletContainer, injectors));
	}

}