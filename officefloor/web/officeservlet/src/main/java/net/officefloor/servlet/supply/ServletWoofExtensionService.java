package net.officefloor.servlet.supply;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.servlet.ServletServicer;
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

		// Create the embedded servlet container
		ClassLoader classLoader = context.getOfficeExtensionContext().getClassLoader();
		TomcatServletManager servletContainer = new TomcatServletManager("/", classLoader);

		// Register the managed object
		ServletManagerManagedObjectSource servletMos = new ServletManagerManagedObjectSource(servletContainer);
		OfficeManagedObjectSource mos = office.addOfficeManagedObjectSource(ServletServicer.class.getSimpleName(),
				servletMos);
		mos.addOfficeManagedObject(ServletServicer.class.getSimpleName(), ManagedObjectScope.THREAD);

		// Register the Servlet Supplier (to capture required inject thread locals)
		ServletSupplierSource servletSupplier = new ServletSupplierSource(servletContainer);
		office.addSupplier("SERVLET", servletSupplier);

		// Provide async context executor
		OfficeManagedObjectTeam mosTeam = mos.getOfficeManagedObjectTeam(AsyncContext.class.getSimpleName());
		OfficeTeam team = office.addOfficeTeam(AsyncContext.class.getSimpleName());
		office.link(mosTeam, team);
	}

}