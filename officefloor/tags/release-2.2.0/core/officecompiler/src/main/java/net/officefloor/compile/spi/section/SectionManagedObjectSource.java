/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.compile.spi.section;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectSource} within an {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionManagedObjectSource {

	/**
	 * Obtains the name of this {@link SectionManagedObjectSource}.
	 * 
	 * @return Name of this {@link SectionManagedObjectSource}.
	 */
	String getSectionManagedObjectSourceName();

	/**
	 * Adds a {@link Property} to source the {@link ManagedObject}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param value
	 *            Value of the {@link Property}.
	 */
	void addProperty(String name, String value);

	/**
	 * Specifies the timeout for the {@link ManagedObject}.
	 * 
	 * @param timeout
	 *            Timeout for the {@link ManagedObject}.
	 */
	void setTimeout(long timeout);

	/**
	 * Obtains the {@link ManagedObjectFlow} for the
	 * {@link ManagedObjectFlowType}.
	 * 
	 * @param managedObjectSourceFlowName
	 *            Name of the {@link ManagedObjectFlowType}.
	 * @return {@link ManagedObjectFlow}.
	 */
	ManagedObjectFlow getManagedObjectFlow(String managedObjectSourceFlowName);

	/**
	 * Obtains the {@link ManagedObjectDependency} for the
	 * {@link ManagedObjectDependencyType} for the Input {@link ManagedObject}.
	 * 
	 * @param managedObjectDependencyName
	 *            Name of the {@link ManagedObjectDependencyType}.
	 * @return {@link ManagedObjectDependency}.
	 */
	ManagedObjectDependency getInputManagedObjectDependency(
			String managedObjectDependencyName);

	/**
	 * Obtains the {@link SectionManagedObject} representing an instance use of
	 * a {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link SectionManagedObject}. Typically this will
	 *            be the name under which the {@link ManagedObject} will be
	 *            registered to the {@link Office}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} of the {@link SectionManagedObject}
	 *            within the {@link Office}.
	 * @return {@link SectionManagedObject}.
	 */
	SectionManagedObject addSectionManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope);

}