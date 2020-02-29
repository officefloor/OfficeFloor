package net.officefloor.servlet;

import javax.servlet.Servlet;

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

		// Register the Servlet Manager
		context.getOfficeArchitect()
				.addOfficeManagedObjectSource(ServletManager.class.getSimpleName(),
						ServletManagerManagedObjectSource.class.getName())
				.addOfficeManagedObject(ServletManager.class.getSimpleName(), ManagedObjectScope.THREAD);
	}

}