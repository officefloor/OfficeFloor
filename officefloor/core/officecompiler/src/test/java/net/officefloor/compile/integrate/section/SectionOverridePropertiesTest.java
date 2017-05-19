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
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Ensure able to override the {@link PropertyList} for various aspects of the
 * {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionOverridePropertiesTest extends AbstractCompileTestCase {

	/**
	 * Ensure can override {@link Property} for the {@link SubSection}.
	 */
	public void testOverrideSubSectionProperties() {

		// Enables override of properties
		this.enableOverrideProperties();

		// Record creating the section types
		this.issues.recordCaptureIssues(false);

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION.OVERRIDE_SUB_SECTION", "function");

		// Compile the OfficeFloor
		this.compile(true);
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

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.OVERRIDE_MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", CompileManagedObject.class.getName(), "additional",
				"another");
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure can override {@link Property} for the
	 * {@link ManagedFunctionSource}.
	 */
	public void testOverrideManagedFunctionSourceProperties() {

		// Enables override of properties
		this.enableOverrideProperties();

		// Record creating the section types
		this.issues.recordCaptureIssues(false);

		// Record the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "function");

		// Compile the OfficeFloor
		this.compile(true);
	}

	public static class CompileManagedObject {
	}

	public static class CompileFunction {
		public void function() {
		}
	}

	@TestSource
	public static class TestSectionSource extends AbstractSectionSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty("value");
		}

		@Override
		public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {
			assertEquals("Property should be overridden", "section", context.getProperty("value"));
			assertEquals("Should have additional property", "another", context.getProperty("additional"));

			// Provide function (to ensure is loaded)
			SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("NAMESPACE",
					ClassManagedFunctionSource.class.getName());
			namespace.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, CompileFunction.class.getName());
			namespace.addSectionFunction("function", "function");
		}
	}

}