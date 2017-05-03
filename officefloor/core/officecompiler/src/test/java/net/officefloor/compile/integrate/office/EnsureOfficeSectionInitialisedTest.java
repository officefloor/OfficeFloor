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

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.spi.office.OfficeSection;
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
	 * Ensures the {@link OfficeSectionObject} is initialised.
	 */
	public void testEnsureOfficeSectionObjectInitialised() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record issue as object not initialised
		this.issues.recordIssue("OFFICE.SECTION", SectionObjectNode.class, "Object not implemented by Section");

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addSectionClassFunction("OFFICE", "SECTION", CompileSectionClass.class, "function");

		// Should not compile
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