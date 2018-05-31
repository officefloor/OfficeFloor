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
package net.officefloor.model.section;

import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;

/**
 * Repository of {@link SectionModel} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionRepository {

	/**
	 * Retrieves the {@link SectionModel} from the {@link ConfigurationItem}.
	 * 
	 * @param section
	 *            {@link SectionModel}.
	 * @param configuration
	 *            {@link ConfigurationItem} containing the {@link SectionModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link SectionModel}.
	 */
	void retrieveSection(SectionModel section, ConfigurationItem configuration) throws Exception;

	/**
	 * Stores the {@link SectionModel} into the {@link ConfigurationItem}.
	 * 
	 * @param section
	 *            {@link SectionModel}.
	 * @param configuration
	 *            {@link WritableConfigurationItem} to contain the
	 *            {@link SectionModel}.
	 * @throws Exception
	 *             If fails to store the {@link SectionModel}.
	 */
	void storeSection(SectionModel section, WritableConfigurationItem configuration) throws Exception;

}