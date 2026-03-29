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

package net.officefloor.compile.integrate.office;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;

/**
 * Ensures order of execution (objects, functions, then escalations).
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutionOrderTest extends OfficeFrameTestCase {

	/**
	 * Ensure order.
	 */
	public void testOrder() throws Exception {
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		Closure<Boolean> isObjectsExplored = new Closure<>(false);
		Closure<Boolean> isFunctionsExplored = new Closure<>(false);
		Closure<Boolean> isEscalationsExplored = new Closure<>(false);
		Closure<Boolean> isCompletionExplored = new Closure<>(false);
		compiler.office((context) -> {
			// Add out of order to confirm order
			OfficeArchitect office = context.getOfficeArchitect();

			// Add completion
			office.addOfficeCompletionExplorer(() -> {
				assertTrue("Objects explored before complete", isObjectsExplored.value);
				assertTrue("Functions explore before complete", isFunctionsExplored.value);
				assertFalse("Escalations explore before complete", isCompletionExplored.value);
				isCompletionExplored.value = true;
			});

			// Add escalation (to be third)
			OfficeEscalation escalation = office.addOfficeEscalation(Exception.class.getName());
			office.link(escalation, context.addSection("HANDLER", MockSection.class).getOfficeSectionInput("service"));
			office.addOfficeEscalationExplorer((explore) -> {
				assertTrue("Objects explored before escalations", isObjectsExplored.value);
				assertTrue("Functions explore before escalations", isFunctionsExplored.value);
				assertFalse("Copmletion explore after escalations", isCompletionExplored.value);
				isEscalationsExplored.value = true;
			});

			// Add objects (to be first)
			OfficeManagedObject managedObject = Singleton.load(office, "OBJECT");
			managedObject.addExecutionExplorer((explore) -> {
				assertFalse("Functions explored after objects", isFunctionsExplored.value);
				assertFalse("Escalations explored after objects", isEscalationsExplored.value);
				assertFalse("Copmletion explore after objects", isCompletionExplored.value);
				isObjectsExplored.value = true;
			});

			// Add functions (to be second)
			OfficeSectionInput input = context.addSection("INPUT", MockSection.class).getOfficeSectionInput("service");
			input.addExecutionExplorer((explore) -> {
				assertTrue("Objects explored before functions", isObjectsExplored.value);
				assertFalse("Escalations explored after functions", isEscalationsExplored.value);
				assertFalse("Copmletion explore after functions", isCompletionExplored.value);
				isFunctionsExplored.value = true;
			});
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			assertTrue("Should execute completion (and subsequently objects, functions and escalations)",
					isCompletionExplored.value);
		}
	}

	public static class MockSection {
		public void service() {
			// no operation
		}
	}
}
