/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.managedfunction;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link ManagedFunctionObjectConfiguration} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionObjectConfigurationImpl<D extends Enum<D>> implements
		ManagedFunctionObjectConfiguration<D> {

	/**
	 * Flag indicating if this is a parameter.
	 */
	private boolean isParameter;

	/**
	 * Name of {@link ManagedObject} within the {@link ManagedObjectScope}.
	 */
	private final String scopeManagedObjectName;

	/**
	 * {@link Object} type required by the {@link ManagedFunction}.
	 */
	private final Class<?> objectType;

	/**
	 * Index of this {@link Object}.
	 */
	private final int index;

	/**
	 * Key of this {@link Object}.
	 */
	private final D key;

	/**
	 * Initiate.
	 * 
	 * @param isParameter
	 *            Indicates if a parameter.
	 * @param scopeManagedObjectName
	 *            Name of {@link ManagedObject} within the
	 *            {@link ManagedObjectScope}.
	 * @param objectType
	 *            Type required of the dependent {@link Object}.
	 * @param index
	 *            Index of this {@link Object}.
	 * @param key
	 *            Key of this {@link Object}.
	 */
	public ManagedFunctionObjectConfigurationImpl(boolean isParameter,
			String scopeManagedObjectName, Class<?> objectType, int index, D key) {
		this.isParameter = isParameter;
		this.scopeManagedObjectName = scopeManagedObjectName;
		this.objectType = objectType;
		this.index = index;
		this.key = key;
	}

	/*
	 * =================== TaskObjectConfiguration =============================
	 */

	@Override
	public boolean isParameter() {
		return this.isParameter;
	}

	@Override
	public String getScopeManagedObjectName() {
		return this.scopeManagedObjectName;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public D getKey() {
		return this.key;
	}

}
