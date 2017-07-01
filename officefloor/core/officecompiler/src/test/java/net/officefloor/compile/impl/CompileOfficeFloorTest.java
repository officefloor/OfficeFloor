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
package net.officefloor.compile.impl;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests the {@link CompileOfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeFloorTest extends OfficeFrameTestCase {

	/**
	 * Ensure can compile {@link OfficeFloor}.
	 */
	public void testCompileOfficeFloor() throws Exception {

		// Reset for testing
		CompileFunction.managedObject = null;

		// Compile the OfficeFloor
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.addOfficeFloorExtension((context) -> {
			// Add managed object (as auto-wire by default)
			OfficeFloorManagedObjectSource mos = context.getOfficeFloorDeployer().addManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileManagedObject.class.getName());
			mos.addOfficeFloorManagedObject("MO", ManagedObjectScope.PROCESS);
		});
		compile.addOfficeExtension((context) -> {
			// Add managed object (as auto-wire by default)
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, DependencyManagedObject.class.getName());
			mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
		});
		compile.addSectionExtension((context) -> {
			// Add managed function
			SectionDesigner designer = context.getSectionDesigner();
			SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("NAMESPACE",
					ClassManagedFunctionSource.class.getName());
			namespace.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, CompileFunction.class.getName());
			SectionFunction function = namespace.addSectionFunction("function", "function");
			designer.link(function.getFunctionObject(DependencyManagedObject.class.getName()),
					designer.addSectionObject("OBJECT", DependencyManagedObject.class.getName()));
		});
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();

		// Ensure can invoke function
		FunctionManager function = officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.function");
		function.invokeProcess(null, null);

		// Ensure provided compile managed object
		assertNotNull("Should have auto-wired dependency", CompileFunction.managedObject.dependency);
	}

	/**
	 * Ensure can compile {@link OfficeFloor} overriding the default
	 * {@link OfficeSection}.
	 */
	public void testOverrideOfficeSection() throws Exception {

		// Reset for testing
		CompileFunction.managedObject = null;

		// Compile the OfficeFloor
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.addOfficeFloorExtension((context) -> {
			// Add managed object (as auto-wire by default)
			OfficeFloorManagedObjectSource mos = context.getOfficeFloorDeployer().addManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileManagedObject.class.getName());
			mos.addOfficeFloorManagedObject("MO", ManagedObjectScope.PROCESS);
		});
		compile.addOfficeExtension((context) -> {
			// Add managed object (as auto-wire by default)
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, DependencyManagedObject.class.getName());
			mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);

			// Override the office section
			context.overrideSection(ClassSectionSource.class, CompileFunction.class.getName());
		});
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();

		// Ensure can invoke function
		FunctionManager function = officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.function");
		function.invokeProcess(null, null);

		// Ensure provided compile managed object
		assertNotNull("Should have auto-wired dependency", CompileFunction.managedObject.dependency);
	}

	public static class CompileManagedObject {
	}

	public static class DependencyManagedObject {
		@Dependency
		private CompileManagedObject dependency;
	}

	public static class CompileFunction {

		private static DependencyManagedObject managedObject;

		public void function(DependencyManagedObject object) {
			managedObject = object;
		}
	}

}