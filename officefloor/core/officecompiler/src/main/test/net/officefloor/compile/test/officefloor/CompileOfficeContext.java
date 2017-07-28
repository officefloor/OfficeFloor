/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.test.officefloor;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Context for compiling the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileOfficeContext {

	/**
	 * Obtains the {@link OfficeArchitect}.
	 * 
	 * @return {@link OfficeArchitect}.
	 */
	OfficeArchitect getOfficeArchitect();

	/**
	 * Obtains the {@link OfficeSourceContext}.
	 * 
	 * @return {@link OfficeSourceContext}.
	 */
	OfficeSourceContext getOfficeSourceContext();

	/**
	 * Adds an {@link OfficeManagedObject} for {@link ClassManagedObjectSource}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObject}.
	 * @param managedObjectClass
	 *            {@link Class} for the {@link ClassManagedObjectSource}.
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addManagedObject(String managedObjectName, Class<?> managedObjectClass,
			ManagedObjectScope scope);

	/**
	 * Adds an {@link OfficeSection} for {@link ClassSectionSource}.
	 * 
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionClass
	 *            {@link Class} for the {@link ClassSectionSource}.
	 * @return {@link OfficeSection}.
	 */
	OfficeSection addSection(String sectionName, Class<?> sectionClass);

	/**
	 * Obtains the {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSection}.
	 */
	OfficeSection getOfficeSection();

	/**
	 * Overrides the default {@link OfficeSection}.
	 * 
	 * @param sectionSourceClass
	 *            {@link SectionSource} {@link Class}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 * @return Overridden {@link OfficeSection}.
	 */
	OfficeSection overrideSection(Class<? extends SectionSource> sectionSourceClass, String sectionLocation);

}