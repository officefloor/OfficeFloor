package net.officefloor.compile.test.officefloor;

import net.officefloor.frame.api.manage.Office;

/**
 * Extension for the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileOfficeExtension {

	/**
	 * Extends the {@link Office}.
	 * 
	 * @param context
	 *            {@link CompileOfficeContext}.
	 * @throws Exception
	 *             If fails to extend.
	 */
	void extend(CompileOfficeContext context) throws Exception;

}