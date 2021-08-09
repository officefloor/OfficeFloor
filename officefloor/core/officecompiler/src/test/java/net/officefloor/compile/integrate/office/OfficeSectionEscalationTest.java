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

import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Ensure not required to configure {@link Escalation} {@link SectionOutput}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSectionEscalationTest extends OfficeFrameTestCase {

	/**
	 * Ensure can link the {@link Escalation} {@link SectionOutput}.
	 */
	public void testLinkEscalationOutput() throws Exception {

		// Construct the office
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((extender, context) -> {

			// Create section that escalates
			OfficeSection section = extender.addOfficeSection("FAIL", ClassSectionSource.class.getName(),
					EscalatingSection.class.getName());

			// Link escalation flow for handling
			OfficeSection handler = extender.addOfficeSection("HANDLE", ClassSectionSource.class.getName(),
					HandleSection.class.getName());
			extender.link(section.getOfficeSectionOutput(Exception.class.getName()),
					handler.getOfficeSectionInput("handle"));
		});
		FunctionManager function = officeFloor.getOffice("OFFICE").getFunctionManager("FAIL.escalate");

		// Ensure section handles escalation
		HandleSection.failure = null;
		function.invokeProcess(null, null);
		assertEquals("Incorrect handling of escalation", "TEST", HandleSection.failure.getMessage());
	}

	/**
	 * Ensure not required to configure {@link Escalation}
	 * {@link SectionOutput}.
	 */
	public void testEscalationHandledByOfficeEscalation() throws Exception {

		// Construct the office
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((extender, context) -> {

			// Should not be required to link escalation output
			extender.addOfficeSection("FAIL", ClassSectionSource.class.getName(), EscalatingSection.class.getName());

			// Configure Office exception handling
			OfficeEscalation escalation = extender.addOfficeEscalation(Exception.class.getName());
			OfficeSection handler = extender.addOfficeSection("HANDLE", ClassSectionSource.class.getName(),
					HandleSection.class.getName());
			extender.link(escalation, handler.getOfficeSectionInput("handle"));
		});
		FunctionManager function = officeFloor.getOffice("OFFICE").getFunctionManager("FAIL.escalate");

		// Ensure escalation handled by office
		HandleSection.failure = null;
		function.invokeProcess(null, null);
		assertEquals("Incorrect handling of escalation", "TEST", HandleSection.failure.getMessage());
	}

	public static class EscalatingSection {
		public void escalate() throws Exception {
			throw new Exception("TEST");
		}
	}

	public static class HandleSection {

		private static Exception failure = null;

		public void handle(@Parameter Exception exception) {
			failure = exception;
		}
	}

}
