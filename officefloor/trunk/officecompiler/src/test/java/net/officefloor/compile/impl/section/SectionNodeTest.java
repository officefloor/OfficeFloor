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

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionBuilder;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
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
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
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
	private SectionNode node = new SectionNodeImpl(SECTION_LOCATION,
			this.issues);

	/**
	 * Tests adding a {@link SectionInput}.
	 */
	public void testAddSectionInput() {
		// Add two different inputs verifying details
		this.replayMockObjects();
		SectionInput input = this.node.addSectionInput("INPUT", String.class
				.getName());
		assertNotNull("Must have input", input);
		assertEquals("Incorrect input name", "INPUT", input
				.getSectionInputName());
		assertNotSame("Should obtain another input", input, this.node
				.addSectionInput("ANOTHER", Integer.class.getName()));
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
		SectionInput inputFirst = this.node.addSectionInput("INPUT",
				String.class.getName());
		SectionInput inputSecond = this.node.addSectionInput("INPUT",
				Integer.class.getName());
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
		SectionOutput output = this.node.addSectionOutput("OUTPUT",
				Double.class.getName(), false);
		assertNotNull("Must have output", output);
		assertEquals("Incorrect output name", "OUTPUT", output
				.getSectionOutputName());
		assertNotSame("Should obtain another output", output, this.node
				.addSectionOutput("ANOTHER", Exception.class.getName(), true));
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
		SectionOutput outputFirst = this.node.addSectionOutput("OUTPUT",
				Object.class.getName(), false);
		SectionOutput outputSecond = this.node.addSectionOutput("OUTPUT",
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
		SectionObject object = this.node.addSectionObject("OBJECT",
				Connection.class.getName());
		assertNotNull("Must have object", object);
		assertEquals("Incorrect object name", "OBJECT", object
				.getSectionObjectName());
		assertNotSame("Should obtain another object", object, this.node
				.addSectionObject("ANOTHER", Object.class.getName()));
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
		SectionObject objectFirst = this.node.addSectionObject("OBJECT",
				Connection.class.getName());
		SectionObject objectSecond = this.node.addSectionObject("OBJECT",
				Exception.class.getName());
		this.verifyMockObjects();

		// Should be the same object
		assertEquals("Should be same object on adding twice", objectFirst,
				objectSecond);
	}

	/**
	 * Ensure can add a {@link SectionManagedObject}.
	 */
	public void testAddSectionManagedObject() {
		// Add two different managed objects verifying details
		this.replayMockObjects();
		SectionManagedObject mo = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());
		assertNotNull("Must have section managed object", mo);
		assertEquals("Incorrect section managed object name", "MO", mo
				.getSectionManagedObjectName());
		assertNotSame("Should obtain another section managed object", mo,
				this.node.addManagedObject("ANOTHER",
						NotUseManagedObjectSource.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionManagedObject} by same name twice.
	 */
	public void testAddSectionManagedObjectTwice() {

		// Record issue in adding the section managed object twice
		this.record_issue("Section managed object MO already added");

		// Add the section managed object twice
		this.replayMockObjects();
		SectionManagedObject moFirst = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObject moSecond = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());
		this.verifyMockObjects();

		// Should be the same section managed object
		assertEquals("Should be same section managed object on adding twice",
				moFirst, moSecond);
	}

	/**
	 * Ensure able to get {@link ManagedObjectFlow} and
	 * {@link ManagedObjectDependency} instances from the
	 * {@link SectionManagedObject}.
	 */
	public void testSectionManagedObjectGetDependenciesAndFlows() {
		// Ensure able to get dependencies/flows from section managed object
		this.replayMockObjects();

		// Obtain the section managed object
		SectionManagedObject mo = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());

		// Ensure can get dependency
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		assertNotNull("Must have dependency", dependency);
		assertEquals("Incorrect dependency name", "DEPENDENCY", dependency
				.getManagedObjectDependencyName());
		assertEquals("Should get same managed object dependency again",
				dependency, mo.getManagedObjectDependency("DEPENDENCY"));
		assertNotSame("Should not be same dependency for different name",
				dependency, mo.getManagedObjectDependency("ANOTHER"));

		// Ensure can get flow
		ManagedObjectFlow flow = mo.getManagedObjectFlow("FLOW");
		assertNotNull("Must have flow", flow);
		assertEquals("Incorrect flow name", "FLOW", flow
				.getManagedObjectFlowName());
		assertEquals("Should get same task flow again", flow, mo
				.getManagedObjectFlow("FLOW"));
		assertNotSame("Should not be same flow for different name", flow, mo
				.getManagedObjectFlow("ANOTHER"));

		this.verifyMockObjects();
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
		SectionInput sectionInput = this.node.addSectionInput("INPUT",
				String.class.getName());
		assertEquals("Inputs should be the same", subSectionInput, sectionInput);

		// Ensure sub section output the same as section output added after
		SubSectionOutput subSectionOutput = this.node
				.getSubSectionOutput("OUTPUT");
		SectionOutput sectionOutput = this.node.addSectionOutput("OUTPUT",
				Exception.class.getName(), true);
		assertEquals("Outputs should be the same", subSectionOutput,
				sectionOutput);

		// Ensure sub section object the same as section object added after
		SubSectionObject subSectionObject = this.node
				.getSubSectionObject("OBJECT");
		SectionObject sectionObject = this.node.addSectionObject("OBJECT",
				Connection.class.getName());
		assertEquals("Objects should be the same", subSectionObject,
				sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link SectionInput} to the {@link SectionTask}.
	 */
	public void testLinkSectionInputToSectionTask() {

		// Record already being linked
		this.record_issue("Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		SectionInput input = this.node.addSectionInput("INPUT", Object.class
				.getName());
		SectionWork work = this.node.addWork("WORK", NotUseWorkSource.class
				.getName());
		SectionTask task = work.addTask("TASK", "TYPE");
		this.node.link(input, task);
		assertFlowLink("input -> task", input, task);

		// Ensure only can link once
		this.node.link(input, work.addTask("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", input, task);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link SectionInput} to the {@link SubSectionInput}.
	 */
	public void testLinkSectionInputToSubSectionInput() {

		// Record already being linked
		this.record_issue("Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		SectionInput input = this.node.addSectionInput("INPUT", Object.class
				.getName());
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				NotUseSectionSource.class.getName(), "LOCATION");
		SubSectionInput subSectionInput = subSection
				.getSubSectionInput("SUB_SECTION_INPUT");
		this.node.link(input, subSectionInput);
		assertFlowLink("input -> sub section input", input, subSectionInput);

		// Ensure only can link once
		this.node.link(input, subSection.getSubSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", input, subSectionInput);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SectionInput} to the {@link SectionOutput}.
	 */
	public void testLinkSectionInputToSectionOutput() {

		// Record already being linked
		this.record_issue("Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		SectionInput input = this.node.addSectionInput("INPUT", Object.class
				.getName());
		SectionOutput output = this.node.addSectionOutput("OUTPUT",
				Object.class.getName(), false);
		this.node.link(input, output);
		assertFlowLink("input -> output", input, output);

		// Ensure only can link once
		this.node.link(input, this.node.addSectionOutput("ANOTHER",
				String.class.getName(), false));
		assertFlowLink("Can only link once", input, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link TaskFlow} to the {@link SectionTask}.
	 */
	public void testLinkTaskFlowToSectionTask() {

		// Record already being linked
		this.record_issue("Task flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionTask task = this.node.addWork("WORK", new NotUseWorkSource())
				.addTask("TASK", "TYPE");
		TaskFlow flow = task.getTaskFlow("FLOW");
		SectionTask targetTask = this.node.addWork("TARGET",
				NotUseWorkSource.class.getName()).addTask("TARGET", "TYPE");
		this.node
				.link(flow, targetTask, FlowInstigationStrategyEnum.SEQUENTIAL);
		assertFlowLink("task flow -> task", flow, targetTask);
		assertFlowInstigationStrategy(flow,
				FlowInstigationStrategyEnum.SEQUENTIAL);

		// Ensure only can link once
		this.node.link(flow, task, FlowInstigationStrategyEnum.PARALLEL);
		assertFlowLink("Can only link once", flow, targetTask);
		assertFlowInstigationStrategy(flow,
				FlowInstigationStrategyEnum.SEQUENTIAL);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link TaskFlow} to the {@link SubSectionInput}.
	 */
	public void testLinkTaskFlowToSubSectionInput() {

		// Record already being linked
		this.record_issue("Task flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionTask task = this.node.addWork("WORK", new NotUseWorkSource())
				.addTask("TASK", "TYPE");
		TaskFlow flow = task.getTaskFlow("FLOW");
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				new NotUseSectionSource(), "LOCATION");
		SubSectionInput input = subSection.getSubSectionInput("INPUT");
		this.node.link(flow, input, FlowInstigationStrategyEnum.PARALLEL);
		assertFlowLink("task flow -> sub section input", flow, input);
		assertFlowInstigationStrategy(flow,
				FlowInstigationStrategyEnum.PARALLEL);

		// Ensure only can link once
		this.node.link(flow, subSection.getSubSectionInput("ANOTHER"),
				FlowInstigationStrategyEnum.SEQUENTIAL);
		assertFlowLink("Can only link once", flow, input);
		assertFlowInstigationStrategy(flow,
				FlowInstigationStrategyEnum.PARALLEL);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link TaskFlow} to the {@link SectionOutput}.
	 */
	public void testLinkTaskFlowToSectionOutput() {

		// Record already being linked
		this.record_issue("Task flow FLOW linked more than once");

		this.replayMockObjects();

		// Link (escalation)
		SectionTask task = this.node.addWork("WORK", new NotUseWorkSource())
				.addTask("TASK", "TYPE");
		TaskFlow flow = task.getTaskEscalation("FLOW");
		SectionOutput output = this.node.addSectionOutput("OUTPUT",
				Exception.class.getName(), true);
		this.node.link(flow, output, FlowInstigationStrategyEnum.ASYNCHRONOUS);
		assertFlowLink("task flow -> section output", flow, output);
		// Task Escalation always sequential
		assertFlowInstigationStrategy(flow,
				FlowInstigationStrategyEnum.SEQUENTIAL);

		// Ensure only can link once
		this.node.link(flow, this.node.addSectionOutput("ANOTHER", Object.class
				.getName(), false), FlowInstigationStrategyEnum.PARALLEL);
		assertFlowLink("Can only link once", flow, output);
		assertFlowInstigationStrategy(flow,
				FlowInstigationStrategyEnum.SEQUENTIAL);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SectionTask} to its next {@link SectionTask}.
	 */
	public void testLinkSectionTaskToNextSectionTask() {

		// Record already being linked
		this.record_issue("Task TASK linked more than once");

		this.replayMockObjects();

		// Link
		SectionWork work = this.node.addWork("WORK", NotUseWorkSource.class
				.getName());
		SectionTask task = work.addTask("TASK", "TYPE");
		SectionTask nextTask = work.addTask("NEXT", "TYPE");
		this.node.link(task, nextTask);
		assertFlowLink("task -> next task", task, nextTask);

		// Ensure only can link once
		this.node.link(task, work.addTask("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", task, nextTask);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SectionTask} to its next {@link SubSectionInput}.
	 */
	public void testLinkSectionTaskToNextSubSectionInput() {

		// Record already being linked
		this.record_issue("Task TASK linked more than once");

		this.replayMockObjects();

		// Link
		SectionTask task = this.node.addWork("WORK",
				NotUseWorkSource.class.getName()).addTask("TASK", "TYPE");
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				new NotUseSectionSource(), "LOCATION");
		SubSectionInput input = subSection.getSubSectionInput("INPUT");
		this.node.link(task, input);
		assertFlowLink("task -> next input", task, input);

		// Ensure only can link once
		this.node.link(task, subSection.getSubSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", task, input);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SectionTask} to the {@link SectionOutput}.
	 */
	public void testLinkSectionTaskToNextSectionOutput() {

		// Record already being linked
		this.record_issue("Task TASK linked more than once");

		this.replayMockObjects();

		// Link
		SectionTask task = this.node.addWork("WORK",
				NotUseWorkSource.class.getName()).addTask("TASK", "TYPE");
		SectionOutput output = this.node.addSectionOutput("OUTPUT",
				Object.class.getName(), false);
		this.node.link(task, output);
		assertFlowLink("task -> next output", task, output);

		// Ensure only can link once
		this.node.link(task, this.node.addSectionOutput("ANOTHER", String.class
				.getName(), false));
		assertFlowLink("Can only link once", task, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionOutput} to the {@link SectionTask}.
	 */
	public void testLinkSubSectionOutputToSectionTask() {

		// Record already being linked
		this.record_issue("Sub section output OUTPUT linked more than once");

		this.replayMockObjects();

		// Link
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				NotUseSectionSource.class.getName(), SECTION_LOCATION);
		SubSectionOutput output = subSection.getSubSectionOutput("OUTPUT");
		SectionWork work = this.node.addWork("WORK", NotUseWorkSource.class
				.getName());
		SectionTask task = work.addTask("TASK", "TYPE");
		this.node.link(output, task);
		assertFlowLink("sub section output -> task", output, task);

		// Ensure only can link once
		this.node.link(output, work.addTask("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", output, task);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionOutput} to the {@link SubSectionInput}.
	 */
	public void testLinkSubSectionOutputToSubSectionInput() {

		// Record already being linked
		this.record_issue("Sub section output OUTPUT linked more than once");

		this.replayMockObjects();

		// Link
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				NotUseSectionSource.class.getName(), SECTION_LOCATION);
		SubSectionOutput output = subSection.getSubSectionOutput("OUTPUT");
		SubSectionInput input = subSection.getSubSectionInput("INPUT");
		this.node.link(output, input);
		assertFlowLink("sub section output -> sub section input", output, input);

		// Ensure only can link once
		this.node.link(output, subSection.getSubSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", output, input);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionOutput} to the {@link SectionOutput}.
	 */
	public void testLinkSubSectionOutputToSectionOutput() {

		// Record already being linked
		this.record_issue("Sub section output OUTPUT linked more than once");

		this.replayMockObjects();

		// Link
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				NotUseSectionSource.class.getName(), SECTION_LOCATION);
		SubSectionOutput output = subSection.getSubSectionOutput("OUTPUT");
		SectionOutput sectionOutput = this.node.addSectionOutput("OUTPUT",
				Object.class.getName(), false);
		this.node.link(output, sectionOutput);
		assertFlowLink("sub section output -> section output", output,
				sectionOutput);

		// Ensure only can link once
		this.node.link(output, this.node.addSectionOutput("ANOTHER",
				String.class.getName(), false));
		assertFlowLink("Can only link once", output, sectionOutput);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectFlow} to the {@link SectionTask}.
	 */
	public void testLinkManagedObjectFlowToSectionTask() {

		// Record already being linked
		this.record_issue("Managed object flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObject mo = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());
		ManagedObjectFlow flow = mo.getManagedObjectFlow("FLOW");
		SectionWork work = this.node.addWork("WORK", NotUseWorkSource.class
				.getName());
		SectionTask task = work.addTask("TASK", "TYPE");
		this.node.link(flow, task);
		assertFlowLink("managed object flow -> section task", flow, task);

		// Ensure only can link once
		this.node.link(flow, work.addTask("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", flow, task);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectFlow} to the {@link SubSectionInput}
	 * .
	 */
	public void testLinkManagedObjectFlowToSubSectionInput() {

		// Record already being linked
		this.record_issue("Managed object flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObject mo = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());
		ManagedObjectFlow flow = mo.getManagedObjectFlow("FLOW");
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				NotUseSectionSource.class.getName(), SECTION_LOCATION);
		SubSectionInput input = subSection.getSubSectionInput("INPUT");
		this.node.link(flow, input);
		assertFlowLink("managed object flow -> sub section input", flow, input);

		// Ensure only can link once
		this.node.link(flow, subSection.getSubSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", flow, input);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectFlow} to the {@link SectionOutput}.
	 */
	public void testLinkManagedObjectFlowToSectionOutput() {

		// Record already being linked
		this.record_issue("Managed object flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObject mo = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());
		ManagedObjectFlow flow = mo.getManagedObjectFlow("FLOW");
		SectionOutput output = this.node.addSectionOutput("OUTPUT",
				Object.class.getName(), false);
		this.node.link(flow, output);
		assertFlowLink("managed object flow -> section output", flow, output);

		// Ensure only can link once
		this.node.link(flow, this.node.addSectionOutput("ANOTHER", String.class
				.getName(), false));
		assertFlowLink("Can only link once", flow, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link TaskObject} to the {@link SectionObject}.
	 */
	public void testLinkTaskObjectToSectionObject() {

		// Record already being linked
		this.record_issue("Task object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		TaskObject object = this.node.addWork("WORK", new NotUseWorkSource())
				.addTask("TASK", "TYPE").getTaskObject("OBJECT");
		SectionObject sectionObject = this.node.addSectionObject("OUTPUT",
				Connection.class.getName());
		this.node.link(object, sectionObject);
		assertObjectLink("task object -> section object", object, sectionObject);

		// Ensure only can link once
		this.node.link(object, this.node.addSectionObject("ANOTHER",
				String.class.getName()));
		assertObjectLink("Can only link once", object, sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link TaskObject} to the {@link SectionManagedObject}.
	 */
	public void testLinkTaskObjectToSectionManagedObject() {

		// Record already being linked
		this.record_issue("Task object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		TaskObject object = this.node.addWork("WORK", new NotUseWorkSource())
				.addTask("TASK", "TYPE").getTaskObject("OBJECT");
		SectionManagedObject mo = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());
		this.node.link(object, mo);
		assertObjectLink("task object -> section managed object", object, mo);

		// Ensure only can link once
		this.node.link(object, this.node.addManagedObject("ANOTHER",
				NotUseManagedObjectSource.class.getName()));
		assertObjectLink("Can only link once", object, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the {@link SectionObject}.
	 */
	public void testLinkSubSectionObjectToSectionObject() {

		// Record already being linked
		this.record_issue("Sub section object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		SubSectionObject object = this.node.addSubSection("SUB_SECTION",
				new NotUseSectionSource(), SECTION_LOCATION)
				.getSubSectionObject("OBJECT");
		SectionObject sectionObject = this.node.addSectionObject("OUTPUT",
				Connection.class.getName());
		this.node.link(object, sectionObject);
		assertObjectLink("sub section object -> section object", object,
				sectionObject);

		// Ensure only can link once
		this.node.link(object, this.node.addSectionObject("ANOTHER",
				String.class.getName()));
		assertObjectLink("Can only link once", object, sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the
	 * {@link SectionManagedObject}.
	 */
	public void testLinkSubSectionObjectToSectionManagedObject() {

		// Record already being linked
		this.record_issue("Sub section object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		SubSectionObject object = this.node.addSubSection("SUB_SECTION",
				new NotUseSectionSource(), SECTION_LOCATION)
				.getSubSectionObject("OBJECT");
		SectionManagedObject mo = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());
		this.node.link(object, mo);
		assertObjectLink("sub section object -> section managed object",
				object, mo);

		// Ensure only can link once
		this.node.link(object, this.node.addManagedObject("ANOTHER",
				NotUseManagedObjectSource.class.getName()));
		assertObjectLink("Can only link once", object, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the {@link SectionObject}.
	 */
	public void testLinkManagedObjectDependencyToSectionObject() {

		// Record already being linked
		this
				.record_issue("Managed object dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObject mo = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		SectionObject sectionObject = this.node.addSectionObject("OBJECT",
				Connection.class.getName());
		this.node.link(dependency, sectionObject);
		assertObjectLink("managed object dependency -> section object",
				dependency, sectionObject);

		// Ensure only can link once
		this.node.link(dependency, this.node.addSectionObject("ANOTHER",
				Object.class.getName()));
		assertObjectLink("Can only link once", dependency, sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the
	 * {@link SectionManagedObject}.
	 */
	public void testLinkManagedObjectDependencyToSectionManagedObject() {

		// Record already being linked
		this
				.record_issue("Managed object dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObject mo = this.node.addManagedObject("MO",
				NotUseManagedObjectSource.class.getName());
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		SectionManagedObject moTarget = this.node.addManagedObject("MO_TARGET",
				NotUseManagedObjectSource.class.getName());
		this.node.link(dependency, moTarget);
		assertObjectLink("managed object dependency -> section managed object",
				dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, this.node.addManagedObject("ANOTHER",
				NotUseManagedObjectSource.class.getName()));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Asserts the {@link LinkFlowNode} source is linked to the target
	 * {@link LinkFlowNode}.
	 * 
	 * @param msg
	 *            Message.
	 * @param linkSource
	 *            Source {@link LinkFlowNode}.
	 * @param linkTarget
	 *            Target {@link LinkFlowNode}.
	 */
	private static void assertFlowLink(String msg, Object linkSource,
			Object linkTarget) {
		assertTrue(msg + ": source must be "
				+ LinkFlowNode.class.getSimpleName(),
				linkSource instanceof LinkFlowNode);
		assertEquals(msg, ((LinkFlowNode) linkSource).getLinkedFlowNode(),
				linkTarget);
	}

	/**
	 * Asserts the {@link FlowInstigationStrategyEnum} for the {@link TaskFlow}.
	 * 
	 * @param taskFlow
	 *            {@link TaskFlow} to check.
	 * @param instigationStrategy
	 *            Expected {@link FlowInstigationStrategyEnum}.
	 */
	private static void assertFlowInstigationStrategy(TaskFlow taskFlow,
			FlowInstigationStrategyEnum instigationStrategy) {
		assertTrue("Task flow must be " + TaskFlowNode.class,
				taskFlow instanceof TaskFlowNode);
		assertEquals("Incorrect instigation strategy", instigationStrategy,
				((TaskFlowNode) taskFlow).getFlowInstigationStrategy());
	}

	/**
	 * Asserts the {@link LinkObjectNode} source is linked to the target
	 * {@link LinkObjectNode}.
	 * 
	 * @param msg
	 *            Message.
	 * @param linkSource
	 *            Source {@link LinkObjectNode}.
	 * @param linkTarget
	 *            Target {@link LinkObjectNode}.
	 */
	private static void assertObjectLink(String msg, Object linkSource,
			Object linkTarget) {
		assertTrue(msg + ": source must be "
				+ LinkObjectNode.class.getSimpleName(),
				linkSource instanceof LinkObjectNode);
		assertEquals(msg, ((LinkObjectNode) linkSource).getLinkedObjectNode(),
				linkTarget);
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
	 * {@link ManagedObjectSource} that should not have its methods invoked.
	 */
	public static class NotUseManagedObjectSource implements
			ManagedObjectSource<Indexed, Indexed> {

		/*
		 * ===================== ManagedObjectSource ======================
		 */

		@Override
		public ManagedObjectSourceSpecification getSpecification() {
			fail("Should not use ManagedObjectSource");
			return null;
		}

		@Override
		public void init(ManagedObjectSourceContext<Indexed> context)
				throws Exception {
			fail("Should not use ManagedObjectSource");
		}

		@Override
		public ManagedObjectSourceMetaData<Indexed, Indexed> getMetaData() {
			fail("Should not use ManagedObjectSource");
			return null;
		}

		@Override
		public void start(ManagedObjectExecuteContext<Indexed> context)
				throws Exception {
			fail("Should not use ManagedObjectSource");
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			fail("Should not use ManagedObjectSource");
		}
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