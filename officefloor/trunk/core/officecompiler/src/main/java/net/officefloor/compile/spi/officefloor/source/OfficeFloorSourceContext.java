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

package net.officefloor.compile.spi.officefloor.source;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.SourceContext;

/**
 * Context for the {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorSourceContext extends SourceContext {

	/**
	 * <p>
	 * Obtains the location of the {@link OfficeFloor}.
	 * <p>
	 * How &quot;location&quot; is interpreted is for the
	 * {@link OfficeFloorSource}.
	 * 
	 * @return Location of the {@link OfficeFloor}.
	 */
	String getOfficeFloorLocation();

	/**
	 * Creates a new {@link PropertyList}.
	 * 
	 * @return New {@link PropertyList}.
	 */
	PropertyList createPropertyList();

	/**
	 * <p>
	 * Loads the {@link ManagedObjectType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link ManagedObject} to allow reflective configuration by the
	 * {@link OfficeFloorSource}.
	 * 
	 * @param managedObjectSourceClassName
	 *            Name of the implementing {@link ManagedObjectSource} class.
	 *            May also be an alias.
	 * @param properties
	 *            {@link PropertyList} to configure the
	 *            {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectType} or <code>null</code> if fails to load
	 *         the {@link ManagedObjectType}.
	 */
	ManagedObjectType<?> loadManagedObjectType(
			String managedObjectSourceClassName, PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link OfficeType}.
	 * <p>
	 * This is to enable obtaining the type information for the {@link Office}
	 * to allow reflective configuration by the {@link OfficeFloorSource}.
	 * 
	 * @param officeSourceClassName
	 *            Name of the implementing {@link OfficeSource} class. May also
	 *            be an alias.
	 * @param location
	 *            Location of the {@link Office}.
	 * @param properties
	 *            {@link PropertyList} to configure the {@link OfficeSource}.
	 * @return {@link OfficeType} or <code>null</code> if fails to load the
	 *         {@link OfficeType}.
	 */
	OfficeType loadOfficeType(String officeSourceClassName, String location,
			PropertyList properties);

}