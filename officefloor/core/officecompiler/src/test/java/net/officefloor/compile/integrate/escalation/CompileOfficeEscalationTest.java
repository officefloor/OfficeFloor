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

package net.officefloor.compile.integrate.escalation;

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;

/**
 * Tests compiling the {@link Office} {@link Escalation}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOfficeEscalationTest extends AbstractCompileTestCase {

	/**
	 * Test an {@link OfficeEscalation}.
	 */
	public void testSimpleEscalation() {

		// Record creating section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "INPUT").linkParameter(0, Throwable.class);
		this.record_officeBuilder_addEscalation(Exception.class, "SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Test an multiple {@link OfficeEscalation} instances.
	 */
	public void testMultipleEscalationOrdering() {

		// Record creating section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "INPUT").linkParameter(0, Throwable.class);
		this.record_officeBuilder_addEscalation(IOException.class, "SECTION.INPUT");
		this.record_officeBuilder_addEscalation(SQLException.class, "SECTION.INPUT");
		this.record_officeBuilder_addEscalation(Exception.class, "SECTION.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Class for {@link ClassManagedFunctionSource}.
	 */
	public static class EscalationClass {
		public void handle(Throwable parameter) {
		}
	}

}
