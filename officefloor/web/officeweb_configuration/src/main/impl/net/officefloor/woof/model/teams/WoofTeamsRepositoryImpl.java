package net.officefloor.woof.model.teams;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.woof.model.teams.WoofTeamsModel;
import net.officefloor.woof.model.teams.WoofTeamsRepository;

/**
 * {@link WoofTeamsRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTeamsRepositoryImpl implements WoofTeamsRepository {

	/**
	 * {@link ModelRepository}.
	 */
	private final ModelRepository modelRepository;

	/**
	 * Initiate.
	 * 
	 * @param modelRepository
	 *            {@link ModelRepository}.
	 */
	public WoofTeamsRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ================== WoofTeamsRepository ====================
	 */

	@Override
	public void retrieveWoofTeams(WoofTeamsModel teams, ConfigurationItem configuration) throws Exception {
		this.modelRepository.retrieve(teams, configuration);
	}

	@Override
	public void storeWoofTeams(WoofTeamsModel teams, WritableConfigurationItem configuration) throws Exception {
		this.modelRepository.store(teams, configuration);
	}

}