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
package net.officefloor.plugin.gwt.module;

import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.model.repository.ModelRepository;

/**
 * {@link GwtModuleRepository} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GwtModuleRepositoryImpl implements GwtModuleRepository {

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
	public GwtModuleRepositoryImpl(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	/*
	 * ===================== GwtModuleRepository =========================
	 */

	@Override
	public GwtModuleModel retrieveGwtModule(ConfigurationItem configuration)
			throws Exception {
		return this.modelRepository.retrieve(new GwtModuleModel(),
				configuration);
	}

	@Override
	public void createGwtModule(GwtModuleModel module,
			ConfigurationItem configuration) throws Exception {
		// TODO implement GwtModuleRepository.createGwtModule
		throw new UnsupportedOperationException(
				"TODO implement GwtModuleRepository.createGwtModule");
	}

	@Override
	public void updateGwtModule(GwtModuleModel module,
			ConfigurationItem configuration) throws Exception {
		// TODO implement GwtModuleRepository.updateGwtModule
		throw new UnsupportedOperationException(
				"TODO implement GwtModuleRepository.updateGwtModule");
	}

}