/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.compile.managedobject;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <code>Type definition</code> of a dependency required by the
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectDependencyType<D extends Enum<D>> {

	/**
	 * Obtains the name of the dependency.
	 * 
	 * @return Name of the dependency.
	 */
	String getDependencyName();

	/**
	 * Obtains the index identifying the dependency.
	 * 
	 * @return Index identifying the dependency.
	 */
	int getIndex();

	/**
	 * Obtains the {@link Class} that the dependent object must
	 * extend/implement.
	 * 
	 * @return Type of the dependency.
	 */
	Class<?> getDependencyType();

	/**
	 * Obtains the key identifying the dependency.
	 * 
	 * @return Key identifying the dependency.
	 */
	D getKey();

}