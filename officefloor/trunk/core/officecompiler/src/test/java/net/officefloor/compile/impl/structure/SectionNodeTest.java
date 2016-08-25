/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.compile.impl.structure;

import java.sql.Connection;

import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.TaskFlowNode;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
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
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests the {@link SectionDesigner}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionNodeTest extends AbstractStructureTestCase {

	/**
	 * Name of the {@link SectionNode}.
	 */
	private static final String SECTION_NAME = "SECTION";

	/**
	 * Location of the {@link OfficeSection} being built.
	 */
	private static final String SECTION_LOCATION = "SECTION_LOCATION";

	/**
	 * {@link SectionDesigner} to be tested.
	 */
	private final SectionNode node = this.nodeContext.createSectionNode(
			SECTION_NAME, null).initialise(new ClassSectionSource(), null,
			SECTION_LOCATION, null, null);

	/**
	 * Ensure allow {@link SectionSource} to report issues via the
	 * {@link SectionDesigner}.
	 */
	public void testAddIssue() {

		// Record adding the issue
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"TEST_ISSUE");

		// Add the issue
		this.replayMockObjects();
		this.node.addIssue("TEST_ISSUE");
		this.verifyMockObjects();
	}

	/**
	 * Ensure allow {@link SectionSource} to report issues via the
	 * {@link SectionDesigner}.
	 */
	public void testAddIssueWithCause() {

		final Exception failure = new Exception("cause");

		// Record adding the issue
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"TEST_ISSUE", failure);

		// Add the issue
		this.replayMockObjects();
		this.node.addIssue("TEST_ISSUE", failure);
		this.verifyMockObjects();
	}

	/**
	 * Tests adding a {@link SectionInput}.
	 */
	public void testAddSectionInput() {
		// Add two different inputs verifying details
		this.replayMockObjects();
		SectionInput input = this.node.addSectionInput("INPUT",
				String.class.getName());
		assertNotNull("Must have input", input);
		assertEquals("Incorrect input name", "INPUT",
				input.getSectionInputName());
		assertNotSame("Should obtain another input", input,
				this.node.addSectionInput("ANOTHER", Integer.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link SectionInput} twice.
	 */
	public void testAddSectionInputTwice() {

		// Record issue in adding the input twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Input INPUT already added");

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
		assertEquals("Incorrect output name", "OUTPUT",
				output.getSectionOutputName());
		assertNotSame("Should obtain another output", output,
				this.node.addSectionOutput("ANOTHER",
						Exception.class.getName(), true));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link SectionOutput} twice.
	 */
	public void testAddSectionOutputTwice() {

		// Record issue in adding the input twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Output OUTPUT already added");

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
		assertEquals("Incorrect object name", "OBJECT",
				object.getSectionObjectName());
		assertNotSame("Should obtain another object", object,
				this.node.addSectionObject("ANOTHER", Object.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link SectionObject} twice.
	 */
	public void testAddSectionObjectTwice() {

		// Record issue in adding the object twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Object OBJECT already added");

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
	 * Ensure can add a {@link SectionManagedObjectSource}.
	 */
	public void testAddSectionManagedObjectSource() {
		// Add two different managed object sources verifying details
		this.replayMockObjects();
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE",
						NotUseManagedObjectSource.class.getName());
		assertNotNull("Must have section managed object source", moSource);
		assertEquals("Incorrect section managed object source name",
				"MO_SOURCE", moSource.getSectionManagedObjectSourceName());
		assertNotSame("Should obtain another section managed object source",
				moSource, this.node.addSectionManagedObjectSource("ANOTHER",
						NotUseManagedObjectSource.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionManagedObjectSource} by same name
	 * twice.
	 */
	public void testAddSectionManagedObjectSourceTwice() {

		// Record issue in adding the section managed object source twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Section managed object source MO already added");

		// Add the section managed object source twice
		this.replayMockObjects();
		SectionManagedObjectSource moSourceFirst = this.node
				.addSectionManagedObjectSource("MO",
						NotUseManagedObjectSource.class.getName());
		SectionManagedObjectSource moSourceSecond = this.node
				.addSectionManagedObjectSource("MO",
						NotUseManagedObjectSource.class.getName());
		this.verifyMockObjects();

		// Should be the same section managed source object
		assertEquals("Should be same section managed object on adding twice",
				moSourceFirst, moSourceSecond);
	}

	/**
	 * Ensure can add a {@link SectionManagedObjectSource} instance.
	 */
	public void testAddSectionManagedObjectSourceInstance() {
		// Add two different managed object sources verifying details
		this.replayMockObjects();
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE",
						new NotUseManagedObjectSource());
		assertNotNull("Must have section managed object source", moSource);
		assertEquals("Incorrect section managed object source name",
				"MO_SOURCE", moSource.getSectionManagedObjectSourceName());
		assertNotSame("Should obtain another section managed object source",
				moSource, this.node.addSectionManagedObjectSource("ANOTHER",
						new NotUseManagedObjectSource()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionManagedObjectSource} instance by same
	 * name twice.
	 */
	public void testAddSectionManagedObjectSourceInstanceTwice() {

		// Record issue in adding the section managed object source twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Section managed object source MO already added");

		// Add the section managed object source twice
		this.replayMockObjects();
		SectionManagedObjectSource moSourceFirst = this.node
				.addSectionManagedObjectSource("MO",
						new NotUseManagedObjectSource());
		SectionManagedObjectSource moSourceSecond = this.node
				.addSectionManagedObjectSource("MO",
						new NotUseManagedObjectSource());
		this.verifyMockObjects();

		// Should be the same section managed source object
		assertEquals("Should be same section managed object on adding twice",
				moSourceFirst, moSourceSecond);
	}

	/**
	 * Ensure can add a {@link SectionManagedObject}.
	 */
	public void testAddSectionManagedObject() {

		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE",
						NotUseManagedObjectSource.class.getName());

		// Add two different managed objects verifying details
		this.replayMockObjects();
		SectionManagedObject mo = moSource.addSectionManagedObject("MO",
				ManagedObjectScope.WORK);
		assertNotNull("Must have section managed object", mo);
		assertEquals("Incorrect section managed object name", "MO",
				mo.getSectionManagedObjectName());
		assertNotSame("Should obtain another section managed object", mo,
				moSource.addSectionManagedObject("ANOTHER",
						ManagedObjectScope.WORK));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionManagedObject} by same name twice.
	 */
	public void testAddSectionManagedObjectTwice() {

		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE",
						NotUseManagedObjectSource.class.getName());

		// Record issue in adding the section managed object twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Section managed object MO already added");

		// Add the section managed object twice
		this.replayMockObjects();
		SectionManagedObject moFirst = moSource.addSectionManagedObject("MO",
				ManagedObjectScope.THREAD);
		SectionManagedObject moSecond = moSource.addSectionManagedObject("MO",
				ManagedObjectScope.PROCESS);
		this.verifyMockObjects();

		// Should be the same section managed object
		assertEquals("Should be same section managed object on adding twice",
				moFirst, moSecond);
	}

	/**
	 * Ensure issue if add {@link SectionManagedObject} by same name twice from
	 * different {@link SectionManagedObjectSource} instances.
	 */
	public void testAddSectionManagedObjectTwiceByDifferentSources() {

		// Record issue in adding the section managed object twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Section managed object MO already added");

		SectionManagedObjectSource moSourceOne = this.node
				.addSectionManagedObjectSource("MO_SOURCE_ONE",
						NotUseManagedObjectSource.class.getName());
		SectionManagedObjectSource moSourceTwo = this.node
				.addSectionManagedObjectSource("MO_SOURCE_TWO",
						NotUseManagedObjectSource.class.getName());

		// Add the section managed object twice by different sources
		this.replayMockObjects();
		SectionManagedObject moFirst = moSourceOne.addSectionManagedObject(
				"MO", ManagedObjectScope.THREAD);
		SectionManagedObject moSecond = moSourceTwo.addSectionManagedObject(
				"MO", ManagedObjectScope.PROCESS);
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
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE",
						NotUseManagedObjectSource.class.getName());
		SectionManagedObject mo = moSource.addSectionManagedObject("MO",
				ManagedObjectScope.THREAD);

		// Ensure can get dependency
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		assertNotNull("Must have dependency", dependency);
		assertEquals("Incorrect dependency name", "DEPENDENCY",
				dependency.getManagedObjectDependencyName());
		assertEquals("Should get same managed object dependency again",
				dependency, mo.getManagedObjectDependency("DEPENDENCY"));
		assertNotSame("Should not be same dependency for different name",
				dependency, mo.getManagedObjectDependency("ANOTHER"));

		// Ensure can get flow
		ManagedObjectFlow flow = moSource.getManagedObjectFlow("FLOW");
		assertNotNull("Must have flow", flow);
		assertEquals("Incorrect flow name", "FLOW",
				flow.getManagedObjectFlowName());
		assertEquals("Should get same task flow again", flow,
				moSource.getManagedObjectFlow("FLOW"));
		assertNotSame("Should not be same flow for different name", flow,
				moSource.getManagedObjectFlow("ANOTHER"));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can add a {@link SectionWork}.
	 */
	public void testAddSectionWork() {
		// Add two different section works verifying details
		this.replayMockObjects();
		SectionWork work = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName());
		assertNotNull("Must have section work", work);
		assertEquals("Incorrect section work name", "WORK",
				work.getSectionWorkName());
		assertNotSame(
				"Should obtain another section work",
				work,
				this.node.addSectionWork("ANOTHER",
						NotUseWorkSource.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionWork} by same name twice.
	 */
	public void testAddSectionWorkTwice() {

		// Record issue in adding the section work twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Section work WORK already added");

		// Add the section work twice
		this.replayMockObjects();
		SectionWork workFirst = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName());
		SectionWork workSecond = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName());
		this.verifyMockObjects();

		// Should be the same section work
		assertEquals("Should be same section work on adding twice", workFirst,
				workSecond);
	}

	/**
	 * Ensure can add a {@link SectionWork} instance.
	 */
	public void testAddSectionWorkInstance() {
		// Add two different section works verifying details
		this.replayMockObjects();
		SectionWork work = this.node.addSectionWork("WORK",
				new NotUseWorkSource());
		assertNotNull("Must have section work", work);
		assertEquals("Incorrect section work name", "WORK",
				work.getSectionWorkName());
		assertNotSame("Should obtain another section work", work,
				this.node.addSectionWork("ANOTHER", new NotUseWorkSource()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionWork} instance by same name twice.
	 */
	public void testAddSectionWorkInstanceTwice() {

		// Record issue in adding the section work twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Section work WORK already added");

		// Add the section work twice
		this.replayMockObjects();
		SectionWork workFirst = this.node.addSectionWork("WORK",
				new NotUseWorkSource());
		SectionWork workSecond = this.node.addSectionWork("WORK",
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
		SectionWork work = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName());
		SectionTask task = work.addSectionTask("TASK", "TYPE");
		assertNotNull("Must have section task", task);
		assertEquals("Incorrect section task name", "TASK",
				task.getSectionTaskName());
		assertNotSame("Should obtain another section task", task,
				work.addSectionTask("ANOTHER", "TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionTask} by same name twice.
	 */
	public void testAddSectionTaskTwice() {

		// Record issue in adding the section task twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Section task TASK already added");

		// Add the section task twice by same work
		this.replayMockObjects();
		SectionWork work = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName());
		SectionTask taskFirst = work.addSectionTask("TASK", "TYPE");
		SectionTask taskSecond = work.addSectionTask("TASK", "ANOTHER_TYPE");
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
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Section task TASK already added");

		// Add the section task twice by different work
		this.replayMockObjects();
		SectionTask taskFirst = this.node.addSectionWork("WORK_ONE",
				NotUseWorkSource.class.getName())
				.addSectionTask("TASK", "TYPE");
		SectionTask taskSecond = this.node.addSectionWork("WORK_TWO",
				NotUseWorkSource.class.getName()).addSectionTask("TASK",
				"ANOTHER_TYPE");
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
		SectionTask task = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName())
				.addSectionTask("TASK", "TYPE");

		// Ensure can get flow
		TaskFlow flow = task.getTaskFlow("FLOW");
		assertNotNull("Must have flow", flow);
		assertEquals("Incorrect flow name", "FLOW", flow.getTaskFlowName());
		assertEquals("Should get same task flow again", flow,
				task.getTaskFlow("FLOW"));
		assertNotSame("Should not be same flow for different name", flow,
				task.getTaskFlow("ANOTHER"));

		// Ensure can get object
		TaskObject object = task.getTaskObject("OBJECT");
		assertNotNull("Must have object", object);
		assertEquals("Incorrect object name", "OBJECT",
				object.getTaskObjectName());
		assertEquals("Should get same task object again", object,
				task.getTaskObject("OBJECT"));
		assertNotSame("Should not be same object for different name", object,
				task.getTaskObject("ANOTHER"));

		// Ensure can get escalation
		TaskFlow escalation = task.getTaskEscalation("ESCALATION");
		assertNotNull("Must have escalation", escalation);
		assertEquals("Incorrect escalation name", "ESCALATION",
				escalation.getTaskFlowName());
		assertEquals("Should get same task flow again", escalation,
				task.getTaskEscalation("ESCALATION"));
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
		assertEquals("Incorrect sub section name", "SUB_SECTION",
				subSection.getSubSectionName());
		assertNotSame("Should obtain another sub section", subSection,
				this.node.addSubSection("ANOTHER", "not used", "not used"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SubSection} by same name twice.
	 */
	public void testAddSubSectionTwice() {

		// Record issue in adding the sub section twice
		this.issues.recordIssue(SECTION_NAME, SectionNodeImpl.class,
				"Sub section SUB_SECTION already added");

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
		assertEquals("Incorrect input name", "INPUT",
				input.getSubSectionInputName());
		assertEquals("Should get same input again", input,
				subSection.getSubSectionInput("INPUT"));
		assertNotSame("Should not be same input for different name", input,
				subSection.getSubSectionInput("ANOTHER"));

		// Ensure can get output
		SubSectionOutput output = subSection.getSubSectionOutput("OUTPUT");
		assertNotNull("Must have output", output);
		assertEquals("Incorrect output name", "OUTPUT",
				output.getSubSectionOutputName());
		assertEquals("Should get same output again", output,
				subSection.getSubSectionOutput("OUTPUT"));
		assertNotSame("Should not be same output for different name", output,
				subSection.getSubSectionOutput("ANOTHER"));

		// Ensure can get object
		SubSectionObject object = subSection.getSubSectionObject("OBJECT");
		assertNotNull("Must have output", object);
		assertEquals("Incorrect output name", "OBJECT",
				object.getSubSectionObjectName());
		assertEquals("Should get same object again", object,
				subSection.getSubSectionObject("OBJECT"));
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
	 * Ensure able to get a {@link OfficeSection} qualified name.
	 */
	public void testSectionQualifiedName() {

		// Create sub sections that provide qualified name
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				NotUseSectionSource.class.getName(), SECTION_LOCATION);

		// Obtain the section qualified name
		SectionNode subSectionNode = (SectionNode) subSection;
		String qualifiedName = subSectionNode.getSectionQualifiedName("WORK");

		// Validate qualified name
		assertEquals("Invalid section qualified name",
				"SECTION.SUB_SECTION.WORK", qualifiedName);
	}

	/**
	 * Ensure can link {@link SectionInput} to the {@link SectionTask}.
	 */
	public void testLinkSectionInputToSectionTask() {

		// Record already being linked
		this.issues.recordIssue("INPUT", SectionInputNodeImpl.class,
				"Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		SectionInput input = this.node.addSectionInput("INPUT",
				Object.class.getName());
		SectionWork work = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName());
		SectionTask task = work.addSectionTask("TASK", "TYPE");
		this.node.link(input, task);
		assertFlowLink("input -> task", input, task);

		// Ensure only can link once
		this.node.link(input, work.addSectionTask("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", input, task);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link SectionInput} to the {@link SubSectionInput}.
	 */
	public void testLinkSectionInputToSubSectionInput() {

		// Record already being linked
		this.issues.recordIssue("INPUT", SectionInputNodeImpl.class,
				"Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		SectionInput input = this.node.addSectionInput("INPUT",
				Object.class.getName());
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
		this.issues.recordIssue("OUTPUT", SectionOutputNodeImpl.class,
				"Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		SectionInput input = this.node.addSectionInput("INPUT",
				Object.class.getName());
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
		this.issues.recordIssue("FLOW", TaskFlowNodeImpl.class,
				"Task flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionTask task = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName())
				.addSectionTask("TASK", "TYPE");
		TaskFlow flow = task.getTaskFlow("FLOW");
		SectionTask targetTask = this.node.addSectionWork("TARGET",
				NotUseWorkSource.class.getName()).addSectionTask("TARGET",
				"TYPE");
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
		this.issues.recordIssue("FLOW", TaskFlowNodeImpl.class,
				"Task flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionTask task = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName())
				.addSectionTask("TASK", "TYPE");
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
		this.issues.recordIssue("FLOW", TaskFlowNodeImpl.class,
				"Task flow FLOW linked more than once");

		this.replayMockObjects();

		// Link (escalation)
		SectionTask task = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName())
				.addSectionTask("TASK", "TYPE");
		TaskFlow flow = task.getTaskEscalation("FLOW");
		SectionOutput output = this.node.addSectionOutput("OUTPUT",
				Exception.class.getName(), true);
		this.node.link(flow, output, FlowInstigationStrategyEnum.ASYNCHRONOUS);
		assertFlowLink("task flow -> section output", flow, output);
		// Task Escalation always sequential
		assertFlowInstigationStrategy(flow,
				FlowInstigationStrategyEnum.SEQUENTIAL);

		// Ensure only can link once
		this.node.link(flow, this.node.addSectionOutput("ANOTHER",
				Object.class.getName(), false),
				FlowInstigationStrategyEnum.PARALLEL);
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
		this.issues.recordIssue("TASK", TaskNodeImpl.class,
				"Task TASK linked more than once");

		this.replayMockObjects();

		// Link
		SectionWork work = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName());
		SectionTask task = work.addSectionTask("TASK", "TYPE");
		SectionTask nextTask = work.addSectionTask("NEXT", "TYPE");
		this.node.link(task, nextTask);
		assertFlowLink("task -> next task", task, nextTask);

		// Ensure only can link once
		this.node.link(task, work.addSectionTask("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", task, nextTask);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SectionTask} to its next {@link SubSectionInput}.
	 */
	public void testLinkSectionTaskToNextSubSectionInput() {

		// Record already being linked
		this.issues.recordIssue("TASK", TaskNodeImpl.class,
				"Task TASK linked more than once");

		this.replayMockObjects();

		// Link
		SectionTask task = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName())
				.addSectionTask("TASK", "TYPE");
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
		this.issues.recordIssue("TASK", TaskNodeImpl.class,
				"Task TASK linked more than once");

		this.replayMockObjects();

		// Link
		SectionTask task = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName())
				.addSectionTask("TASK", "TYPE");
		SectionOutput output = this.node.addSectionOutput("OUTPUT",
				Object.class.getName(), false);
		this.node.link(task, output);
		assertFlowLink("task -> next output", task, output);

		// Ensure only can link once
		this.node.link(task, this.node.addSectionOutput("ANOTHER",
				String.class.getName(), false));
		assertFlowLink("Can only link once", task, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionOutput} to the {@link SectionTask}.
	 */
	public void testLinkSubSectionOutputToSectionTask() {

		// Record already being linked
		this.issues.recordIssue("OUTPUT", SectionOutputNodeImpl.class,
				"Sub section output OUTPUT linked more than once");

		this.replayMockObjects();

		// Link
		SubSection subSection = this.node.addSubSection("SUB_SECTION",
				NotUseSectionSource.class.getName(), SECTION_LOCATION);
		SubSectionOutput output = subSection.getSubSectionOutput("OUTPUT");
		SectionWork work = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName());
		SectionTask task = work.addSectionTask("TASK", "TYPE");
		this.node.link(output, task);
		assertFlowLink("sub section output -> task", output, task);

		// Ensure only can link once
		this.node.link(output, work.addSectionTask("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", output, task);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionOutput} to the {@link SubSectionInput}.
	 */
	public void testLinkSubSectionOutputToSubSectionInput() {

		// Record already being linked
		this.issues.recordIssue("OUTPUT", SectionOutputNodeImpl.class,
				"Sub section output OUTPUT linked more than once");

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
		this.issues.recordIssue("OUTPUT", SectionOutputNodeImpl.class,
				"Sub section output OUTPUT linked more than once");

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
		this.issues.recordIssue("FLOW", ManagedObjectFlowNodeImpl.class,
				"Managed object source flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO",
						NotUseManagedObjectSource.class.getName());
		ManagedObjectFlow flow = moSource.getManagedObjectFlow("FLOW");
		SectionWork work = this.node.addSectionWork("WORK",
				NotUseWorkSource.class.getName());
		SectionTask task = work.addSectionTask("TASK", "TYPE");
		this.node.link(flow, task);
		assertFlowLink("managed object flow -> section task", flow, task);

		// Ensure only can link once
		this.node.link(flow, work.addSectionTask("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", flow, task);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectFlow} to {@link SubSectionInput}.
	 */
	public void testLinkManagedObjectFlowToSubSectionInput() {

		// Record already being linked
		this.issues.recordIssue("FLOW", ManagedObjectFlowNodeImpl.class,
				"Managed object source flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO",
						NotUseManagedObjectSource.class.getName());
		ManagedObjectFlow flow = moSource.getManagedObjectFlow("FLOW");
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
		this.issues.recordIssue("FLOW", ManagedObjectFlowNodeImpl.class,
				"Managed object source flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO",
						NotUseManagedObjectSource.class.getName());
		ManagedObjectFlow flow = moSource.getManagedObjectFlow("FLOW");
		SectionOutput output = this.node.addSectionOutput("OUTPUT",
				Object.class.getName(), false);
		this.node.link(flow, output);
		assertFlowLink("managed object flow -> section output", flow, output);

		// Ensure only can link once
		this.node.link(flow, this.node.addSectionOutput("ANOTHER",
				String.class.getName(), false));
		assertFlowLink("Can only link once", flow, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link TaskObject} to the {@link SectionObject}.
	 */
	public void testLinkTaskObjectToSectionObject() {

		// Record already being linked
		this.issues.recordIssue("OBJECT", TaskObjectNodeImpl.class,
				"Task object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		TaskObject object = this.node
				.addSectionWork("WORK", NotUseWorkSource.class.getName())
				.addSectionTask("TASK", "TYPE").getTaskObject("OBJECT");
		SectionObject sectionObject = this.node.addSectionObject("OUTPUT",
				Connection.class.getName());
		this.node.link(object, sectionObject);
		assertObjectLink("task object -> section object", object, sectionObject);

		// Ensure only can link once
		this.node.link(object,
				this.node.addSectionObject("ANOTHER", String.class.getName()));
		assertObjectLink("Can only link once", object, sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link TaskObject} to the {@link SectionManagedObject}.
	 */
	public void testLinkTaskObjectToSectionManagedObject() {

		// Record already being linked
		this.issues.recordIssue("OBJECT", TaskObjectNodeImpl.class,
				"Task object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		TaskObject object = this.node
				.addSectionWork("WORK", NotUseWorkSource.class.getName())
				.addSectionTask("TASK", "TYPE").getTaskObject("OBJECT");
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE",
						NotUseManagedObjectSource.class.getName());
		SectionManagedObject mo = moSource.addSectionManagedObject("MO",
				ManagedObjectScope.WORK);
		this.node.link(object, mo);
		assertObjectLink("task object -> section managed object", object, mo);

		// Ensure only can link once
		this.node.link(object, moSource.addSectionManagedObject("ANOTHER",
				ManagedObjectScope.THREAD));
		assertObjectLink("Can only link once", object, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the {@link SectionObject}.
	 */
	public void testLinkSubSectionObjectToSectionObject() {

		// Record already being linked
		this.issues.recordIssue("OBJECT", SectionObjectNodeImpl.class,
				"Sub section object OBJECT linked more than once");

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
		this.node.link(object,
				this.node.addSectionObject("ANOTHER", String.class.getName()));
		assertObjectLink("Can only link once", object, sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the
	 * {@link SectionManagedObject}.
	 */
	public void testLinkSubSectionObjectToSectionManagedObject() {

		// Record already being linked
		this.issues.recordIssue("OBJECT", SectionObjectNodeImpl.class,
				"Sub section object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		SubSectionObject object = this.node.addSubSection("SUB_SECTION",
				new NotUseSectionSource(), SECTION_LOCATION)
				.getSubSectionObject("OBJECT");
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE",
						NotUseManagedObjectSource.class.getName());
		SectionManagedObject mo = moSource.addSectionManagedObject("MO",
				ManagedObjectScope.PROCESS);
		this.node.link(object, mo);
		assertObjectLink("sub section object -> section managed object",
				object, mo);

		// Ensure only can link once
		this.node.link(object, moSource.addSectionManagedObject("ANOTHER",
				ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", object, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the {@link SectionObject}.
	 */
	public void testLinkManagedObjectDependencyToSectionObject() {

		// Record already being linked
		this.issues.recordIssue("DEPENDENCY",
				ManagedObjectDependencyNodeImpl.class,
				"Managed object dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE",
						NotUseManagedObjectSource.class.getName());
		SectionManagedObject mo = moSource.addSectionManagedObject("MO",
				ManagedObjectScope.WORK);
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		SectionObject sectionObject = this.node.addSectionObject("OBJECT",
				Connection.class.getName());
		this.node.link(dependency, sectionObject);
		assertObjectLink("managed object dependency -> section object",
				dependency, sectionObject);

		// Ensure only can link once
		this.node.link(dependency,
				this.node.addSectionObject("ANOTHER", Object.class.getName()));
		assertObjectLink("Can only link once", dependency, sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the
	 * {@link SectionManagedObject}.
	 */
	public void testLinkManagedObjectDependencyToSectionManagedObject() {

		// Record already being linked
		this.issues.recordIssue("DEPENDENCY",
				ManagedObjectDependencyNodeImpl.class,
				"Managed object dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE",
						NotUseManagedObjectSource.class.getName());
		SectionManagedObject mo = moSource.addSectionManagedObject("MO",
				ManagedObjectScope.WORK);
		ManagedObjectDependency dependency = mo
				.getManagedObjectDependency("DEPENDENCY");
		SectionManagedObjectSource moSourceTarget = this.node
				.addSectionManagedObjectSource("MO_SOURCE_TARGET",
						NotUseManagedObjectSource.class.getName());
		SectionManagedObject moTarget = moSourceTarget.addSectionManagedObject(
				"MO_TARGET", ManagedObjectScope.PROCESS);
		this.node.link(dependency, moTarget);
		assertObjectLink("managed object dependency -> section managed object",
				dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addSectionManagedObject(
				"ANOTHER", ManagedObjectScope.WORK));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link Input {@link ManagedObjectDependency} to the
	 * {@link SectionObject}.
	 */
	public void testLinkInputManagedObjectDependencyToSectionObject() {

		// Record already being linked
		this.issues.recordIssue("DEPENDENCY",
				ManagedObjectDependencyNodeImpl.class,
				"Managed object dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE", (String) null);
		ManagedObjectDependency dependency = moSource
				.getInputManagedObjectDependency("DEPENDENCY");
		SectionObject moTarget = this.node.addSectionObject("MO_TARGET",
				Connection.class.getName());
		this.node.link(dependency, moTarget);
		assertObjectLink(
				"input managed object dependency -> office floor managed object",
				dependency, moTarget);

		// Ensure only can link once
		this.node.link(
				dependency,
				this.node.addSectionObject("ANOTHER",
						Connection.class.getName()));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link Input {@link ManagedObjectDependency} to the
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkInputManagedObjectDependencyToSectionManagedObject() {

		// Record already being linked
		this.issues.recordIssue("DEPENDENCY",
				ManagedObjectDependencyNodeImpl.class,
				"Managed object dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node
				.addSectionManagedObjectSource("MO_SOURCE", (String) null);
		ManagedObjectDependency dependency = moSource
				.getInputManagedObjectDependency("DEPENDENCY");
		SectionManagedObjectSource moSourceTarget = this.node
				.addSectionManagedObjectSource("MO_SOURCE_TARGET",
						(String) null);
		SectionManagedObject moTarget = moSourceTarget.addSectionManagedObject(
				"MO_TARGET", ManagedObjectScope.PROCESS);
		this.node.link(dependency, moTarget);
		assertObjectLink(
				"input managed object dependency -> office floor managed object",
				dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addSectionManagedObject(
				"ANOTHER", ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
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
	 * {@link ManagedObjectSource} that should not have its methods invoked.
	 */
	@TestSource
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

		@Override
		public void stop() {
			fail("Should not use ManagedObjectSource");
		}
	}

	/**
	 * {@link WorkSource} that should not have its methods invoked.
	 */
	@TestSource
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
	@TestSource
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
		public void sourceSection(SectionDesigner sectionBuilder,
				SectionSourceContext context) throws Exception {
			fail("Should not use SectionSource");
		}
	}
}