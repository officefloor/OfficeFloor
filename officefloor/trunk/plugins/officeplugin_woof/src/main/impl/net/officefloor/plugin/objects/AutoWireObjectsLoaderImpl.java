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
package net.officefloor.plugin.objects;

import net.officefloor.autowire.AutoWireApplication;
import net.officefloor.model.objects.AutoWireObjectsRepository;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link AutoWireObjectsLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireObjectsLoaderImpl implements AutoWireObjectsLoader {

	/**
	 * {@link AutoWireObjectsRepository}.
	 */
	private final AutoWireObjectsRepository repository;

	/**
	 * Initiate.
	 * 
	 * @param repository
	 *            {@link AutoWireObjectsRepository}.
	 */
	public AutoWireObjectsLoaderImpl(AutoWireObjectsRepository repository) {
		this.repository = repository;
	}

	/*
	 * ======================= AutoWireObjectsLoader ===========================
	 */

	@Override
	public void loadAutoWireObjectsConfiguration(
			ConfigurationItem objectsConfiguration,
			AutoWireApplication application) throws Exception {
		// TODO implement AutoWireObjectsLoader.loadAutoWireObjectsConfiguration
		throw new UnsupportedOperationException(
				"TODO implement AutoWireObjectsLoader.loadAutoWireObjectsConfiguration");
	}

}