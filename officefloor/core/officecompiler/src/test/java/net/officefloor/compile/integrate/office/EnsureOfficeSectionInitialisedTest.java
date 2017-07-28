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
		this.doEnsureInitialised(() -> this.issues.recordIssue("UNKNOWN", SectionOutputNodeImpl.class,
				"Section Output not implemented\n\nTree = { \"name\": \"OFFICE\", \"type\": \"Office\", \"initialised\": true, \"children\": [ { \"name\": \"SECTION\", \"type\": \"Section\", \"initialised\": true, \"children\": [ { \"name\": \"function\", \"type\": \"Section Input\", \"initialised\": true }, { \"name\": \"UNKNOWN\", \"type\": \"Section Output\", \"initialised\": false }, { \"name\": \"function\", \"type\": \"Managed Function\", \"initialised\": true, \"children\": [ { \"name\": \"OBJECT\", \"type\": \"Function Object\", \"initialised\": true } ] }, { \"name\": \"OBJECT\", \"type\": \"Managed Object Source\", \"initialised\": true }, { \"name\": \"OBJECT\", \"type\": \"Managed Object\", \"initialised\": true } ] } ] }\n\n"));
	}

	/**
	 * Ensures the {@link OfficeSectionObject} is initialised.
	 */
	public void testEnsureOfficeSectionObjectInitialised() {
		this.doEnsureInitialised(() -> this.issues.recordIssue("UNKNOWN", SectionObjectNodeImpl.class,
				"Section Object not implemented\n\nTree = { \"name\": \"OFFICE\", \"type\": \"Office\", \"initialised\": true, \"children\": [ { \"name\": \"SECTION\", \"type\": \"Section\", \"initialised\": true, \"children\": [ { \"name\": \"function\", \"type\": \"Section Input\", \"initialised\": true }, { \"name\": \"UNKNOWN\", \"type\": \"Section Object\", \"initialised\": false }, { \"name\": \"function\", \"type\": \"Managed Function\", \"initialised\": true, \"children\": [ { \"name\": \"OBJECT\", \"type\": \"Function Object\", \"initialised\": true } ] }, { \"name\": \"OBJECT\", \"type\": \"Managed Object Source\", \"initialised\": true }, { \"name\": \"OBJECT\", \"type\": \"Managed Object\", \"initialised\": true } ] } ] }\n\n"));
	}

	/**
	 * Undertakes ensuring initialised.
	 * 
	 * @param recorder
	 *            {@link Runnable} to record issue.
	 */
	private void doEnsureInitialised(Runnable recorder) {

		// Record loading section type
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