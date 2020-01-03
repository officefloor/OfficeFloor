package net.officefloor.woof.model.objects;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.woof.model.objects.WoofObjectsModel;
import net.officefloor.woof.model.objects.WoofObjectsRepository;

/**
 * {@link WoofObjectsRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofObjectsRepositoryImpl implements WoofObjectsRepository {

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
	public WoofObjectsRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ================== WoofObjectsRepository ====================
	 */

	@Override
	public void retrieveWoofObjects(WoofObjectsModel objects, ConfigurationItem configuration) throws Exception {
		this.modelRepository.retrieve(objects, configuration);
	}

	@Override
	public void storeWoofObjects(WoofObjectsModel objects, WritableConfigurationItem configuration) throws Exception {
		this.modelRepository.store(objects, configuration);
	}

}