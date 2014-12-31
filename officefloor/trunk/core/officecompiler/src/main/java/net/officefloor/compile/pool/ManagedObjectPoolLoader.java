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
package net.officefloor.compile.pool;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceProperty;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceSpecification;

/**
 * Loads the {@link ManagedObjectPoolType} from the
 * {@link ManagedObjectPoolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedObjectPoolSourceSpecification} for the
	 * {@link ManagedObjectPoolSource}.
	 * 
	 * @param managedObjectPoolSourceClass
	 *            Class of the {@link ManagedObjectPoolSource}.
	 * @return {@link PropertyList} of the
	 *         {@link ManagedObjectPoolSourceProperty} instances of the
	 *         {@link ManagedObjectPoolSourceSpecification} or <code>null</code>
	 *         if issues, which are reported to the {@link CompilerIssues}.
	 */
	<PS extends ManagedObjectPoolSource> PropertyList loadSpecification(
			Class<PS> managedObjectPoolSourceClass);

	/**
	 * Loads and returns the {@link ManagedObjectPoolType} sourced from the
	 * {@link ManagedObjectPoolSource}.
	 * 
	 * @param managedObjectPoolSourceClass
	 *            Class of the {@link ManagedObjectPoolSource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link ManagedObjectPoolType}.
	 * @return {@link ManagedObjectPoolType} or <code>null</code> if issues,
	 *         which is reported to the {@link CompilerIssues}.
	 */
	<PS extends ManagedObjectPoolSource> ManagedObjectPoolType loadManagedObjectPoolType(
			Class<PS> managedObjectPoolSourceClass, PropertyList propertyList);

}