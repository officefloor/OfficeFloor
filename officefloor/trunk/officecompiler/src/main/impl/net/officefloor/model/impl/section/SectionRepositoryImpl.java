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
package net.officefloor.model.impl.section;

import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.SectionRepository;

/**
 * {@link SectionRepository} implementation.
 * 
 * @author Daniel
 */
public class SectionRepositoryImpl implements SectionRepository {

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
	public SectionRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ===================== SectionRepository ==============================
	 */

	@Override
	public SectionModel retrieveSection(ConfigurationItem configuration)
			throws Exception {

		// Load the section from the configuration
		SectionModel section = this.modelRepository.retrieve(
				new SectionModel(), configuration);

		// TODO connect output -> input

		// TODO connect output -> external flow

		// TODO connect object -> external managed object

		// Return the section
		return section;
	}

	@Override
	public void storeSection(SectionModel section,
			ConfigurationItem configuration) throws Exception {

		// TODO store output -> input

		// TODO store output -> external flow

		// TODO store object -> external managed object

		// Store the section into the configuration
		this.modelRepository.store(section, configuration);
	}

}