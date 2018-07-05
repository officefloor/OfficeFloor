/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.construct.office;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.configuration.BoundInputManagedObjectConfiguration;

/**
 * {@link BoundInputManagedObjectConfiguration} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class BoundInputManagedObjectConfigurationImpl implements
		BoundInputManagedObjectConfiguration {

	/**
	 * Input {@link ManagedObject} name.
	 */
	private final String inputManagedObjectName;

	/**
	 * Name of the {@link ManagedObjectSource} to bind to the input
	 * {@link ManagedObject}.
	 */
	private final String boundManagedObjectSourceName;

	/**
	 * Initiate.
	 *
	 * @param inputManagedObjectName
	 *            Input {@link ManagedObject} name.
	 * @param boundManagedObjectSourceName
	 *            Name of the {@link ManagedObjectSource} to bind to the input
	 *            {@link ManagedObject}.
	 */
	public BoundInputManagedObjectConfigurationImpl(
			String inputManagedObjectName, String boundManagedObjectSourceName) {
		this.inputManagedObjectName = inputManagedObjectName;
		this.boundManagedObjectSourceName = boundManagedObjectSourceName;
	}

	/*
	 * =============== BoundInputManagedObjectConfiguration ====================
	 */

	@Override
	public String getInputManagedObjectName() {
		return this.inputManagedObjectName;
	}

	@Override
	public String getBoundManagedObjectSourceName() {
		return this.boundManagedObjectSourceName;
	}

}