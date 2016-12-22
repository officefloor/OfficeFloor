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
package net.officefloor.compile.spi.section.source;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.SourceContext;

/**
 * Context for loading a {@link SectionType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionSourceContext extends SourceContext {

	/**
	 * <p>
	 * Obtains the location of the {@link OfficeSection}.
	 * <p>
	 * How &quot;location&quot; is interpreted is for the {@link SectionSource}.
	 * 
	 * @return Location of the {@link OfficeSection}.
	 */
	String getSectionLocation();

	/**
	 * Creates a {@link PropertyList} for loading types.
	 * 
	 * @return New {@link PropertyList} to aid in loading types.
	 */
	PropertyList createPropertyList();

	/**
	 * <p>
	 * Loads the {@link FunctionNamespaceType}.
	 * <p>
	 * This is to enable obtaining the type information for the {@link Work} to
	 * allow reflective configuration by the {@link SectionSource}.
	 * 
	 * @param workSourceClassName
	 *            Name of the implementing {@link ManagedFunctionSource} class. May also be
	 *            an alias.
	 * @param properties
	 *            {@link PropertyList} to configure the implementing
	 *            {@link ManagedFunctionSource}.
	 * @return {@link FunctionNamespaceType} or <code>null</code> if fails to load the
	 *         {@link FunctionNamespaceType}.
	 */
	FunctionNamespaceType<?> loadWorkType(String workSourceClassName, PropertyList properties);

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