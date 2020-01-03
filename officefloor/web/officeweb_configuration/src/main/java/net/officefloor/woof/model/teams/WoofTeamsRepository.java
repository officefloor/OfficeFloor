package net.officefloor.woof.model.teams;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.woof.model.teams.WoofTeamsModel;

/**
 * Repository for obtaining the {@link WoofTeamsModel} for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTeamsRepository {

	/**
	 * Retrieves the {@link WoofTeamsModel} from the {@link ConfigurationItem}.
	 * 
	 * @param teams
	 *            {@link WoofTeamsModel}.
	 * @param configuration
	 *            {@link ConfigurationItem}.
	 * @throws Exception
	 *             If fails to retrieve the {@link WoofTeamsModel}.
	 */
	void retrieveWoofTeams(WoofTeamsModel teams, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link WoofTeamsModel} within the
	 * {@link WritableConfigurationItem}.
	 * 
	 * @param teams
	 *            {@link WoofTeamsModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem}.
	 * @throws Exception
	 *             If fails to store the {@link WoofTeamsModel}.
	 */
	void storeWoofTeams(WoofTeamsModel teams, WritableConfigurationItem configuration) throws Exception;

}