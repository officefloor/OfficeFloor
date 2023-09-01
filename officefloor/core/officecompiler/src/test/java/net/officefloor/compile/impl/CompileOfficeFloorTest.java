/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.test.officefloor.CompileOfficeExtension;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileOfficeFloorExtension;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests the {@link CompileOfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeFloorTest {

	/**
	 * Ensure can compile {@link OfficeFloor}.
	 */
	@Test
	public void compileOfficeFloor() throws Exception {

		// Reset for testing
		CompileFunction.managedObject = null;

		// Compile the OfficeFloor
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {
			// Add managed object (as auto-wire by default)
			OfficeFloorManagedObjectSource mos = context.getOfficeFloorDeployer().addManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileManagedObject.class.getName());
			mos.addOfficeFloorManagedObject("MO", ManagedObjectScope.PROCESS);
		});
		compile.office((context) -> {
			// Add managed object (as auto-wire by default)
			OfficeManagedObjectSource mos = context.getOfficeArchitect().addOfficeManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, DependencyManagedObject.class.getName());
			mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
		});
		compile.section((context) -> {
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
		assertNotNull(CompileFunction.managedObject.dependency, "Should have auto-wired dependency");
	}

	/**
	 * Ensure can compile {@link OfficeFloor} overriding the default
	 * {@link OfficeSection}.
	 */
	@Test
	public void overrideOfficeSection() throws Exception {

		// Reset for testing
		CompileFunction.managedObject = null;

		// Compile the OfficeFloor
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {
			// Add managed object (as auto-wire by default)
			OfficeFloorManagedObjectSource mos = context.getOfficeFloorDeployer().addManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileManagedObject.class.getName());
			mos.addOfficeFloorManagedObject("MO", ManagedObjectScope.PROCESS);
		});
		compile.office((context) -> {
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
		assertNotNull(CompileFunction.managedObject.dependency, "Should have auto-wired dependency");
	}

	/**
	 * Ensure can specify {@link Class} for {@link ClassSectionSource}.
	 */
	@Test
	public void classSimplify() throws Exception {

		// Reset for testing
		CompileFunction.managedObject = null;

		// Compile the OfficeFloor
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor((context) -> {
			context.addManagedObject("MO", CompileManagedObject.class, ManagedObjectScope.PROCESS);
		});
		compile.office((context) -> {
			context.addManagedObject("MO", DependencyManagedObject.class, ManagedObjectScope.THREAD);
			context.addSection("TEST", CompileFunction.class);
		});
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();

		// Ensure can invoke function
		FunctionManager function = officeFloor.getOffice("OFFICE").getFunctionManager("TEST.function");
		function.invokeProcess(null, null);

		// Ensure provided compile managed object
		assertNotNull(CompileFunction.managedObject.dependency, "Should have auto-wired dependency");
	}

	/**
	 * Ensure can load extension wrappers.
	 */
	@Test
	public void extensionWrappers() throws Exception {

		// Reset for testing
		CompileFunction.managedObject = null;

		// Compile the OfficeFloor
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.officeFloor(CompileOfficeFloorExtension.of((officeFloor, context) -> {
			// Add managed object (as auto-wire by default)
			OfficeFloorManagedObjectSource mos = officeFloor.addManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, CompileManagedObject.class.getName());
			mos.addOfficeFloorManagedObject("MO", ManagedObjectScope.PROCESS);
		}));
		compile.office(CompileOfficeExtension.of((office, context) -> {
			// Add managed object (as auto-wire by default)
			OfficeManagedObjectSource mos = office.addOfficeManagedObjectSource("MOS",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, DependencyManagedObject.class.getName());
			mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
		}));
		compile.section((context) -> {
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
		assertNotNull(CompileFunction.managedObject.dependency, "Should have auto-wired dependency");
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
