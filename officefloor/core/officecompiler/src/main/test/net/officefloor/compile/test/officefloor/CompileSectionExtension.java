package net.officefloor.compile.test.officefloor;

import net.officefloor.compile.spi.office.OfficeSection;

/**
 * Extends the {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileSectionExtension {

	/**
	 * Extends the {@link OfficeSection}.
	 * 
	 * @param context
	 *            {@link CompileSectionContext}.
	 * @throws Exception
	 *             If fails to extend.
	 */
	void extend(CompileSectionContext context) throws Exception;

}