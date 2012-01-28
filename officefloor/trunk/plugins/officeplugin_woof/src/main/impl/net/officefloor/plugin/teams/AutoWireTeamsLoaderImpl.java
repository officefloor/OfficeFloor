/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.teams;

import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.teams.AutoWireTeamsRepository;

/**
 * {@link AutoWireTeamsLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireTeamsLoaderImpl implements AutoWireTeamsLoader {

	/**
	 * {@link AutoWireTeamsRepository}.
	 */
	private final AutoWireTeamsRepository repository;

	/**
	 * Initiate.
	 * 
	 * @param repository
	 *            {@link AutoWireTeamsRepository}.
	 */
	public AutoWireTeamsLoaderImpl(AutoWireTeamsRepository repository) {
		this.repository = repository;
	}

	/*
	 * ======================= AutoWireTeamsLoader ===========================
	 */

	@Override
	public void loadAutoWireTeamsConfiguration(
			ConfigurationItem teamsConfiguration,
			AutoWireApplication application) throws Exception {
		// TODO implement AutoWireTeamsLoader.loadAutoWireTeamsConfiguration
		throw new UnsupportedOperationException(
				"TODO implement AutoWireTeamsLoader.loadAutoWireTeamsConfiguration");
	}

}