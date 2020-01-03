package net.officefloor.compile.spi.officefloor.extension;

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Enables plug-in extension of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorExtensionService {

	/**
	 * Extends the {@link OfficeFloor}.
	 * 
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param context
	 *            {@link OfficeFloorExtensionContext}.
	 * @throws Exception
	 *             If fails to extend the {@link OfficeFloor}.
	 */
	void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception;

}