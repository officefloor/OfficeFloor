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
package net.officefloor.compile.object;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.spi.office.OfficeSectionFunction;

/**
 * <code>Type definition</code> of a dependent object of an {@link OfficeSectionFunction}.
 *
 * @author Daniel Sagenschneider
 */
public interface ObjectDependencyType {

	/**
	 * <p>
	 * Obtains the name of this object dependency.
	 * <p>
	 * This would correspond to either the {@link ManagedFunctionObjectType} or the
	 * {@link ManagedObjectDependencyType} name.
	 * 
	 * @return Name of this object dependency.
	 */
	String getObjectDependencyName();

	/**
	 * Obtains the type required of this object dependency.
	 * 
	 * @return Type required of this object dependency.
	 */
	String getObjectDependencyType();

	/**
	 * Obtains the type qualifier required of this object dependency.
	 * 
	 * @return Type qualifier required of this object dependency. May be
	 *         <code>null</code> if no qualifier.
	 */
	String getObjectDependencyTypeQualifier();

	/**
	 * Indicates if the object dependency is a parameter.
	 * 
	 * @return <code>true</code> if object dependency is a parameter.
	 */
	boolean isParameter();

	/**
	 * <p>
	 * Obtains the object that fulfills the dependency.
	 * <p>
	 * Should the {@link ObjectDependencyType} represent a parameter then no
	 * {@link DependentObjectType} will be provided.
	 * 
	 * @return {@link DependentObjectType} or <code>null</code> if parameter or
	 *         unable to obtain {@link DependentObjectType}.
	 */
	DependentObjectType getDependentObjectType();

}