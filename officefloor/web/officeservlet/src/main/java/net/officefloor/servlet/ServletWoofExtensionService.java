package net.officefloor.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
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

		// Register the Servlet Manager
		OfficeManagedObjectSource mos = office.addOfficeManagedObjectSource(ServletManager.class.getSimpleName(),
				ServletManagerManagedObjectSource.class.getName());
		mos.addOfficeManagedObject(ServletManager.class.getSimpleName(), ManagedObjectScope.THREAD);

		// Provide async context executor
		OfficeManagedObjectTeam mosTeam = mos.getOfficeManagedObjectTeam(AsyncContext.class.getSimpleName());
		OfficeTeam team = office.addOfficeTeam(AsyncContext.class.getSimpleName());
		office.link(mosTeam, team);
	}

}