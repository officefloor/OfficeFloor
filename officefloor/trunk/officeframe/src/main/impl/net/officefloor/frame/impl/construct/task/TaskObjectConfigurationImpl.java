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
package net.officefloor.frame.impl.construct.task;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.configuration.TaskObjectConfiguration;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link TaskObjectConfiguration} implementation.
 * 
 * @author Daniel
 */
public class TaskObjectConfigurationImpl<D extends Enum<D>> implements
		TaskObjectConfiguration<D> {

	/**
	 * Flag indicating if this is a parameter.
	 */
	private boolean isParameter;

	/**
	 * Name of {@link ManagedObject} within the {@link ManagedObjectScope}.
	 */
	private final String scopeManagedObjectName;

	/**
	 * {@link Object} type required by the {@link Task}.
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
	public TaskObjectConfigurationImpl(boolean isParameter,
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