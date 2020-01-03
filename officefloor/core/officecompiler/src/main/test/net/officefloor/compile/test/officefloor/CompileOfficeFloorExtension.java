package net.officefloor.compile.test.officefloor;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Extension for the {@link CompileOfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileOfficeFloorExtension {

	/**
	 * Extends the {@link OfficeFloor}.
	 * 
	 * @param context
	 *            {@link CompileOfficeFloorContext}.
	 * @throws Exception
	 *             If fails to extend.
	 */
	void extend(CompileOfficeFloorContext context) throws Exception;

}