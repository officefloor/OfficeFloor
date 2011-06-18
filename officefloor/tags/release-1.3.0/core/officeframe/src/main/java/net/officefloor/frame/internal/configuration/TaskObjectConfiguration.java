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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Configuration for a dependent {@link Object} of a {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskObjectConfiguration<D> {

	/**
	 * Indicates if this dependent {@link Object} is the argument passed to the
	 * {@link Task}.
	 * 
	 * @return <code>true</code> if is argument passed to the {@link Task}.
	 *         <code>false</code> indicates it is a {@link ManagedObject}
	 *         dependency.
	 */
	boolean isParameter();

	/**
	 * <p>
	 * Obtains the name of the {@link ManagedObject} within the
	 * {@link ManagedObjectScope}.
	 * <p>
	 * This must return a value if not a parameter.
	 * 
	 * @return Name of the {@link ManagedObject} within the
	 *         {@link ManagedObjectScope}.
	 */
	String getScopeManagedObjectName();

	/**
	 * Obtains the type of {@link Object} required by the {@link Task}.
	 * 
	 * @return Type of {@link Object} required by the {@link Task}.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the index identifying the dependent {@link Object}.
	 * 
	 * @return Index identifying the dependent {@link Object}.
	 */
	int getIndex();

	/**
	 * Obtains the key identifying the dependent {@link Object}.
	 * 
	 * @return Key identifying the dependent {@link Object}. <code>null</code>
	 *         if indexed.
	 */
	D getKey();

}