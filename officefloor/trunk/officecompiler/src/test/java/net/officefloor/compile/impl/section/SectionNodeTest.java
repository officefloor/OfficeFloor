/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.section;

import java.sql.Connection;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.source.OfficeSection;
import net.officefloor.compile.spi.section.SectionBuilder;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkSourceSpecification;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link SectionBuilder}.
 * 
 * @author Daniel
 */
public class SectionNodeTest extends OfficeFrameTestCase {

	/**
	 * Location of the {@link OfficeSection} being built.
	 */
	private static final String SECTION_LOCATION = "SECTION_LOCATION";

	/**
	 * {@link CompilerIssues}.
	 */
	private CompilerIssues issues = this.createMock(CompilerIssues.class);

	/**
	 * {@link SectionBuilder} to be tested.
	 */
	private SectionNode node = new SectionNode(SECTION_LOCATION, this.issues);

	/**
	 * Tests adding a {@link SectionInput}.
	 */
	public void testAddSectionInput() {
		// Add two different inputs verifying details
		this.replayMockObjects();
		SectionInput input = this.node
				.addInput("INPUT", String.class.getName());
		assertNotNull("Must have input", input);
		assertEquals("Incorrect input name", "INPUT", input
				.getSectionInputName());
		assertNotSame("Should obtain another input", input, this.node.addInput(
				"ANOTHER", Integer.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link SectionInput} twice.
	 */
	public void testAddSectionInputTwice() {

		// Record issue in adding the input twice
		this.record_issue("Input INPUT already added");

		// Add the input twice
		this.replayMockObjects();
		SectionInput inputFirst = this.node.addInput("INPUT", String.class
				.getName());
		SectionInput inputSecond = this.node.addInput("INPUT", Integer.class
				.getName());
		this.verifyMockObjects();

		// Should be the same input
		assertEquals("Should be same input on adding twice", inputFirst,
				inputSecond);
	}

	/**
	 * Tests adding a {@link SectionOutput}.
	 */
	public void testSectionOutput() {
		// Add two different outputs verifying details
		this.replayMockObjects();
		SectionOutput output = this.node.addOutput("OUTPUT", Double.class
				.getName(), false);
		assertNotNull("Must have output", output);
		assertEquals("Incorrect output name", "OUTPUT", output
				.getSectionOutputName());
		assertNotSame("Should obtain another output", output, this.node
				.addOutput("ANOTHER", Exception.class.getName(), true));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link SectionOutput} twice.
	 */
	public void testAddSectionOutputTwice() {

		// Record issue in adding the input twice
		this.record_issue("Output OUTPUT already added");

		// Add the output twice
		this.replayMockObjects();
		SectionOutput outputFirst = this.node.addOutput("OUTPUT", Object.class
				.getName(), false);
		SectionOutput outputSecond = this.node.addOutput("OUTPUT",
				Exception.class.getName(), true);
		this.verifyMockObjects();

		// Should be the same output
		assertEquals("Should be same output on adding twice", outputFirst,
				outputSecond);
	}

	/**
	 * Tests obtaining a {@link SectionObject}.
	 */
	public void testSectionObject() {
		// Add two different outputs verifying details
		this.replayMockObjects();
		SectionObject object = this.node.addObject("OBJECT", Connection.class
				.getName());
		assertNotNull("Must have object", object);
		assertEquals("Incorrect object name", "OBJECT", object
				.getSectionObjectName());
		assertNotSame("Should obtain another object", object, this.node
				.addObject("ANOTHER", Object.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link SectionObject} twice.
	 */
	public void testAddSectionObjectTwice() {

		// Record issue in adding the object twice
		this.record_issue("Object OBJECT already added");

		// Add the object twice
		this.replayMockObjects();
		SectionObject objectFirst = this.node.addObject("OBJECT",
				Connection.class.getName());
		SectionObject objectSecond = this.node.addObject("OBJECT",
				Exception.class.getName());
		this.verifyMockObjects();

		// Should be the same object
		assertEquals("Should be same object on adding twice", objectFirst,
				objectSecond);
	}

	/**
	 * Ensure can add a {@link SectionWork}.
	 */
	public void testAddSectionWork() {
		// Add two different section works verifying details
		this.replayMockObjects();
		SectionWork work = this.node.addWork("WORK", NotUseWorkSource.class
				.getName());
		assertNotNull("Must have section work", work);
		assertEquals("Incorrect section work name", "WORK", work
				.getSectionWorkName());
		assertNotSame("Should obtain another section work", work, this.node
				.addWork("ANOTHER", NotUseWorkSource.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionWork} by same name twice.
	 */
	public void testAddSectionWorkTwice() {

		// Record issue in adding the section work twice
		this.record_issue("Section work WORK already added");

		// Add the section work twice
		this.replayMockObjects();
		SectionWork workFirst = this.node.addWork("WORK",
				new NotUseWorkSource());
		SectionWork workSecond = this.node.addWork("WORK",
				new NotUseWorkSource());
		this.verifyMockObjects();

		// Should be the same section work
		assertEquals("Should be same section work on adding twice", workFirst,
				workSecond);
	}

	/**
	 * Ensure can add a {@link SectionTask}.
	 */
	public void testAddSectionTask() {
		// Add two different section tasks verifying details
		this.replayMockObjects();
		SectionWork work = this.node.addWork("WORK", NotUseWorkSource.class
				.getName());
		SectionTask task = work.addTask("TASK", "TYPE");
		assertNotNull("Must have section task", task);
		assertEquals("Incorrect section task name", "TASK", task
				.getSectionTaskName());
		assertNotSame("Should obtain another section task", task, work.addTask(
				"ANOTHER", "TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionTask} by same name twice.
	 */
	public void testAddSectionTaskTwice() {

		// Record issue in adding the section task twice
		this.issues.addIssue(LocationType.SECTION, SECTION_LOCATION,
				AssetType.WORK, "WORK", "Section task TASK already added");

		// Add the section task twice by same work
		this.replayMockObjects();
		SectionWork work = this.node.addWork("WORK", new NotUseWorkSource());
		SectionTask taskFirst = work.addTask("TASK", "TYPE");
		SectionTask taskSecond = work.addTask("TASK", "ANOTHER_TYPE");
		this.verifyMockObjects();

		// Should be the same section work
		assertEquals("Should be same section task on adding twice", taskFirst,
				taskSecond);
	}

	/**
	 * Ensure issue if add {@link SectionTask} by same name twice within a
	 * {@link OfficeSection} with different {@link SectionWork}.
	 */
	public void testAddSectionTaskTwiceByDifferentSectionWork() {

		// Record issue in adding the section task twice
		this.issues.addIssue(LocationType.SECTION, SECTION_LOCATION,
				AssetType.WORK, "WORK_TWO", "Section task TASK already added");

		// Add the section task twice by different work
		this.replayMockObjects();
		SectionTask taskFirst = this.node.addWork("WORK_ONE",
				new NotUseWorkSource()).addTask("TASK", "TYPE");
		SectionTask taskSecond = this.node.addWork("WORK_TWO",
				new NotUseWorkSource()).addTask("TASK", "ANOTHER_TYPE");
		this.verifyMockObjects();

		// Should be the same section work
		assertEquals("Should be same section task on adding twice", taskFirst,
				taskSecond);
	}

	/**
	 * Ensure able to get {@link TaskFlow} and {@link TaskObject} instances from
	 * the {@link SectionTask}.
	 */
	public void testSectionTaskGetFlowsAndObjects() {
		// Ensure able to get flow/objects from section task
		this.replayMockObjects();

		// Obtain the section task
		SectionTask task = this.node.addWork("WORK",
				NotUseWorkSource.class.getName()).addTask("TASK", "TYPE");

		// Ensure can get flow
		TaskFlow flow = task.getTaskFlow("FLOW");
		assertNotNull("Must have flow", flow);
		assertEquals("Incorrect flow name", "FLOW", flow.getTaskFlowName());
		assertEquals("Should get same task flow again", flow, task
				.getTaskFlow("FLOW"));
		assertNotSame("Should not be same flow for different name", flow, task
				.getTaskFlow("ANOTHER"));

		// Ensure can get object
		TaskObject object = task.getTaskObject("OBJECT");
		assertNotNull("Must have object", object);
		assertEquals("Incorrect object name", "OBJECT", object
				.getTaskObjectName());
		assertEquals("Should get same task object again", object, task
				.getTaskObject("OBJECT"));
		assertNotSame("Should not be same object for different name", object,
				task.getTaskObject("ANOTHER"));

		// Ensure can get escalation
		TaskFlow escalation = task.getTaskEscalation("ESCALATION");
		assertNotNull("Must have escalation", escalation);
		assertEquals("Incorrect escalation name", "ESCALATION", escalation
				.getTaskFlowName());
		assertEquals("Should get same task flow again", escalation, task
				.getTaskEscalation("ESCALATION"));
		assertNotSame("Should not be same escalation for different name",
				escalation, task.getTaskEscalation("ANOTHER"));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can add a {@link SubSection}.
	 */
	public void testAddSubSection() {
		// Add two different sub sections verifying details
		this.replayMockObjects();
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				NotUseSectionSource.class.getName(), "SUB_SECTION_LOCATION");
		assertNotNull("Must have sub section", subSection);
		assertEquals("Incorrect sub section name", "SUB_SECTION", subSection
				.getSubSectionName());
		assertNotSame("Should obtain another sub section", subSection,
				this.node.addSubSection("ANOTHER", "not used", "not used"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SubSection} by same name twice.
	 */
	public void testAddSubSectionTwice() {

		// Record issue in adding the sub section twice
		this.record_issue("Sub section SUB_SECTION already added");

		// Add the sub section twice
		this.replayMockObjects();
		SubSection subSectionFirst = this.node.addSubSection("SUB_SECTION",
				new NotUseSectionSource(), null);
		SubSection subSectionSecond = this.node.addSubSection("SUB_SECTION",
				new NotUseSectionSource(), null);
		this.verifyMockObjects();

		// Should be the same sub section
		assertEquals("Should be same sub section on adding twice",
				subSectionFirst, subSectionSecond);
	}

	/**
	 * Ensure able to get {@link SubSectionInput}, {@link SubSectionOutput} and
	 * {@link SubSectionObject} instances from the {@link SubSection}.
	 */
	public void testSubSectionGetInputsOutputsAndObjects() {
		// Ensure able to get inputs/outputs/objects from sub section
		this.replayMockObjects();

		// Obtain the sub section
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				NotUseSectionSource.class.getName(), "LOCATION");

		// Ensure can get input
		SubSectionInput input = subSection.getSubSectionInput("INPUT");
		assertNotNull("Must have input", input);
		assertEquals("Incorrect input name", "INPUT", input
				.getSubSectionInputName());
		assertEquals("Should get same input again", input, subSection
				.getSubSectionInput("INPUT"));
		assertNotSame("Should not be same input for different name", input,
				subSection.getSubSectionInput("ANOTHER"));

		// Ensure can get output
		SubSectionOutput output = subSection.getSubSectionOutput("OUTPUT");
		assertNotNull("Must have output", output);
		assertEquals("Incorrect output name", "OUTPUT", output
				.getSubSectionOutputName());
		assertEquals("Should get same output again", output, subSection
				.getSubSectionOutput("OUTPUT"));
		assertNotSame("Should not be same output for different name", output,
				subSection.getSubSectionOutput("ANOTHER"));

		// Ensure can get object
		SubSectionObject object = subSection.getSubSectionObject("OBJECT");
		assertNotNull("Must have output", object);
		assertEquals("Incorrect output name", "OBJECT", object
				.getSubSectionObjectName());
		assertEquals("Should get same object again", object, subSection
				.getSubSectionObject("OBJECT"));
		assertNotSame("Should not be same object for different name", object,
				subSection.getSubSectionObject("ANOTHER"));

		this.verifyMockObjects();
	}

	/**
	 * Ensures able to add a {@link SectionInput}, {@link SectionOutput} and
	 * {@link SectionObject} after obtaining them as {@link SubSectionInput},
	 * {@link SubSectionOutput} and {@link SubSectionObject} instances from the
	 * node as a {@link SubSection}.
	 */
	public void testSectionAddInputOutputObjectAfterSubSectionGet() {
		this.replayMockObjects();

		// Ensure sub section input the same as section input added after
		SubSectionInput subSectionInput = this.node.getSubSectionInput("INPUT");
		SectionInput sectionInput = this.node.addInput("INPUT", String.class
				.getName());
		assertEquals("Inputs should be the same", subSectionInput, sectionInput);

		// Ensure sub section output the same as section output added after
		SubSectionOutput subSectionOutput = this.node
				.getSubSectionOutput("OUTPUT");
		SectionOutput sectionOutput = this.node.addOutput("OUTPUT",
				Exception.class.getName(), true);
		assertEquals("Outputs should be the same", subSectionOutput,
				sectionOutput);

		// Ensure sub section object the same as section object added after
		SubSectionObject subSectionObject = this.node
				.getSubSectionObject("OBJECT");
		SectionObject sectionObject = this.node.addObject("OBJECT",
				Connection.class.getName());
		assertEquals("Objects should be the same", subSectionObject,
				sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Records adding an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(LocationType.SECTION, SECTION_LOCATION, null,
				null, issueDescription);
	}

	/**
	 * {@link WorkSource} that should not have its methods invoked.
	 */
	public static class NotUseWorkSource implements WorkSource<Work> {

		/*
		 * ================== WorkSource ====================================
		 */

		@Override
		public WorkSourceSpecification getSpecification() {
			fail("Should not use WorkSource");
			return null;
		}

		@Override
		public void sourceWork(WorkTypeBuilder<Work> workTypeBuilder,
				WorkSourceContext context) throws Exception {
			fail("Should not use WorkSource");
		}
	}

	/**
	 * {@link SectionSource} that should not have its methods invoked.
	 */
	public static class NotUseSectionSource implements SectionSource {

		/*
		 * ==================== SectionSource =============================
		 */

		@Override
		public SectionSourceSpecification getSpecification() {
			fail("Should not use SectionSource");
			return null;
		}

		@Override
		public void sourceSection(SectionBuilder sectionBuilder,
				SectionSourceContext context) throws Exception {
			fail("Should not use SectionSource");
		}
	}
}