package net.officefloor.server.http.servlet;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.server.http.HttpServer;

/**
 * {@link OfficeExtensionService} to mock {@link HttpServer} setup.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerOfficeFloorExtensionService implements OfficeFloorExtensionService, OfficeExtensionService {

	/**
	 * {@link OfficeFloorExtensionService}.
	 */
	private static OfficeFloorExtensionService officeFloorExtensionService = null;

	/**
	 * {@link OfficeExtensionService}.
	 */
	private static OfficeExtensionService officeExtensionService = null;

	/**
	 * Logic to run within context.
	 */
	@FunctionalInterface
	public static interface WithinContext {
		void runInContext() throws Exception;
	}

	/**
	 * Runs {@link WithinContext} logic.
	 * 
	 * @param officeFloorExtension {@link OfficeFloorExtensionService}.
	 * @param officeExtension      {@link OfficeExtensionService}.
	 * @param logic                {@link WithinContext} logic.
	 * @throws Exception If failure with logic.
	 */
	public static void runWithinContext(OfficeFloorExtensionService officeFloorExtension,
			OfficeExtensionService officeExtension, WithinContext logic) throws Exception {
		officeFloorExtensionService = officeFloorExtension;
		officeExtensionService = officeExtension;
		try {
			logic.runInContext();
		} finally {
			officeFloorExtensionService = null;
			officeExtensionService = null;
		}
	}

	/*
	 * ======================= OfficeFloorExtensionService =======================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {
		if (officeFloorExtensionService != null) {
			officeFloorExtensionService.extendOfficeFloor(officeFloorDeployer, context);
		}
	}

	/*
	 * ========================== OfficeExtensionService ========================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
		if (officeExtensionService != null) {
			officeExtensionService.extendOffice(officeArchitect, context);
		}
	}

}