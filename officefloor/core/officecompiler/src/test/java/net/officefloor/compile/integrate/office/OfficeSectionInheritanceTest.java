/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.integrate.office;

import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Tests the inheritance of an {@link OfficeSection} by another
 * {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSectionInheritanceTest extends AbstractCompileTestCase {

	/**
	 * Ensure can inherit {@link Flow} link.
	 */
	public void testInheritFlowLink() throws Exception {

		// Construct the Office
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((extender, context) -> {

			// Create the parent section
			OfficeSection parent = extender.addOfficeSection("PARENT", ClassSectionSource.class.getName(),
					CompileParent.class.getName());

			// Create the handler for the parent
			OfficeSection handler = extender.addOfficeSection("HANDLER", ClassSectionSource.class.getName(),
					CompileHandler.class.getName());
			extender.link(parent.getOfficeSectionOutput("doFlow"), handler.getOfficeSectionInput("handle"));

			// Create the child section
			OfficeSection child = extender.addOfficeSection("CHILD", ClassSectionSource.class.getName(),
					CompileChild.class.getName());

			// Child to inherit links from parent
			child.setSuperOfficeSection(parent);
		});

		// Invoke the child to ensure inherit link
		CompileHandler.handleParameter = null;
		officeFloor.getOffice("OFFICE").getFunctionManager("CHILD.child").invokeProcess(null, null);
		assertEquals("Should be child invoking handler", "CHILD", CompileHandler.handleParameter);
	}

	/**
	 * Ensure can override a {@link Flow} link.
	 */
	public void testOverrideFlowLink() throws Exception {

		// Construct the Office
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((extender, context) -> {

			// Create the parent section
			OfficeSection parent = extender.addOfficeSection("PARENT", ClassSectionSource.class.getName(),
					CompileParent.class.getName());

			// Create the handler for the parent
			OfficeSection handler = extender.addOfficeSection("HANDLER", ClassSectionSource.class.getName(),
					CompileHandler.class.getName());
			extender.link(parent.getOfficeSectionOutput("doFlow"), handler.getOfficeSectionInput("handle"));

			// Create the child section
			OfficeSection child = extender.addOfficeSection("CHILD", ClassSectionSource.class.getName(),
					CompileChild.class.getName());

			// Child to inherit links from parent
			child.setSuperOfficeSection(parent);

			// Have child override parent flow
			OfficeSection overrideHandler = extender.addOfficeSection("CHILD_HANDLER",
					ClassSectionSource.class.getName(), CompileOverrideHandler.class.getName());
			extender.link(child.getOfficeSectionOutput("doFlow"), overrideHandler.getOfficeSectionInput("handle"));
		});

		// Invoke the child to ensure inherit link
		CompileHandler.handleParameter = null;
		CompileOverrideHandler.handleParameter = null;
		officeFloor.getOffice("OFFICE").getFunctionManager("CHILD.child").invokeProcess(null, null);
		assertNull("Should not follow parent link", CompileHandler.handleParameter);
		assertEquals("Should be child invoking override handler", "CHILD", CompileOverrideHandler.handleParameter);
	}

	/**
	 * Ensure issue if cyclic inheritance of {@link OfficeSection} instances.
	 */
	public void testIssueIfCyclicInheritance() throws Exception {

		// Ensure issue if cyclic inheritance
		MockCompilerIssues issues = new MockCompilerIssues(this);

		// Record creating the sections
		issues.recordCaptureIssues(false);
		issues.recordCaptureIssues(false);
		issues.recordCaptureIssues(false);
		issues.recordCaptureIssues(false);

		// Ensure issue if cyclic inheritance
		issues.recordIssue("OFFICE.CHILD", SectionNodeImpl.class,
				"Cyclic section inheritance hierarchy ( OFFICE.PARENT : OFFICE.CHILD : OFFICE.PARENT : ... )");

		// Test
		this.replayMockObjects();

		// Handle compile
		CompileOffice compiler = new CompileOffice();
		compiler.getOfficeFloorCompiler().setCompilerIssues(issues);

		// Construct the Office
		OfficeFloor officeFloor = compiler.compileOffice((extender, context) -> {

			// Create the parent section
			OfficeSection parent = extender.addOfficeSection("PARENT", ClassSectionSource.class.getName(),
					CompileParent.class.getName());

			// Create the child section
			OfficeSection child = extender.addOfficeSection("CHILD", ClassSectionSource.class.getName(),
					CompileChild.class.getName());

			// Flows not handled, so must search inheritance hierarchy
			parent.getOfficeSectionOutput("doFlow");
			child.getOfficeSectionOutput("doFlow");

			// Create cyclic inheritance
			child.setSuperOfficeSection(parent);
			parent.setSuperOfficeSection(child);
		});
		assertNull("Should not compile OfficeFloor", officeFloor);

		// Verify
		this.verifyMockObjects();
	}

	@FlowInterface
	public static interface CompileFlows {
		void doFlow(String parameter);
	}

	public static class CompileParent {
		public void parent(CompileFlows flows) {
			flows.doFlow("PARENT");
		}
	}

	public static class CompileChild {
		public void child(CompileFlows flows) {
			flows.doFlow("CHILD");
		}
	}

	public static class CompileHandler {
		public static String handleParameter = null;

		public void handle(@Parameter String parameter) {
			handleParameter = parameter;
		}
	}

	public static class CompileOverrideHandler {
		public static String handleParameter = null;

		public void handle(@Parameter String parameter) {
			handleParameter = parameter;
		}
	}

}
