package net.officefloor.woof.compile;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.woof.WoofContext;

/**
 * Extension to compile WoOF into the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileWoofExtension {

	/**
	 * Extends the {@link Office}.
	 * 
	 * @param context {@link WoofContext}.
	 * @throws Exception If fails to extend.
	 */
	void extend(WoofContext context) throws Exception;

}