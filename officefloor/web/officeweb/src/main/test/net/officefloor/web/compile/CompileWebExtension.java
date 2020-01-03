package net.officefloor.web.compile;

import net.officefloor.frame.api.manage.Office;

/**
 * Extension to compile the web application into the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileWebExtension {

	/**
	 * Extends the {@link Office}.
	 * 
	 * @param context
	 *            {@link CompileWebContext}.
	 * @throws Exception
	 *             If fails to extend.
	 */
	void extend(CompileWebContext context) throws Exception;

}
