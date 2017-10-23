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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.spi.office.AugmentedFunctionObject;
import net.officefloor.compile.spi.office.AugmentedManagedObject;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.singleton.Singleton;

/**
 * Ensure able to augment {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AugmentManagedFunctionTest extends OfficeFrameTestCase {

	/**
	 * {@link MockObject}.
	 */
	private static MockObject object = null;

	/**
	 * Ensure can augment the {@link ManagedFunction}.
	 */
	public void testAugmentManagedFunction() throws Exception {

		// Create the managed object
		MockObject mockObject = new MockObject();

		// Compile the OfficeFloor with augmented managed function
		CompileOfficeFloor compile = new CompileOfficeFloor();
		compile.office((context) -> {

			// Augment the function object
			context.getOfficeArchitect().addManagedFunctionAugmentor((augment) -> {

				// Ensure have managed function name (identify function)
				assertEquals("Incorrect managed function name", "SECTION.function", augment.getManagedFunctionName());

				// Validate can add objects for function parameters
				for (ManagedFunctionObjectType<?> type : augment.getManagedFunctionType().getObjectTypes()) {
					Class<?> objectType = type.getObjectType();
					if (objectType.isAnnotationPresent(MockAnnotation.class)) {

						// Add the managed object
						AugmentedManagedObject managedObject = augment
								.addManagedObjectSource("OBJECT", new Singleton(mockObject))
								.addAugmentedManagedObject("OBJECT", ManagedObjectScope.PROCESS);

						// Obtain the function object
						AugmentedFunctionObject object = augment.getFunctionObject(type.getObjectName());
						assertFalse("Should not be linked", object.isLinked());

						// Link managed object
						augment.link(object, managedObject);
					}
				}
			});
		});
		compile.section((context) -> {
			SectionFunctionNamespace namespace = context.getSectionDesigner().addSectionFunctionNamespace("NAMESPACE",
					ClassManagedFunctionSource.class.getName());
			namespace.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, MockFunction.class.getName());
			namespace.addSectionFunction("function", "function");
		});
		OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor();

		// Reset for test
		object = null;

		// Execute the method (with augmented object)
		FunctionManager function = officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.function");
		function.invokeProcess(null, null);

		// Should have loaded the augmented object
		assertSame("Should load augmented object", mockObject, object);
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface MockAnnotation {
	}

	@MockAnnotation
	public static class MockObject {
	}

	public static class MockFunction {
		public void function(MockObject object) {
			AugmentManagedFunctionTest.object = object;
		}
	}

}