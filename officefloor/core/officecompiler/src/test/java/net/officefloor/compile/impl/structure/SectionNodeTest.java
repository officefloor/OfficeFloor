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

package net.officefloor.compile.impl.structure;

import java.sql.Connection;

import net.officefloor.compile.internal.structure.FunctionFlowNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceSpecification;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionObject;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.SectionSourceSpecification;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Tests the {@link SectionDesigner}.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionNodeTest extends AbstractStructureTestCase {

	/**
	 * Location of the {@link OfficeSection} being built.
	 */
	private static final String SECTION_LOCATION = "SECTION_LOCATION";

	/**
	 * Mock {@link OfficeNode}.
	 */
	private final OfficeNode office = this.createMock(OfficeNode.class);

	/**
	 * Mock {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode officeFloor = this.createMock(OfficeFloorNode.class);

	/**
	 * {@link SectionDesigner} to be tested.
	 */
	private final SectionNode node = this.nodeContext.createSectionNode("SECTION", this.office);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.node.initialise(ClassSectionSource.class.getName(), new ClassSectionSource(), SECTION_LOCATION);
	}

	/**
	 * Ensure allow {@link SectionSource} to report issues via the
	 * {@link SectionDesigner}.
	 */
	public void testAddIssue() {

		// Record adding the issue
		this.recordIssue("OFFICE.SECTION", SectionNodeImpl.class, "TEST_ISSUE");

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
		this.recordReturn(this.office, this.office.getQualifiedName("SECTION"), "OFFICE.SECTION");
		this.issues.recordIssue("OFFICE.SECTION", SectionNodeImpl.class, "TEST_ISSUE", failure);

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
		SectionInput input = this.node.addSectionInput("INPUT", String.class.getName());
		assertNotNull("Must have input", input);
		assertEquals("Incorrect input name", "INPUT", input.getSectionInputName());
		assertNotSame("Should obtain another input", input,
				this.node.addSectionInput("ANOTHER", Integer.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link SectionInput} twice.
	 */
	public void testAddSectionInputTwice() {

		// Record issue in adding the input twice
		this.recordIssue("OFFICE.SECTION.INPUT", SectionInputNodeImpl.class, "Section Input INPUT already added");

		// Add the input twice
		this.replayMockObjects();
		SectionInput inputFirst = this.node.addSectionInput("INPUT", String.class.getName());
		SectionInput inputSecond = this.node.addSectionInput("INPUT", Integer.class.getName());
		this.verifyMockObjects();

		// Should be the same input
		assertEquals("Should be same input on adding twice", inputFirst, inputSecond);
	}

	/**
	 * Tests adding a {@link SectionOutput}.
	 */
	public void testSectionOutput() {
		// Add two different outputs verifying details
		this.replayMockObjects();
		SectionOutput output = this.node.addSectionOutput("OUTPUT", Double.class.getName(), false);
		assertNotNull("Must have output", output);
		assertEquals("Incorrect output name", "OUTPUT", output.getSectionOutputName());
		assertNotSame("Should obtain another output", output,
				this.node.addSectionOutput("ANOTHER", Exception.class.getName(), true));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link SectionOutput} twice.
	 */
	public void testAddSectionOutputTwice() {

		// Record issue in adding the input twice
		this.recordIssue("OFFICE.SECTION.OUTPUT", SectionOutputNodeImpl.class, "Section Output OUTPUT already added");

		// Add the output twice
		this.replayMockObjects();
		SectionOutput outputFirst = this.node.addSectionOutput("OUTPUT", Object.class.getName(), false);
		SectionOutput outputSecond = this.node.addSectionOutput("OUTPUT", Exception.class.getName(), true);
		this.verifyMockObjects();

		// Should be the same output
		assertEquals("Should be same output on adding twice", outputFirst, outputSecond);
	}

	/**
	 * Tests obtaining a {@link SectionObject}.
	 */
	public void testSectionObject() {
		// Add two different outputs verifying details
		this.replayMockObjects();
		SectionObject object = this.node.addSectionObject("OBJECT", Connection.class.getName());
		assertNotNull("Must have object", object);
		assertEquals("Incorrect object name", "OBJECT", object.getSectionObjectName());
		assertNotSame("Should obtain another object", object,
				this.node.addSectionObject("ANOTHER", Object.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add the {@link SectionObject} twice.
	 */
	public void testAddSectionObjectTwice() {

		// Record issue in adding the object twice
		this.recordIssue("OFFICE.SECTION.OBJECT", SectionObjectNodeImpl.class, "Section Object OBJECT already added");

		// Add the object twice
		this.replayMockObjects();
		SectionObject objectFirst = this.node.addSectionObject("OBJECT", Connection.class.getName());
		SectionObject objectSecond = this.node.addSectionObject("OBJECT", Exception.class.getName());
		this.verifyMockObjects();

		// Should be the same object
		assertEquals("Should be same object on adding twice", objectFirst, objectSecond);
	}

	/**
	 * Ensure can add a {@link SectionManagedObjectSource}.
	 */
	public void testAddSectionManagedObjectSource() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode();
		this.recordOfficeFloorNode();

		// Add two different managed object sources verifying details
		this.replayMockObjects();
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE",
				NotUseManagedObjectSource.class.getName());
		assertNotNull("Must have section managed object source", moSource);
		assertEquals("Incorrect section managed object source name", "MO_SOURCE",
				moSource.getSectionManagedObjectSourceName());
		assertNotSame("Should obtain another section managed object source", moSource,
				this.node.addSectionManagedObjectSource("ANOTHER", NotUseManagedObjectSource.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionManagedObjectSource} by same name twice.
	 */
	public void testAddSectionManagedObjectSourceTwice() {

		// Record obtaining the OfficeFloor for the managed object
		this.recordOfficeFloorNode();

		// Record issue in adding the section managed object source twice
		this.recordIssue("OFFICE.SECTION.MO", ManagedObjectSourceNodeImpl.class,
				"Managed Object Source MO already added");

		// Add the section managed object source twice
		this.replayMockObjects();
		SectionManagedObjectSource moSourceFirst = this.node.addSectionManagedObjectSource("MO",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObjectSource moSourceSecond = this.node.addSectionManagedObjectSource("MO",
				NotUseManagedObjectSource.class.getName());
		this.verifyMockObjects();

		// Should be the same section managed source object
		assertEquals("Should be same section managed object on adding twice", moSourceFirst, moSourceSecond);
	}

	/**
	 * Ensure can add a {@link SectionManagedObjectSource} instance.
	 */
	public void testAddSectionManagedObjectSourceInstance() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode();
		this.recordOfficeFloorNode();

		// Add two different managed object sources verifying details
		this.replayMockObjects();
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE",
				new NotUseManagedObjectSource());
		assertNotNull("Must have section managed object source", moSource);
		assertEquals("Incorrect section managed object source name", "MO_SOURCE",
				moSource.getSectionManagedObjectSourceName());
		assertNotSame("Should obtain another section managed object source", moSource,
				this.node.addSectionManagedObjectSource("ANOTHER", new NotUseManagedObjectSource()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionManagedObjectSource} instance by same name
	 * twice.
	 */
	public void testAddSectionManagedObjectSourceInstanceTwice() {

		// Record obtaining the OfficeFloor for the managed object
		this.recordOfficeFloorNode();

		// Record issue in adding the section managed object source twice
		this.recordIssue("OFFICE.SECTION.MO", ManagedObjectSourceNodeImpl.class,
				"Managed Object Source MO already added");

		// Add the section managed object source twice
		this.replayMockObjects();
		SectionManagedObjectSource moSourceFirst = this.node.addSectionManagedObjectSource("MO",
				new NotUseManagedObjectSource());
		SectionManagedObjectSource moSourceSecond = this.node.addSectionManagedObjectSource("MO",
				new NotUseManagedObjectSource());
		this.verifyMockObjects();

		// Should be the same section managed source object
		assertEquals("Should be same section managed object on adding twice", moSourceFirst, moSourceSecond);
	}

	/**
	 * Ensure can add a {@link SectionManagedObject}.
	 */
	public void testAddSectionManagedObject() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode(5);

		// Add the managed object source
		this.replayMockObjects();
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE",
				NotUseManagedObjectSource.class.getName());

		// Add two different managed objects verifying details
		SectionManagedObject mo = moSource.addSectionManagedObject("MO", ManagedObjectScope.FUNCTION);
		assertNotNull("Must have section managed object", mo);
		assertEquals("Incorrect section managed object name", "MO", mo.getSectionManagedObjectName());
		assertNotSame("Should obtain another section managed object", mo,
				moSource.addSectionManagedObject("ANOTHER", ManagedObjectScope.FUNCTION));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionManagedObject} by same name twice.
	 */
	public void testAddSectionManagedObjectTwice() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode(3);

		// Record issue in adding the section managed object twice
		this.recordIssue("OFFICE.SECTION.MO", ManagedObjectNodeImpl.class, "Managed Object MO already added");

		// Add the managed object source
		this.replayMockObjects();
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE",
				NotUseManagedObjectSource.class.getName());

		// Add the section managed object twice
		SectionManagedObject moFirst = moSource.addSectionManagedObject("MO", ManagedObjectScope.THREAD);
		SectionManagedObject moSecond = moSource.addSectionManagedObject("MO", ManagedObjectScope.PROCESS);
		this.verifyMockObjects();

		// Should be the same section managed object
		assertEquals("Should be same section managed object on adding twice", moFirst, moSecond);
	}

	/**
	 * Ensure issue if add {@link SectionManagedObject} by same name twice from
	 * different {@link SectionManagedObjectSource} instances.
	 */
	public void testAddSectionManagedObjectTwiceByDifferentSources() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode(4);

		// Record issue in adding the section managed object twice
		this.recordIssue("OFFICE.SECTION.MO", ManagedObjectNodeImpl.class, "Managed Object MO already added");

		// Add the managed object sources
		this.replayMockObjects();
		SectionManagedObjectSource moSourceOne = this.node.addSectionManagedObjectSource("MO_SOURCE_ONE",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObjectSource moSourceTwo = this.node.addSectionManagedObjectSource("MO_SOURCE_TWO",
				NotUseManagedObjectSource.class.getName());

		// Add the section managed object twice by different sources
		SectionManagedObject moFirst = moSourceOne.addSectionManagedObject("MO", ManagedObjectScope.THREAD);
		SectionManagedObject moSecond = moSourceTwo.addSectionManagedObject("MO", ManagedObjectScope.PROCESS);
		this.verifyMockObjects();

		// Should be the same section managed object
		assertEquals("Should be same section managed object on adding twice", moFirst, moSecond);
	}

	/**
	 * Ensure able to get {@link ManagedObjectFlow} and
	 * {@link ManagedObjectDependency} instances from the
	 * {@link SectionManagedObject}.
	 */
	public void testSectionManagedObjectGetDependenciesAndFlows() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode(3);

		// Ensure able to get dependencies/flows from section managed object
		this.replayMockObjects();

		// Obtain the section managed object
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObject mo = moSource.addSectionManagedObject("MO", ManagedObjectScope.THREAD);

		// Ensure can get dependency
		ManagedObjectDependency dependency = mo.getSectionManagedObjectDependency("DEPENDENCY");
		assertNotNull("Must have dependency", dependency);
		assertEquals("Incorrect dependency name", "DEPENDENCY", dependency.getManagedObjectDependencyName());
		assertEquals("Should get same managed object dependency again", dependency,
				mo.getSectionManagedObjectDependency("DEPENDENCY"));
		assertNotSame("Should not be same dependency for different name", dependency,
				mo.getSectionManagedObjectDependency("ANOTHER"));

		// Ensure can get flow
		ManagedObjectFlow flow = moSource.getSectionManagedObjectFlow("FLOW");
		assertNotNull("Must have flow", flow);
		assertEquals("Incorrect flow name", "FLOW", flow.getManagedObjectFlowName());
		assertEquals("Should get same function flow again", flow, moSource.getSectionManagedObjectFlow("FLOW"));
		assertNotSame("Should not be same flow for different name", flow,
				moSource.getSectionManagedObjectFlow("ANOTHER"));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can add a {@link SectionFunctionNamespace}.
	 */
	public void testAddSectionFunctionNamespace() {
		// Add two different section namespaces verifying details
		this.replayMockObjects();
		SectionFunctionNamespace namespace = this.node.addSectionFunctionNamespace("NAMESPACE",
				NotUseManagedFunctionSource.class.getName());
		assertNotNull("Must have section namespace", namespace);
		assertEquals("Incorrect section namespace name", "NAMESPACE", namespace.getSectionFunctionNamespaceName());
		assertNotSame("Should obtain another section namespace", namespace,
				this.node.addSectionFunctionNamespace("ANOTHER", NotUseManagedFunctionSource.class.getName()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionFunctionNamespace} by same name twice.
	 */
	public void testAddSectionFunctionNamespaceTwice() {

		// Record issue in adding the section namespace twice
		this.recordIssue("OFFICE.SECTION.NAMESPACE", FunctionNamespaceNodeImpl.class,
				"Function Namespace NAMESPACE already added");

		// Add the section namespace twice
		this.replayMockObjects();
		SectionFunctionNamespace namespaceFirst = this.node.addSectionFunctionNamespace("NAMESPACE",
				NotUseManagedFunctionSource.class.getName());
		SectionFunctionNamespace namespaceSecond = this.node.addSectionFunctionNamespace("NAMESPACE",
				NotUseManagedFunctionSource.class.getName());
		this.verifyMockObjects();

		// Should be the same section namespace
		assertEquals("Should be same section namespace on adding twice", namespaceFirst, namespaceSecond);
	}

	/**
	 * Ensure can add a {@link SectionFunctionNamespace} instance.
	 */
	public void testAddSectionFunctionNamespaceInstance() {
		// Add two different section namespaces verifying details
		this.replayMockObjects();
		SectionFunctionNamespace namespace = this.node.addSectionFunctionNamespace("NAMESPACE",
				new NotUseManagedFunctionSource());
		assertNotNull("Must have section namespace", namespace);
		assertEquals("Incorrect section namespace name", "NAMESPACE", namespace.getSectionFunctionNamespaceName());
		assertNotSame("Should obtain another section namespace", namespace,
				this.node.addSectionFunctionNamespace("ANOTHER", new NotUseManagedFunctionSource()));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionFunctionNamespace} instance by same name
	 * twice.
	 */
	public void testAddSectionFunctionNamespaceInstanceTwice() {

		// Record issue in adding the section namespace twice
		this.recordIssue("OFFICE.SECTION.NAMESPACE", FunctionNamespaceNodeImpl.class,
				"Function Namespace NAMESPACE already added");

		// Add the section namespace twice
		this.replayMockObjects();
		SectionFunctionNamespace namespaceFirst = this.node.addSectionFunctionNamespace("NAMESPACE",
				new NotUseManagedFunctionSource());
		SectionFunctionNamespace namespaceSecond = this.node.addSectionFunctionNamespace("NAMESPACE",
				new NotUseManagedFunctionSource());
		this.verifyMockObjects();

		// Should be the same section namespace
		assertEquals("Should be same section namespace on adding twice", namespaceFirst, namespaceSecond);
	}

	/**
	 * Ensure can add a {@link SectionFunction}.
	 */
	public void testAddSectionFunction() {
		// Add two different section functions verifying details
		this.replayMockObjects();
		SectionFunctionNamespace namespace = this.node.addSectionFunctionNamespace("NAMESPACE",
				NotUseManagedFunctionSource.class.getName());
		SectionFunction function = namespace.addSectionFunction("FUNCTION", "TYPE");
		assertNotNull("Must have section function", function);
		assertEquals("Incorrect section function name", "FUNCTION", function.getSectionFunctionName());
		assertNotSame("Should obtain another section function", function,
				namespace.addSectionFunction("ANOTHER", "TYPE"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SectionFunction} by same name twice.
	 */
	public void testAddSectionFunctionTwice() {

		// Record issue in adding the section function twice
		this.recordIssue("OFFICE.SECTION.FUNCTION", ManagedFunctionNodeImpl.class,
				"Managed Function FUNCTION already added");

		// Add the section function twice by same namespace
		this.replayMockObjects();
		SectionFunctionNamespace namespace = this.node.addSectionFunctionNamespace("NAMESPACE",
				NotUseManagedFunctionSource.class.getName());
		SectionFunction functionFirst = namespace.addSectionFunction("FUNCTION", "TYPE");
		SectionFunction functionSecond = namespace.addSectionFunction("FUNCTION", "ANOTHER_TYPE");
		this.verifyMockObjects();

		// Should be the same section namespace
		assertEquals("Should be same section function on adding twice", functionFirst, functionSecond);
	}

	/**
	 * Ensure issue if add {@link SectionFunction} by same name twice within a
	 * {@link OfficeSection} with different {@link SectionFunctionNamespace}.
	 */
	public void testAddSectionFunctionTwiceByDifferentSectionFunctionNamespace() {

		// Record issue in adding the section function twice
		this.recordIssue("OFFICE.SECTION.FUNCTION", ManagedFunctionNodeImpl.class,
				"Managed Function FUNCTION already added");

		// Add the section function twice by different namespace
		this.replayMockObjects();
		SectionFunction functionFirst = this.node
				.addSectionFunctionNamespace("NAMESPACE_ONE", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("FUNCTION", "TYPE");
		SectionFunction functionSecond = this.node
				.addSectionFunctionNamespace("NAMESPACE_TWO", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("FUNCTION", "ANOTHER_TYPE");
		this.verifyMockObjects();

		// Should be the same section namespace
		assertEquals("Should be same section function on adding twice", functionFirst, functionSecond);
	}

	/**
	 * Ensure able to get {@link FunctionFlow} and {@link FunctionObject} instances
	 * from the {@link SectionFunction}.
	 */
	public void testSectionFunctionGetFlowsAndObjects() {
		// Ensure able to get flow/objects from section function
		this.replayMockObjects();

		// Obtain the section function
		SectionFunction function = this.node
				.addSectionFunctionNamespace("NAMESPACE", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("FUNCTION", "TYPE");

		// Ensure can get flow
		FunctionFlow flow = function.getFunctionFlow("FLOW");
		assertNotNull("Must have flow", flow);
		assertEquals("Incorrect flow name", "FLOW", flow.getFunctionFlowName());
		assertEquals("Should get same function flow again", flow, function.getFunctionFlow("FLOW"));
		assertNotSame("Should not be same flow for different name", flow, function.getFunctionFlow("ANOTHER"));

		// Ensure can get object
		FunctionObject object = function.getFunctionObject("OBJECT");
		assertNotNull("Must have object", object);
		assertEquals("Incorrect object name", "OBJECT", object.getFunctionObjectName());
		assertEquals("Should get same function object again", object, function.getFunctionObject("OBJECT"));
		assertNotSame("Should not be same object for different name", object, function.getFunctionObject("ANOTHER"));

		// Ensure can get escalation
		FunctionFlow escalation = function.getFunctionEscalation("ESCALATION");
		assertNotNull("Must have escalation", escalation);
		assertEquals("Incorrect escalation name", "ESCALATION", escalation.getFunctionFlowName());
		assertEquals("Should get same function flow again", escalation, function.getFunctionEscalation("ESCALATION"));
		assertNotSame("Should not be same escalation for different name", escalation,
				function.getFunctionEscalation("ANOTHER"));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can add a {@link SubSection}.
	 */
	public void testAddSubSection() {
		// Add two different sub sections verifying details
		this.replayMockObjects();
		SubSection subSection = this.node.addSubSection("SUB_SECTION", NotUseSectionSource.class.getName(),
				"SUB_SECTION_LOCATION");
		assertNotNull("Must have sub section", subSection);
		assertEquals("Incorrect sub section name", "SUB_SECTION", subSection.getSubSectionName());
		assertNotSame("Should obtain another sub section", subSection,
				this.node.addSubSection("ANOTHER", "not used", "not used"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if add {@link SubSection} by same name twice.
	 */
	public void testAddSubSectionTwice() {

		// Record issue in adding the sub section twice
		this.recordIssue("OFFICE.SECTION.SUB_SECTION", SectionNodeImpl.class, "Section SUB_SECTION already added");

		// Add the sub section twice
		this.replayMockObjects();
		SubSection subSectionFirst = this.node.addSubSection("SUB_SECTION", new NotUseSectionSource(), null);
		SubSection subSectionSecond = this.node.addSubSection("SUB_SECTION", new NotUseSectionSource(), null);
		this.verifyMockObjects();

		// Should be the same sub section
		assertEquals("Should be same sub section on adding twice", subSectionFirst, subSectionSecond);
	}

	/**
	 * Ensure able to get {@link SubSectionInput}, {@link SubSectionOutput} and
	 * {@link SubSectionObject} instances from the {@link SubSection}.
	 */
	public void testSubSectionGetInputsOutputsAndObjects() {
		// Ensure able to get inputs/outputs/objects from sub section
		this.replayMockObjects();

		// Obtain the sub section
		SubSection subSection = this.node.addSubSection("SUB_SECTION", NotUseSectionSource.class.getName(), "LOCATION");

		// Ensure can get input
		SubSectionInput input = subSection.getSubSectionInput("INPUT");
		assertNotNull("Must have input", input);
		assertEquals("Incorrect input name", "INPUT", input.getSubSectionInputName());
		assertEquals("Should get same input again", input, subSection.getSubSectionInput("INPUT"));
		assertNotSame("Should not be same input for different name", input, subSection.getSubSectionInput("ANOTHER"));

		// Ensure can get output
		SubSectionOutput output = subSection.getSubSectionOutput("OUTPUT");
		assertNotNull("Must have output", output);
		assertEquals("Incorrect output name", "OUTPUT", output.getSubSectionOutputName());
		assertEquals("Should get same output again", output, subSection.getSubSectionOutput("OUTPUT"));
		assertNotSame("Should not be same output for different name", output,
				subSection.getSubSectionOutput("ANOTHER"));

		// Ensure can get object
		SubSectionObject object = subSection.getSubSectionObject("OBJECT");
		assertNotNull("Must have output", object);
		assertEquals("Incorrect output name", "OBJECT", object.getSubSectionObjectName());
		assertEquals("Should get same object again", object, subSection.getSubSectionObject("OBJECT"));
		assertNotSame("Should not be same object for different name", object,
				subSection.getSubSectionObject("ANOTHER"));

		this.verifyMockObjects();
	}

	/**
	 * Ensures able to add a {@link SectionInput}, {@link SectionOutput} and
	 * {@link SectionObject} after obtaining them as {@link SubSectionInput},
	 * {@link SubSectionOutput} and {@link SubSectionObject} instances from the node
	 * as a {@link SubSection}.
	 */
	public void testSectionAddInputOutputObjectAfterSubSectionGet() {
		this.replayMockObjects();

		// Ensure sub section input the same as section input added after
		SubSectionInput subSectionInput = this.node.getSubSectionInput("INPUT");
		SectionInput sectionInput = this.node.addSectionInput("INPUT", String.class.getName());
		assertEquals("Inputs should be the same", subSectionInput, sectionInput);

		// Ensure sub section output the same as section output added after
		SubSectionOutput subSectionOutput = this.node.getSubSectionOutput("OUTPUT");
		SectionOutput sectionOutput = this.node.addSectionOutput("OUTPUT", Exception.class.getName(), true);
		assertEquals("Outputs should be the same", subSectionOutput, sectionOutput);

		// Ensure sub section object the same as section object added after
		SubSectionObject subSectionObject = this.node.getSubSectionObject("OBJECT");
		SectionObject sectionObject = this.node.addSectionObject("OBJECT", Connection.class.getName());
		assertEquals("Objects should be the same", subSectionObject, sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensure able to get a {@link OfficeSection} qualified name.
	 */
	public void testSectionQualifiedName() {

		// Create sub sections that provide qualified name
		SubSection subSection = this.node.addSubSection("SUB_SECTION", NotUseSectionSource.class.getName(),
				SECTION_LOCATION);

		// Obtain the section qualified name
		SectionNode subSectionNode = (SectionNode) subSection;
		String qualifiedName = subSectionNode.getSectionQualifiedName("NAMESPACE");

		// Validate qualified name
		assertEquals("Invalid section qualified name", "SECTION.SUB_SECTION.NAMESPACE", qualifiedName);
	}

	/**
	 * Ensure able to get a {@link Office} qualified name.
	 */
	public void testQualifiedName() {

		// Record obtaining the office qualified name
		this.recordReturn(this.office, this.office.getQualifiedName("SECTION"), "OFFICE.SECTION");
		this.replayMockObjects();

		// Create sub sections that provide qualified name
		SubSection subSection = this.node.addSubSection("SUB_SECTION", NotUseSectionSource.class.getName(),
				SECTION_LOCATION);

		// Obtain the section qualified name
		SectionNode subSectionNode = (SectionNode) subSection;
		String qualifiedName = subSectionNode.getQualifiedName("NAMESPACE");

		// Validate qualified name
		assertEquals("Invalid section qualified name", "OFFICE.SECTION.SUB_SECTION.NAMESPACE", qualifiedName);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link SectionInput} to the {@link SectionFunction}.
	 */
	public void testLinkSectionInputToSectionFunction() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.INPUT", SectionInputNodeImpl.class,
				"Section Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		SectionInput input = this.node.addSectionInput("INPUT", Object.class.getName());
		SectionFunctionNamespace namespace = this.node.addSectionFunctionNamespace("NAMESPACE",
				NotUseManagedFunctionSource.class.getName());
		SectionFunction function = namespace.addSectionFunction("FUNCTION", "TYPE");
		this.node.link(input, function);
		assertFlowLink("input -> function", input, function);

		// Ensure only can link once
		this.node.link(input, namespace.addSectionFunction("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", input, function);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link {@link SectionInput} to the {@link SubSectionInput}.
	 */
	public void testLinkSectionInputToSubSectionInput() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.INPUT", SectionInputNodeImpl.class,
				"Section Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		SectionInput input = this.node.addSectionInput("INPUT", Object.class.getName());
		SubSection subSection = this.node.addSubSection("SUB_SECTION", NotUseSectionSource.class.getName(), "LOCATION");
		SubSectionInput subSectionInput = subSection.getSubSectionInput("SUB_SECTION_INPUT");
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
		this.recordIssue("OFFICE.SECTION.INPUT", SectionInputNodeImpl.class,
				"Section Input INPUT linked more than once");

		this.replayMockObjects();

		// Link
		SectionInput input = this.node.addSectionInput("INPUT", Object.class.getName());
		SectionOutput output = this.node.addSectionOutput("OUTPUT", Object.class.getName(), false);
		this.node.link(input, output);
		assertFlowLink("input -> output", input, output);

		// Ensure only can link once
		this.node.link(input, this.node.addSectionOutput("ANOTHER", String.class.getName(), false));
		assertFlowLink("Can only link once", input, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link FunctionFlow} to the {@link SectionFunction}.
	 */
	public void testLinkFunctionFlowToSectionFunction() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.FUNCTION.FLOW", FunctionFlowNodeImpl.class,
				"Function Flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionFunction function = this.node
				.addSectionFunctionNamespace("NAMESPACE", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("FUNCTION", "TYPE");
		FunctionFlow flow = function.getFunctionFlow("FLOW");
		SectionFunction targetFunction = this.node
				.addSectionFunctionNamespace("TARGET", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("TARGET", "TYPE");
		this.node.link(flow, targetFunction, false);
		assertFlowLink("function flow -> function", flow, targetFunction);
		assertSpawnThreadState(flow, false);

		// Ensure only can link once
		this.node.link(flow, function, true);
		assertFlowLink("Can only link once", flow, targetFunction);
		assertSpawnThreadState(flow, false);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link FunctionFlow} to the {@link SubSectionInput}.
	 */
	public void testLinkFunctionFlowToSubSectionInput() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.FUNCTION.FLOW", FunctionFlowNodeImpl.class,
				"Function Flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionFunction function = this.node
				.addSectionFunctionNamespace("NAMESPACE", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("FUNCTION", "TYPE");
		FunctionFlow flow = function.getFunctionFlow("FLOW");
		SubSection subSection = this.node.addSubSection("SUB_SECTION", new NotUseSectionSource(), "LOCATION");
		SubSectionInput input = subSection.getSubSectionInput("INPUT");
		this.node.link(flow, input, true);
		assertFlowLink("function flow -> sub section input", flow, input);
		assertSpawnThreadState(flow, true);

		// Ensure only can link once
		this.node.link(flow, subSection.getSubSectionInput("ANOTHER"), false);
		assertFlowLink("Can only link once", flow, input);
		assertSpawnThreadState(flow, true);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link FunctionFlow} to the {@link SectionOutput}.
	 */
	public void testLinkFunctionFlowToSectionOutput() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.FUNCTION.FLOW", FunctionFlowNodeImpl.class,
				"Function Flow FLOW linked more than once");

		this.replayMockObjects();

		// Link (escalation)
		SectionFunction function = this.node
				.addSectionFunctionNamespace("NAMESPACE", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("FUNCTION", "TYPE");
		FunctionFlow flow = function.getFunctionEscalation("FLOW");
		SectionOutput output = this.node.addSectionOutput("OUTPUT", Exception.class.getName(), true);
		this.node.link(flow, output, true);
		assertFlowLink("function flow -> section output", flow, output);
		// Function Escalation always not spawn
		assertSpawnThreadState(flow, false);

		// Ensure only can link once
		this.node.link(flow, this.node.addSectionOutput("ANOTHER", Object.class.getName(), false), true);
		assertFlowLink("Can only link once", flow, output);
		assertSpawnThreadState(flow, false);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SectionFunction} to its next {@link SectionFunction}.
	 */
	public void testLinkSectionFunctionToNextSectionFunction() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.FUNCTION", ManagedFunctionNodeImpl.class,
				"Managed Function FUNCTION linked more than once");

		this.replayMockObjects();

		// Link
		SectionFunctionNamespace namespace = this.node.addSectionFunctionNamespace("NAMESPACE",
				NotUseManagedFunctionSource.class.getName());
		SectionFunction function = namespace.addSectionFunction("FUNCTION", "TYPE");
		SectionFunction nextFunction = namespace.addSectionFunction("NEXT", "TYPE");
		this.node.link(function, nextFunction);
		assertFlowLink("function -> next function", function, nextFunction);

		// Ensure only can link once
		this.node.link(function, namespace.addSectionFunction("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", function, nextFunction);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SectionFunction} to its next {@link SubSectionInput}.
	 */
	public void testLinkSectionFunctionToNextSubSectionInput() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.FUNCTION", ManagedFunctionNodeImpl.class,
				"Managed Function FUNCTION linked more than once");

		this.replayMockObjects();

		// Link
		SectionFunction function = this.node
				.addSectionFunctionNamespace("NAMESPACE", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("FUNCTION", "TYPE");
		SubSection subSection = this.node.addSubSection("SUB_SECTION", new NotUseSectionSource(), "LOCATION");
		SubSectionInput input = subSection.getSubSectionInput("INPUT");
		this.node.link(function, input);
		assertFlowLink("function -> next input", function, input);

		// Ensure only can link once
		this.node.link(function, subSection.getSubSectionInput("ANOTHER"));
		assertFlowLink("Can only link once", function, input);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SectionFunction} to the {@link SectionOutput}.
	 */
	public void testLinkSectionFunctionToNextSectionOutput() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.FUNCTION", ManagedFunctionNodeImpl.class,
				"Managed Function FUNCTION linked more than once");

		this.replayMockObjects();

		// Link
		SectionFunction function = this.node
				.addSectionFunctionNamespace("NAMESPACE", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("FUNCTION", "TYPE");
		SectionOutput output = this.node.addSectionOutput("OUTPUT", Object.class.getName(), false);
		this.node.link(function, output);
		assertFlowLink("function -> next output", function, output);

		// Ensure only can link once
		this.node.link(function, this.node.addSectionOutput("ANOTHER", String.class.getName(), false));
		assertFlowLink("Can only link once", function, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionOutput} to the {@link SectionFunction}.
	 */
	public void testLinkSubSectionOutputToSectionFunction() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.SUB_SECTION.OUTPUT", SectionOutputNodeImpl.class,
				"Section Output OUTPUT linked more than once");

		this.replayMockObjects();

		// Link
		SubSection subSection = this.node.addSubSection("SUB_SECTION", NotUseSectionSource.class.getName(),
				SECTION_LOCATION);
		SubSectionOutput output = subSection.getSubSectionOutput("OUTPUT");
		SectionFunctionNamespace namespace = this.node.addSectionFunctionNamespace("NAMESPACE",
				NotUseManagedFunctionSource.class.getName());
		SectionFunction function = namespace.addSectionFunction("FUNCTION", "TYPE");
		this.node.link(output, function);
		assertFlowLink("sub section output -> function", output, function);

		// Ensure only can link once
		this.node.link(output, namespace.addSectionFunction("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", output, function);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionOutput} to the {@link SubSectionInput}.
	 */
	public void testLinkSubSectionOutputToSubSectionInput() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.SUB_SECTION.OUTPUT", SectionOutputNodeImpl.class,
				"Section Output OUTPUT linked more than once");

		this.replayMockObjects();

		// Link
		SubSection subSection = this.node.addSubSection("SUB_SECTION", NotUseSectionSource.class.getName(),
				SECTION_LOCATION);
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
		this.recordIssue("OFFICE.SECTION.SUB_SECTION.OUTPUT", SectionOutputNodeImpl.class,
				"Section Output OUTPUT linked more than once");

		this.replayMockObjects();

		// Link
		SubSection subSection = this.node.addSubSection("SUB_SECTION", NotUseSectionSource.class.getName(),
				SECTION_LOCATION);
		SubSectionOutput output = subSection.getSubSectionOutput("OUTPUT");
		SectionOutput sectionOutput = this.node.addSectionOutput("OUTPUT", Object.class.getName(), false);
		this.node.link(output, sectionOutput);
		assertFlowLink("sub section output -> section output", output, sectionOutput);

		// Ensure only can link once
		this.node.link(output, this.node.addSectionOutput("ANOTHER", String.class.getName(), false));
		assertFlowLink("Can only link once", output, sectionOutput);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectFlow} to the {@link SectionFunction}.
	 */
	public void testLinkManagedObjectFlowToSectionFunction() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode();

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.MO.FLOW", ManagedObjectFlowNodeImpl.class,
				"Managed Object Source Flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObjectFlow flow = moSource.getSectionManagedObjectFlow("FLOW");
		SectionFunctionNamespace namespace = this.node.addSectionFunctionNamespace("NAMESPACE",
				NotUseManagedFunctionSource.class.getName());
		SectionFunction function = namespace.addSectionFunction("FUNCTION", "TYPE");
		this.node.link(flow, function);
		assertFlowLink("managed object flow -> section function", flow, function);

		// Ensure only can link once
		this.node.link(flow, namespace.addSectionFunction("ANOTHER", "TYPE"));
		assertFlowLink("Can only link once", flow, function);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link ManagedObjectFlow} to {@link SubSectionInput}.
	 */
	public void testLinkManagedObjectFlowToSubSectionInput() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode();

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.MO.FLOW", ManagedObjectFlowNodeImpl.class,
				"Managed Object Source Flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObjectFlow flow = moSource.getSectionManagedObjectFlow("FLOW");
		SubSection subSection = this.node.addSubSection("SUB_SECTION", NotUseSectionSource.class.getName(),
				SECTION_LOCATION);
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

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode();

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.MO.FLOW", ManagedObjectFlowNodeImpl.class,
				"Managed Object Source Flow FLOW linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObjectFlow flow = moSource.getSectionManagedObjectFlow("FLOW");
		SectionOutput output = this.node.addSectionOutput("OUTPUT", Object.class.getName(), false);
		this.node.link(flow, output);
		assertFlowLink("managed object flow -> section output", flow, output);

		// Ensure only can link once
		this.node.link(flow, this.node.addSectionOutput("ANOTHER", String.class.getName(), false));
		assertFlowLink("Can only link once", flow, output);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link FunctionObject} to the {@link SectionObject}.
	 */
	public void testLinkFunctionObjectToSectionObject() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.FUNCTION.OBJECT", FunctionObjectNodeImpl.class,
				"Function Object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		FunctionObject object = this.node
				.addSectionFunctionNamespace("NAMESPACE", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("FUNCTION", "TYPE").getFunctionObject("OBJECT");
		SectionObject sectionObject = this.node.addSectionObject("OUTPUT", Connection.class.getName());
		this.node.link(object, sectionObject);
		assertObjectLink("function object -> section object", object, sectionObject);

		// Ensure only can link once
		this.node.link(object, this.node.addSectionObject("ANOTHER", String.class.getName()));
		assertObjectLink("Can only link once", object, sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link FunctionObject} to the {@link SectionManagedObject}.
	 */
	public void testLinkFunctionObjectToSectionManagedObject() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode(5);

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.FUNCTION.OBJECT", FunctionObjectNodeImpl.class,
				"Function Object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		FunctionObject object = this.node
				.addSectionFunctionNamespace("NAMESPACE", NotUseManagedFunctionSource.class.getName())
				.addSectionFunction("FUNCTION", "TYPE").getFunctionObject("OBJECT");
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObject mo = moSource.addSectionManagedObject("MO", ManagedObjectScope.FUNCTION);
		this.node.link(object, mo);
		assertObjectLink("function object -> section managed object", object, mo);

		// Ensure only can link once
		this.node.link(object, moSource.addSectionManagedObject("ANOTHER", ManagedObjectScope.THREAD));
		assertObjectLink("Can only link once", object, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the {@link SectionObject}.
	 */
	public void testLinkSubSectionObjectToSectionObject() {

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.SUB_SECTION.OBJECT", SectionObjectNodeImpl.class,
				"Section Object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		SubSectionObject object = this.node.addSubSection("SUB_SECTION", new NotUseSectionSource(), SECTION_LOCATION)
				.getSubSectionObject("OBJECT");
		SectionObject sectionObject = this.node.addSectionObject("OUTPUT", Connection.class.getName());
		this.node.link(object, sectionObject);
		assertObjectLink("sub section object -> section object", object, sectionObject);

		// Ensure only can link once
		this.node.link(object, this.node.addSectionObject("ANOTHER", String.class.getName()));
		assertObjectLink("Can only link once", object, sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the
	 * {@link SectionManagedObject}.
	 */
	public void testLinkSubSectionObjectToSectionManagedObject() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode(5);

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.SUB_SECTION.OBJECT", SectionObjectNodeImpl.class,
				"Section Object OBJECT linked more than once");

		this.replayMockObjects();

		// Link
		SubSectionObject object = this.node.addSubSection("SUB_SECTION", new NotUseSectionSource(), SECTION_LOCATION)
				.getSubSectionObject("OBJECT");
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObject mo = moSource.addSectionManagedObject("MO", ManagedObjectScope.PROCESS);
		this.node.link(object, mo);
		assertObjectLink("sub section object -> section managed object", object, mo);

		// Ensure only can link once
		this.node.link(object, moSource.addSectionManagedObject("ANOTHER", ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", object, mo);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the {@link SectionObject}.
	 */
	public void testLinkManagedObjectDependencyToSectionObject() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode(3);

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.MO.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObject mo = moSource.addSectionManagedObject("MO", ManagedObjectScope.FUNCTION);
		SectionManagedObjectDependency dependency = mo.getSectionManagedObjectDependency("DEPENDENCY");
		SectionObject sectionObject = this.node.addSectionObject("OBJECT", Connection.class.getName());
		this.node.link(dependency, sectionObject);
		assertObjectLink("managed object dependency -> section object", dependency, sectionObject);

		// Ensure only can link once
		this.node.link(dependency, this.node.addSectionObject("ANOTHER", Object.class.getName()));
		assertObjectLink("Can only link once", dependency, sectionObject);

		this.verifyMockObjects();
	}

	/**
	 * Ensures can link {@link SubSectionObject} to the
	 * {@link SectionManagedObject}.
	 */
	public void testLinkManagedObjectDependencyToSectionManagedObject() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode(8);

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.MO.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObject mo = moSource.addSectionManagedObject("MO", ManagedObjectScope.FUNCTION);
		SectionManagedObjectDependency dependency = mo.getSectionManagedObjectDependency("DEPENDENCY");
		SectionManagedObjectSource moSourceTarget = this.node.addSectionManagedObjectSource("MO_SOURCE_TARGET",
				NotUseManagedObjectSource.class.getName());
		SectionManagedObject moTarget = moSourceTarget.addSectionManagedObject("MO_TARGET", ManagedObjectScope.PROCESS);
		this.node.link(dependency, moTarget);
		assertObjectLink("managed object dependency -> section managed object", dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addSectionManagedObject("ANOTHER", ManagedObjectScope.FUNCTION));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link Input {@link ManagedObjectDependency} to the
	 * {@link SectionObject}.
	 */
	public void testLinkInputManagedObjectDependencyToSectionObject() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode();

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.MO_SOURCE.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE", (String) null);
		SectionManagedObjectDependency dependency = moSource.getInputSectionManagedObjectDependency("DEPENDENCY");
		SectionObject moTarget = this.node.addSectionObject("MO_TARGET", Connection.class.getName());
		this.node.link(dependency, moTarget);
		assertObjectLink("input managed object dependency -> office floor managed object", dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, this.node.addSectionObject("ANOTHER", Connection.class.getName()));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Ensure can link Input {@link ManagedObjectDependency} to the
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkInputManagedObjectDependencyToSectionManagedObject() {

		// Record obtaining the OfficeFloor for the managed objects
		this.recordOfficeFloorNode(6);

		// Record already being linked
		this.recordIssue("OFFICE.SECTION.MO_SOURCE.DEPENDENCY", ManagedObjectDependencyNodeImpl.class,
				"Managed Object Dependency DEPENDENCY linked more than once");

		this.replayMockObjects();

		// Link
		SectionManagedObjectSource moSource = this.node.addSectionManagedObjectSource("MO_SOURCE", (String) null);
		SectionManagedObjectDependency dependency = moSource.getInputSectionManagedObjectDependency("DEPENDENCY");
		SectionManagedObjectSource moSourceTarget = this.node.addSectionManagedObjectSource("MO_SOURCE_TARGET",
				(String) null);
		SectionManagedObject moTarget = moSourceTarget.addSectionManagedObject("MO_TARGET", ManagedObjectScope.PROCESS);
		this.node.link(dependency, moTarget);
		assertObjectLink("input managed object dependency -> office floor managed object", dependency, moTarget);

		// Ensure only can link once
		this.node.link(dependency, moSourceTarget.addSectionManagedObject("ANOTHER", ManagedObjectScope.PROCESS));
		assertObjectLink("Can only link once", dependency, moTarget);

		this.verifyMockObjects();
	}

	/**
	 * Records obtaining the {@link OfficeFloorNode} once.
	 */
	private void recordOfficeFloorNode() {
		this.recordOfficeFloorNode(1);
	}

	/**
	 * Records obtaining the {@link OfficeFloorNode} multiple times.
	 * 
	 * @param count
	 */
	private void recordOfficeFloorNode(int count) {
		for (int i = 0; i < count; i++) {
			this.recordReturn(this.office, this.office.getOfficeFloorNode(), this.officeFloor);
		}
	}

	/**
	 * Records qualifying the {@link SectionNode} name.
	 */
	private void recordSectionQualifiedName() {
		this.recordReturn(this.office, this.office.getQualifiedName("SECTION"), "OFFICE.SECTION");
	}

	/**
	 * Records an issue handling qualifying the {@link SectionNode} name.
	 * 
	 * @param name             Name.
	 * @param nodeType         {@link Node} {@link Class}.
	 * @param issueDescription {@link CompilerIssue} description.
	 */
	private void recordIssue(String name, Class<? extends Node> nodeType, String issueDescription) {
		this.recordSectionQualifiedName();
		this.issues.recordIssue(name, nodeType, issueDescription);
	}

	/**
	 * Asserts the spawn {@link ThreadState} for the {@link FunctionFlow}.
	 * 
	 * @param functionFlow       {@link FunctionFlow} to check.
	 * @param isSpawnThreadState Expected spawn {@link ThreadState}.
	 */
	private static void assertSpawnThreadState(FunctionFlow functionFlow, boolean isSpawnThreadState) {
		assertTrue("Function flow must be " + FunctionFlowNode.class, functionFlow instanceof FunctionFlowNode);
		assertEquals("Incorrect spawn thread state", isSpawnThreadState,
				((FunctionFlowNode) functionFlow).isSpawnThreadState());
	}

	/**
	 * {@link ManagedObjectSource} that should not have its methods invoked.
	 */
	@TestSource
	public static class NotUseManagedObjectSource implements ManagedObjectSource<Indexed, Indexed> {

		/*
		 * ===================== ManagedObjectSource ======================
		 */

		@Override
		public ManagedObjectSourceSpecification getSpecification() {
			fail("Should not use ManagedObjectSource");
			return null;
		}

		@Override
		public ManagedObjectSourceMetaData<Indexed, Indexed> init(ManagedObjectSourceContext<Indexed> context)
				throws Exception {
			fail("Should not use ManagedObjectSource");
			return null;
		}

		@Override
		public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {
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
	 * {@link ManagedFunctionSource} that should not have its methods invoked.
	 */
	@TestSource
	public static class NotUseManagedFunctionSource implements ManagedFunctionSource {

		/*
		 * ================== ManagedFunctionSource ==================
		 */

		@Override
		public ManagedFunctionSourceSpecification getSpecification() {
			fail("Should not use FunctionNamespaceSource");
			return null;
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceBuilder,
				ManagedFunctionSourceContext context) throws Exception {
			fail("Should not use ManagedFunctionSource");
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
		public void sourceSection(SectionDesigner sectionBuilder, SectionSourceContext context) throws Exception {
			fail("Should not use SectionSource");
		}
	}
}
