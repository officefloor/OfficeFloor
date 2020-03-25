package net.officefloor.servlet.supply;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.Servlet;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.servlet.inject.FieldDependencyExtractor;
import net.officefloor.servlet.inject.FieldDependencyExtractorServiceFactory;
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
		OfficeExtensionContext extensionContext = context.getOfficeExtensionContext();

		// Load the field dependency extractors
		List<FieldDependencyExtractor> extractors = new LinkedList<>();
		for (FieldDependencyExtractor extractor : extensionContext
				.loadOptionalServices(FieldDependencyExtractorServiceFactory.class)) {
			extractors.add(extractor);
		}

		// Create the injector registry
		InjectionRegistry injectionRegistry = new InjectionRegistry(
				extractors.toArray(new FieldDependencyExtractor[extractors.size()]));

		// Create the embedded servlet container
		ClassLoader classLoader = extensionContext.getClassLoader();
		TomcatServletManager servletContainer = new TomcatServletManager("/", injectionRegistry, classLoader);

		// Register the Servlet Supplier (to capture required inject thread locals)
		office.addSupplier("SERVLET", new ServletSupplierSource(servletContainer, injectionRegistry));
	}

}