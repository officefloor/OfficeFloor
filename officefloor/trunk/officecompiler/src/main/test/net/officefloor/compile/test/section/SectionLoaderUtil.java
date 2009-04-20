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
package net.officefloor.compile.test.section;

import net.officefloor.compile.impl.section.SectionLoaderImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.test.issues.FailCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;

/**
 * Utility class for testing a {@link SectionLoader}.
 * 
 * @author Daniel
 */
public class SectionLoaderUtil {

	/**
	 * Validates the {@link SectionSourceSpecification} for the
	 * {@link SectionSource}.
	 * 
	 * @param sectionSourceClass
	 *            {@link SectionSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <S extends SectionSource> PropertyList validateSpecification(
			Class<S> sectionSourceClass, String... propertyNameLabels) {

		// Create the section loader
		SectionLoader sectionLoader = new SectionLoaderImpl();

		// Load the specification
		PropertyList propertyList = sectionLoader.loadSpecification(
				sectionSourceClass, new FailCompilerIssues());

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList,
				propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * All access via static methods.
	 */
	private SectionLoaderUtil() {
	}

}