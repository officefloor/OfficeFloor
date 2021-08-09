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

import net.officefloor.compile.impl.structure.SectionObjectNodeImpl;
import net.officefloor.compile.impl.structure.SectionOutputNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Ensures the {@link OfficeSection} instances are correctly initialised to the
 * {@link Office} specification of them.
 * 
 * @author Daniel Sagenschneider
 */
public class EnsureOfficeSectionInitialisedTest extends AbstractCompileTestCase {

	/**
	 * Ensure the {@link OfficeSectionInput} is initialised.
	 */
	public void testEnsureOfficeSectionOutputInitialised() {
		this.doEnsureInitialised(() -> this.issues.recordIssue("OFFICE.SECTION.UNKNOWN", SectionOutputNodeImpl.class,
				"Section Output not implemented\n\nTree = { \"name\": \"OFFICE\", \"type\": \"Office\", \"initialised\": true, \"children\": [ { \"name\": \"SECTION\", \"type\": \"Section\", \"initialised\": true, \"children\": [ { \"name\": \"function\", \"type\": \"Section Input\", \"initialised\": true }, { \"name\": \"UNKNOWN\", \"type\": \"Section Output\", \"initialised\": false }, { \"name\": \"function\", \"type\": \"Managed Function\", \"initialised\": true, \"children\": [ { \"name\": \"OBJECT\", \"type\": \"Function Object\", \"initialised\": true } ] }, { \"name\": \"OBJECT\", \"type\": \"Managed Object Source\", \"initialised\": true }, { \"name\": \"OBJECT\", \"type\": \"Managed Object\", \"initialised\": true } ] } ] }\n\n"));
	}

	/**
	 * Ensures the {@link OfficeSectionObject} is initialised.
	 */
	public void testEnsureOfficeSectionObjectInitialised() {
		this.doEnsureInitialised(() -> this.issues.recordIssue("OFFICE.SECTION.UNKNOWN", SectionObjectNodeImpl.class,
				"Section Object not implemented\n\nTree = { \"name\": \"OFFICE\", \"type\": \"Office\", \"initialised\": true, \"children\": [ { \"name\": \"SECTION\", \"type\": \"Section\", \"initialised\": true, \"children\": [ { \"name\": \"function\", \"type\": \"Section Input\", \"initialised\": true }, { \"name\": \"UNKNOWN\", \"type\": \"Section Object\", \"initialised\": false }, { \"name\": \"function\", \"type\": \"Managed Function\", \"initialised\": true, \"children\": [ { \"name\": \"OBJECT\", \"type\": \"Function Object\", \"initialised\": true } ] }, { \"name\": \"OBJECT\", \"type\": \"Managed Object Source\", \"initialised\": true }, { \"name\": \"OBJECT\", \"type\": \"Managed Object\", \"initialised\": true } ] } ] }\n\n"));
	}

	/**
	 * Undertakes ensuring initialised.
	 * 
	 * @param recorder {@link Runnable} to record issue.
	 */
	private void doEnsureInitialised(Runnable recorder) {

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record issue
		recorder.run();

		// Should not compiler
		this.compile(false);
	}

	/**
	 * Simple {@link ClassSectionSource} {@link Class}.
	 */
	public static class CompileSectionClass {
		public void function() {
		}
	}

}
