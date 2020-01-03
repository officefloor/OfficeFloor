package net.officefloor.compile.spi.office.extension;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.manage.Office;

/**
 * Enables plug-in extension of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeExtensionService {

	/**
	 * Extends the {@link Office}.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param context
	 *            {@link OfficeExtensionContext}.
	 * @throws Exception
	 *             If fails to extend the {@link Office}.
	 */
	void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception;

}