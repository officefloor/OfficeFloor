package net.officefloor.server.servlet.test;

import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;

/**
 * Settings for running tests.
 * 
 * @author Daniel Sagenschneider
 */
public class MockServerSettings {

	/**
	 * {@link OfficeFloorExtensionService}.
	 */
	static OfficeFloorExtensionService officeFloorExtensionService = null;

	/**
	 * {@link OfficeExtensionService}.
	 */
	static OfficeExtensionService officeExtensionService = null;

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

}
