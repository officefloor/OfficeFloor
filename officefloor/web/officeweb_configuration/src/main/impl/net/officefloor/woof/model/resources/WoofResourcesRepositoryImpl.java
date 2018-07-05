/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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