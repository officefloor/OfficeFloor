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

package net.officefloor.compile.run.managedobject;

import net.officefloor.compile.run.AbstractRunTestCase;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * Ensure can run an {@link InputManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class RunInputManagedObjectTest extends AbstractRunTestCase {

	/**
	 * Ensure can use {@link InputManagedObject}.
	 */
	public void testInputManagedObject() throws Exception {

		// Open the OfficeFloor
		OfficeFloor officeFloor = this.open();

		// Obtain the method
		FunctionManager function = officeFloor.getOffice("OFFICE").getFunctionManager("SECTION.function");

		// Ensure can inject managed object
		CompileSection.injectedObject = null;
		function.invokeProcess(null, null);
		assertNotNull("Must have injected object", CompileSection.injectedObject);
		DependencyManagedObject object = CompileSection.injectedObject;
		assertNotNull("Should have dependency", object.dependency);

		// Ensure can trigger process with input object
		CompileSection.injectedObject = null;
		object.flows.doProcess();
		assertNotNull("Must have input object", CompileSection.injectedObject);
		assertNotNull("Should have input dependency", CompileSection.injectedObject.dependency);
	}

	public static class CompileManagedObject {
	}

	public static class DependencyManagedObject {
		@Dependency
		private CompileManagedObject dependency;

		@FlowInterface
		public static interface Flows {
			void doProcess();
		}

		private Flows flows;
	}

	public static class CompileSection {

		private static DependencyManagedObject injectedObject = null;

		public void function(DependencyManagedObject object) {
			injectedObject = object;
		}
	}

	/**
	 * Ensure can use multiple {@link ManagedObjectSource} instances for
	 * {@link InputManagedObject}.
	 */
	public void testMultipleInputManagedObjects() throws Exception {

		// Open the OfficeFloor
		OfficeFloor officeFloor = this.open();

		// Obtain the functions
		Office office = officeFloor.getOffice("OFFICE");
		FunctionManager functionOne = office.getFunctionManager("SECTION_ONE.function");
		FunctionManager functionTwo = office.getFunctionManager("SECTION_TWO.function");

		// Obtain the two input objects (for invoking flows)
		InputSection.inputObject = null;
		functionOne.invokeProcess(null, null);
		InputManagedObject one = InputSection.inputObject;
		assertNotNull("Must have first object", one);
		InputSection.inputObject = null;
		functionTwo.invokeProcess(null, null);
		InputManagedObject two = InputSection.inputObject;
		assertNotNull("Must have second object", two);

		// Ensure inject first object
		InputSection.inputObject = null;
		one.flows.doProcess();
		assertEquals("Incorrect first input object", "ONE", InputSection.inputObject.dependency.getText());

		// Ensure inject second object
		InputSection.inputObject = null;
		two.flows.doProcess();
		assertEquals("Incorrect second input object", "TWO", InputSection.inputObject.dependency.getText());

		// Ensure bind first object (when not input)
		InputSection.inputObject = null;
		office.getFunctionManager("INPUT_SECTION.function").invokeProcess(null, null);
		assertEquals("Incorrect bound object", "ONE", InputSection.inputObject.dependency.getText());
	}

	public static interface DependentObject {
		String getText();
	}

	public static class DependentOne implements DependentObject {
		@Override
		public String getText() {
			return "ONE";
		}
	}

	public static class DependentTwo implements DependentObject {
		@Override
		public String getText() {
			return "TWO";
		}
	}

	public static class InputManagedObject {
		@Dependency
		private DependentObject dependency;

		@FlowInterface
		public static interface Flows {
			void doProcess();
		}

		private Flows flows;
	}

	public static class InputSection {

		private static InputManagedObject inputObject = null;

		public void function(InputManagedObject object) {
			inputObject = object;
		}
	}

}
