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
package net.officefloor.compile.integrate.section;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SubSection;

/**
 * Ensure able to override the {@link PropertyList} for various aspects of the
 * {@link OfficeSubSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class SubSectionOverridePropertiesTest extends AbstractCompileTestCase {

	/**
	 * Ensure can override {@link Property} for the {@link SubSection}.
	 */
	public void testOverrideSubSectionProperties() {
		fail("TODO implement");
	}

	/**
	 * Ensure can override {@link Property} for the
	 * {@link SectionManagedObjectSource}.
	 */
	public void testOverrideManagedObjectSourceProperties() {

		// Enables override of properties
		this.enableOverrideProperties();

		// Record creating the section types
		this.issues.recordCaptureIssues(false);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override {@link Property} for the
	 * {@link ManagedFunctionSource}.
	 */
	public void testOverrideManagedFunctionSourceProperties() {
		fail("TODO implement");
	}

	public static class CompileManagedObject {
	}

}