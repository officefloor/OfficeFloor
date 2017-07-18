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
package net.officefloor.model.teams;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

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
	 * ================== AutoWireTeamsRepository ====================
	 */

	@Override
	public WoofTeamsModel retrieveAutoWireTeams(
			ConfigurationItem configuration) throws Exception {
		return this.modelRepository.retrieve(new WoofTeamsModel(),
				configuration);
	}

	@Override
	public void storeAutoWireTeams(WoofTeamsModel teams,
			ConfigurationItem configuration) throws Exception {
		this.modelRepository.store(teams, configuration);
	}

}