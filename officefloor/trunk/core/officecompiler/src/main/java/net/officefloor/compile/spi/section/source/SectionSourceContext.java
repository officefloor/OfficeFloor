/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.compile.spi.section.source;

import java.util.Properties;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * Context for loading a {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionSourceContext {

	/**
	 * <p>
	 * Obtains the location of the {@link OfficeSection}.
	 * <p>
	 * How &quot;location&quot; is interpreted is for the {@link SectionSource},
	 * however passing it to {@link #getConfiguration(String)} should return a
	 * {@link ConfigurationItem}.
	 * 
	 * @return Location of the {@link OfficeSection}.
	 */
	String getSectionLocation();

	/**
	 * Obtains the {@link ConfigurationItem}.
	 * 
	 * @param location
	 *            Location of the {@link ConfigurationItem}.
	 * @return {@link ConfigurationItem} or <code>null</code> if can not find
	 *         the {@link ConfigurationItem}.
	 */
	ConfigurationItem getConfiguration(String location);

	/**
	 * <p>
	 * Obtains the names of the available properties in the order they were
	 * defined. This allows for ability to provide variable number of properties
	 * identified by a naming convention and being able to maintain their order.
	 * <p>
	 * An example would be providing a listing of routing configurations, each
	 * entry named <code>route.[something]</code> and order indicating priority.
	 * 
	 * @return Names of the properties in the order defined.
	 */
	String[] getPropertyNames();

	/**
	 * Obtains a required property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Value of the property.
	 * @throws SectionUnknownPropertyError
	 *             If property was not configured. Let this propagate as the
	 *             framework will handle it.
	 */
	String getProperty(String name) throws SectionUnknownPropertyError;

	/**
	 * Obtains the property value or subsequently the default value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param defaultValue
	 *            Default value if property not specified.
	 * @return Value of the property or the the default value if not specified.
	 */
	String getProperty(String name, String defaultValue);

	/**
	 * Properties to configure the {@link SectionType}.
	 * 
	 * @return Properties specific for the {@link SectionType}.
	 */
	Properties getProperties();

	/**
	 * Obtains the {@link ClassLoader} for loading the {@link SectionType}.
	 * 
	 * @return {@link ClassLoader} for loading the {@link SectionType}.
	 */
	ClassLoader getClassLoader();

	/**
	 * Creates a {@link PropertyList} for loading types.
	 * 
	 * @return New {@link PropertyList} to aid in loading types.
	 */
	PropertyList createPropertyList();

	/**
	 * <p>
	 * Loads the {@link WorkType}.
	 * <p>
	 * This is to enable obtaining the type information for the {@link Work} to
	 * allow reflective configuration by the {@link SectionSource}.
	 * 
	 * @param workSourceClassName
	 *            Name of the implementing {@link WorkSource} class. May also be
	 *            an alias.
	 * @param properties
	 *            {@link PropertyList} to configure the implementing
	 *            {@link WorkSource}.
	 * @return {@link WorkType} or <code>null</code> if fails to load the
	 *         {@link WorkType}.
	 */
	WorkType<?> loadWorkType(String workSourceClassName, PropertyList properties);

	/**
	 * <p>
	 * Loads the {@link ManagedObjectType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link ManagedObject} to allow reflective configuration by the
	 * {@link SectionSource}.
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
	 * Loads the {@link SectionType}.
	 * <p>
	 * This is to enable obtaining the type information for the
	 * {@link SubSection} to allow reflective configuration by the
	 * {@link SectionSource}.
	 * 
	 * @param sectionSourceClassName
	 *            Name of the implementing {@link SectionSource} class. May also
	 *            be an alias.
	 * @param location
	 *            Location of the {@link SubSection}.
	 * @param properties
	 *            {@link PropertyList} to configure the {@link SectionSource}.
	 * @return {@link SectionType} or <code>null</code> if fails to load the
	 *         {@link SectionType}.
	 */
	SectionType loadSectionType(String sectionSourceClassName, String location,
			PropertyList properties);

}