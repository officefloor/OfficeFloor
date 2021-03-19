/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.woof.model.objects;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

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
