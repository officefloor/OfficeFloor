package net.officefloor.woof;

import net.officefloor.web.build.WebArchitect;

/**
 * Loads the WoOF configuration to the {@link WebArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofLoader {

	/**
	 * Loads the WoOF configuration.
	 * 
	 * @param context
	 *            {@link WoofContext}.
	 * @throws Exception
	 *             If fails to load the configuration.
	 */
	void loadWoofConfiguration(WoofContext context) throws Exception;

}