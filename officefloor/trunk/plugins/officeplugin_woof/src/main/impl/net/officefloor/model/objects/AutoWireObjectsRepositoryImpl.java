/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.model.objects;

import net.officefloor.model.objects.AutoWireObjectsModel;
import net.officefloor.model.objects.AutoWireObjectsRepository;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link AutoWireObjectsRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireObjectsRepositoryImpl implements AutoWireObjectsRepository {

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
	public AutoWireObjectsRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ================== AutoWireObjectsRepository ====================
	 */

	@Override
	public AutoWireObjectsModel retrieveAutoWireObjects(
			ConfigurationItem configuration) throws Exception {
		return this.modelRepository.retrieve(new AutoWireObjectsModel(),
				configuration);
	}

	@Override
	public void storeAutoWireObjects(AutoWireObjectsModel objects,
			ConfigurationItem configuration) throws Exception {
		this.modelRepository.store(objects, configuration);
	}

}