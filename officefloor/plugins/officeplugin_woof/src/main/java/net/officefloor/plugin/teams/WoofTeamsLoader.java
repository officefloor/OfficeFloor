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
package net.officefloor.plugin.teams;

import net.officefloor.model.teams.WoofTeamsModel;

/**
 * Loads the {@link WoofTeamsModel} configuration.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTeamsLoader {

	/**
	 * Loads the {@link WoofTeamsModel} configuration.
	 * 
	 * @param context
	 *            {@link WoofTeamsLoaderContext}.
	 * @throws Exception
	 *             If fails to load the configuration.
	 */
	void loadAutoWireTeamsConfiguration(WoofTeamsLoaderContext context) throws Exception;

}