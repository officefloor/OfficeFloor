/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.test.managedobject;

import net.officefloor.compile.impl.managedobject.ManagedObjectLoaderImpl;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;

/**
 * Utility class for testing the {@link ManagedObjectSource}.
 * 
 * @author Daniel
 */
public class ManagedObjectLoaderUtil {

	/**
	 * Validates the {@link ManagedObjectSourceSpecification} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> PropertyList validateSpecification(
			Class<S> managedObjectSourceClass, String... propertyNameLabels) {

		// Create the managed object loader
		ManagedObjectLoader managedObjectLoader = new ManagedObjectLoaderImpl(
				LocationType.OFFICE_FLOOR, null, "TEST");

		// Load the specification
		PropertyList propertyList = managedObjectLoader.loadSpecification(
				managedObjectSourceClass, new FailCompilerIssues());

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList,
				propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * All access is via static methods.
	 */
	private ManagedObjectLoaderUtil() {
	}

}