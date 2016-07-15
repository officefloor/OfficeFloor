/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.compile.section;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeTask;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <code>Type definition</code> of a dependent object of an {@link OfficeTask}.
 *
 * @author Daniel Sagenschneider
 */
public interface ObjectDependencyType {

	/**
	 * <p>
	 * Obtains the name of this object dependency.
	 * <p>
	 * This would correspond to either the {@link TaskObjectType} or the
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
	Class<?> getObjectDependencyType();

	/**
	 * Obtains the type qualifier required of this object dependency.
	 * 
	 * @return Type qualifier required of this object dependency. May be
	 *         <code>null</code> if no qualifier.
	 */
	String getObjectDependencyTypeQualifier();

	/**
	 * <p>
	 * Obtains the {@link ManagedObject} that full fills the dependency.
	 * <p>
	 * Should the {@link ObjectDependencyType} represent a parameter then no
	 * {@link DependentManagedObjectType} will be provided.
	 * <p>
	 * Expected return types are:
	 * <ol>
	 * <li>{@link OfficeSectionManagedObject}</li>
	 * <li>{@link OfficeManagedObject}</li>
	 * <li>{@link OfficeObject}</li>
	 * <li><code>null</code> if not yet linked (or issue in linking)</li>
	 * </ol>
	 * 
	 * @return {@link DependentManagedObjectType} or <code>null</code> if a
	 *         parameter.
	 */
	DependentManagedObjectType getDependentManagedObject();

}