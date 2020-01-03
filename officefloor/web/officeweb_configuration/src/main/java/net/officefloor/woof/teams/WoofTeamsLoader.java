package net.officefloor.woof.teams;

import net.officefloor.woof.model.teams.WoofTeamsModel;

/**
 * Loads the {@link WoofTeamsModel} configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTeamsLoader {

	/**
	 * Loads the {@link WoofTeamsModel} configuration.
	 * 
	 * @param context {@link WoofTeamsLoaderContext}.
	 * @throws Exception If fails to load the configuration.
	 */
	void loadWoofTeamsConfiguration(WoofTeamsLoaderContext context) throws Exception;

	/**
	 * Loads the use of the {@link WoofTeamsModel} configuration.
	 * 
	 * @param context {@link WoofTeamsUsageContext}.
	 * @throws Exception If fails to load the usage.
	 */
	void loadWoofTeamsUsage(WoofTeamsUsageContext context) throws Exception;

}