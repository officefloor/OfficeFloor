package net.officefloor.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;

/**
 * {@link OfficeFloorExtensionService} for {@link Servlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletOfficeFloorExtensionService
		implements OfficeFloorExtensionService, OfficeFloorExtensionServiceFactory {

	/*
	 * ==================== OfficeFloorExtensionServiceFactory ====================
	 */

	@Override
	public OfficeFloorExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== OfficeFloorExtensionService =======================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Add team for Servlet Async
		OfficeFloorTeam team = officeFloorDeployer.addTeam(AsyncContext.class.getSimpleName(),
				ExecutorCachedTeamSource.class.getName());
		team.addTypeQualification(null, AsyncContext.class.getSimpleName());

		// Provide team for Servlet Async
		for (DeployedOffice office : officeFloorDeployer.getDeployedOffices()) {
			officeFloorDeployer.link(office.getDeployedOfficeTeam(AsyncContext.class.getSimpleName()), team);
		}
	}

}