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
package net.officefloor.compile.test.work;

import java.util.List;

import junit.framework.TestCase;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.work.WorkLoaderImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.WorkLoader;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.compile.test.issues.FailCompilerIssues;
import net.officefloor.frame.api.execute.Work;

/**
 * Utility class for testing a {@link WorkSource}.
 * 
 * @author Daniel
 */
public class WorkLoaderUtil {

	/**
	 * Validates the {@link WorkSourceSpecification} for the {@link WorkSource}.
	 * 
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <W extends Work, WS extends WorkSource<W>> PropertyList validateSpecification(
			Class<WS> workSourceClass, String... propertyNameLabels) {

		// Create the work loader
		WorkLoader workLoader = new WorkLoaderImpl("TEST", "TEST");

		// Load the specification
		PropertyList propertyList = workLoader.loadSpecification(
				workSourceClass, new FailCompilerIssues());

		// Verify the properties
		List<Property> properties = propertyList.getPropertyList();
		TestCase.assertEquals("Incorrect number of properties",
				propertyNameLabels.length / 2, properties.size());
		for (int i = 0; i < propertyNameLabels.length; i += 2) {
			Property property = properties.get(i / 2);
			String name = propertyNameLabels[i];
			String label = propertyNameLabels[i + 1];
			TestCase.assertEquals("Incorrect name for property " + i, name,
					property.getName());
			TestCase.assertEquals("Incorrect label for property " + i, label,
					property.getLabel());
		}

		// Return the property list
		return propertyList;
	}

	/**
	 * Convenience method that loads the {@link WorkType} by obtaining the
	 * {@link ClassLoader} from the {@link WorkSource} class.
	 * 
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link WorkSource}.
	 * @return Loaded {@link WorkType}.
	 */
	public static <W extends Work, WS extends WorkSource<W>> WorkType<W> loadWork(
			Class<WS> workSourceClass, String... propertyNameValues) {

		// Obtain the class loader
		ClassLoader classLoader = workSourceClass.getClassLoader();

		// Return the loaded work
		return loadWork(workSourceClass, classLoader, propertyNameValues);
	}

	/**
	 * Convenience method that loads the {@link WorkType}.
	 * 
	 * @param workSourceClass
	 *            {@link WorkSource} class.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link WorkSource}.
	 * @return Loaded {@link WorkType}.
	 */
	public static <W extends Work, WS extends WorkSource<W>> WorkType<W> loadWork(
			Class<WS> workSourceClass, ClassLoader classLoader,
			String... propertyNameValues) {

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Create the work loader
		WorkLoader workLoader = new WorkLoaderImpl("TEST", "TEST");

		// Return the loaded work
		return workLoader.loadWork(workSourceClass, propertyList, classLoader,
				new FailCompilerIssues());
	}

	/**
	 * All access via static methods.
	 */
	private WorkLoaderUtil() {
	}
}
