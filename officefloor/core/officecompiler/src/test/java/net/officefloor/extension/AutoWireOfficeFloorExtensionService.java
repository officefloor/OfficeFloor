package net.officefloor.extension;

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Provides means to auto-wire aspects of the {@link OfficeFloor} within
 * testing.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeFloorExtensionService implements OfficeFloorExtensionService {

	/**
	 * Indicates whether to auto-wire the objects.
	 */
	private static boolean isEnableAutoWireObjects = false;

	/**
	 * Indicates whether to auto-wire the teams.
	 */
	private static boolean isEnableAutoWireTeams = false;

	/**
	 * Resets the state for next test.
	 */
	public static void reset() {
		isEnableAutoWireObjects = false;
		isEnableAutoWireTeams = false;
	}

	/**
	 * Enable auto-wire of the objects.
	 */
	public static void enableAutoWireObjects() {
		isEnableAutoWireObjects = true;
	}

	/**
	 * Enable auto-wire of the teams.
	 */
	public static void enableAutoWireTeams() {
		isEnableAutoWireTeams = true;
	}

	/*
	 * =================== OfficeFloorExtensionService ===================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Enable auto-wiring as appropriate
		if (isEnableAutoWireObjects) {
			officeFloorDeployer.enableAutoWireObjects();
		}
		if (isEnableAutoWireTeams) {
			officeFloorDeployer.enableAutoWireTeams();
		}
	}

}