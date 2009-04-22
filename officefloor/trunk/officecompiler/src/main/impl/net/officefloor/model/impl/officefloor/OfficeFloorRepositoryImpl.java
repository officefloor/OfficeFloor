/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.model.impl.officefloor;

import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorRepository;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link OfficeFloorRepository} implementation.
 * 
 * @author Daniel
 */
public class OfficeFloorRepositoryImpl implements OfficeFloorRepository {

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
	public OfficeFloorRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ===================== OfficeFloorRepository =============================
	 */

	@Override
	public OfficeFloorModel retrieveOfficeFloor(ConfigurationItem configuration)
			throws Exception {

		// Load the office floor from configuration
		OfficeFloorModel officeFloor = this.modelRepository.retrieve(
				new OfficeFloorModel(), configuration);

		// TODO link the connections

		// Return the office floor
		return officeFloor;
	}

	@Override
	public void storeOfficeFloor(OfficeFloorModel officeFloor,
			ConfigurationItem configuration) throws Exception {

		// TODO specify the links

		// Store the office floor
		this.modelRepository.store(officeFloor, configuration);
	}

}