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
package net.officefloor.compile.integrate.office;

import net.officefloor.autowire.AutoWire;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.extension.AutoWireOfficeExtensionService;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.section.clazz.SectionClassManagedObjectSource;

/**
 * Tests the {@link AutoWire} of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeTest extends AbstractCompileTestCase {

	/**
	 * Ensure can auto-wire an {@link OfficeManagedObject}.
	 */
	public void testAutoWireOfficeManagedObject() {

		AutoWireOfficeExtensionService.enableAutoWireObjects();
		
		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "function");
		function.linkManagedObject(0, "OFFICE.SECTION.OBJECT", CompileSectionClass.class);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.OBJECT", SectionClassManagedObjectSource.class,
				0, SectionClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileSectionClass.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.OBJECT", "OFFICE.SECTION.OBJECT");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.SECTION.OBJECT", "OFFICE.SECTION.OBJECT");

		// Auto-wire
		function.linkManagedObject(0, "OFFICE.SECTION.MANAGED_OBJECT", CompileManagedObject.class);

		this.compile(true);
	}

	public static class CompileSectionClass {
		public void function(CompileManagedObject object) {
		}
	}

	public static class CompileManagedObject {
	}
}