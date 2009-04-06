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
package net.officefloor.model.section;

import net.officefloor.model.repository.ConfigurationItem;

/**
 * Repository of {@link SectionModel} instances.
 * 
 * @author Daniel
 */
public interface SectionRepository {

	/**
	 * Retrieves the {@link SectionModel} from the {@link ConfigurationItem}.
	 * 
	 * @param configuration
	 *            {@link ConfigurationItem} containing the {@link SectionModel}.
	 * @return {@link SectionModel}.
	 * @throws Exception
	 *             If fails to retrieve the {@link SectionModel}.
	 */
	SectionModel retrieveSection(ConfigurationItem configuration)
			throws Exception;

	/**
	 * Stores the {@link SectionModel} into the {@link ConfigurationItem}.
	 * 
	 * @param section
	 *            {@link SectionModel}.
	 * @param configuration
	 *            {@link ConfigurationItem} to contain the {@link SectionModel}.
	 * @throws Exception
	 *             If fails to store the {@link SectionModel}.
	 */
	void storeSection(SectionModel section, ConfigurationItem configuration)
			throws Exception;

}