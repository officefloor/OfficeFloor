package net.officefloor.woof.model.resources;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link WoofResourcesRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofResourcesRepositoryImpl implements WoofResourcesRepository {

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
	public WoofResourcesRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ================== WoofResourcesRepository ====================
	 */

	@Override
	public void retrieveWoofResources(WoofResourcesModel resources, ConfigurationItem configuration) throws Exception {
		this.modelRepository.retrieve(resources, configuration);
	}

	@Override
	public void storeWoofResources(WoofResourcesModel resources, WritableConfigurationItem configuration)
			throws Exception {
		this.modelRepository.store(resources, configuration);
	}

}