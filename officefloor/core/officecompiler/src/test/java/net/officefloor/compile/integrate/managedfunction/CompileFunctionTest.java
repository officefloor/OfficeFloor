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
package net.officefloor.compile.integrate.managedfunction;

import net.officefloor.compile.impl.structure.ManagedFunctionNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.managedfunction.clazz.ClassFunctionFactory;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.ManagedFunctionParameterFactory;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests compiling a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileFunctionTest extends AbstractCompileTestCase {

	/**
	 * Tests compiling a simple {@link ManagedFunction}.
	 */
	public void testSimpleFunction() {

		// Record loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION", "OFFICE_TEAM");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} with a differentiator.
	 */
	public void testDifferentiatorFunction() {

		// Record loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION", "OFFICE_TEAM");
		this.record_functionBuilder_setDifferentiator(DifferentiatorManagedFunctionSource.DIFFERENTIATOR);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures issue if {@link FunctionFlow} not linked.
	 */
	public void testFunctionFlowNotLinked() {

		// Record loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION", "OFFICE_TEAM");
		this.issues.recordIssue("FUNCTION", ManagedFunctionNodeImpl.class, "Flow flow is not linked to a FunctionNode");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} linking a {@link Flow} to
	 * another {@link ManagedFunction} on the same {@link FunctionNamespace}.
	 */
	public void testLinkFlowToFunctionOnSameFunctionNamespace() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> functionOne = this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION_A",
				"OFFICE_TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION_B", "OFFICE_TEAM");
		functionOne.linkFlow(0, "SECTION.NAMESPACE.FUNCTION_B", String.class, true);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} linking a {@link Flow} to
	 * different {@link FunctionNamespace} in the same {@link OfficeSection}.
	 */
	public void testLinkFlowToFunctionOnDifferentFunctionNamespaceInSameSection() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.NAMESPACE_A.FUNCTION_A",
				"OFFICE_TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE_B.FUNCTION_B", "OFFICE_TEAM");
		function.linkFlow(0, "SECTION.NAMESPACE_B.FUNCTION_B", String.class, false);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} linking a {@link Flow} to a
	 * {@link ManagedFunction} in a different {@link SubSection}.
	 */
	public void testLinkFlowToFunctionInDifferentSubSection() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this
				.record_officeBuilder_addFunction("SECTION.SUB_SECTION_A.NAMESPACE.FUNCTION", "OFFICE_TEAM");
		this.record_officeBuilder_addFunction("SECTION.SUB_SECTION_B.NAMESPACE.INPUT", "OFFICE_TEAM");
		function.linkFlow(0, "SECTION.SUB_SECTION_B.NAMESPACE.INPUT", String.class, true);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} linking a {@link Flow} to a
	 * {@link ManagedFunction} in a different {@link OfficeSection}.
	 */
	public void testLinkFlowToFunctionInDifferentOfficeSection() {

		// Record loading section types
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION_A.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		this.record_officeBuilder_addFunction("SECTION_B.NAMESPACE.INPUT", "OFFICE_TEAM");
		function.linkFlow(0, "SECTION_B.NAMESPACE.INPUT", String.class, true);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures compiling a {@link ManagedFunction} linking to next
	 * {@link ManagedFunction} on the same {@link FunctionNamespace}.
	 */
	public void testLinkFunctionNextToFunctionOnSameFunctionNamespace() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION_A",
				"OFFICE_TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION_B", "OFFICE_TEAM");
		function.setNextFunction("SECTION.NAMESPACE.FUNCTION_B", Integer.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures compiling a {@link ManagedFunction} linking to next
	 * {@link ManagedFunction} in a different {@link OfficeSection}.
	 */
	public void testLinkFunctionNextToFunctionInDifferentOfficeSection() {

		// Record loading section types
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION_A.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		this.record_officeBuilder_addFunction("SECTION_B.NAMESPACE.INPUT", "OFFICE_TEAM");
		function.setNextFunction("SECTION_B.NAMESPACE.INPUT", Integer.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures compiling a {@link ManagedFunction} linking to next
	 * {@link ManagedFunction} that is through a parent {@link SubSection}.
	 */
	public void testLinkFunctionNextToFunctionThroughParentSection() {

		// Record loading section types
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this
				.record_officeBuilder_addFunction("SECTION_A.desk-one.NAMESPACE.FUNCTION", "OFFICE_TEAM");
		this.record_officeBuilder_addFunction("SECTION_B.NAMESPACE.INPUT", "OFFICE_TEAM");
		function.setNextFunction("SECTION_B.NAMESPACE.INPUT", Integer.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures compiling a {@link ManagedFunction} linking to next
	 * {@link ManagedFunction} that is through a child {@link SubSection}.
	 */
	public void testLinkFunctionNextToFunctionThroughChildSection() {

		// Record loading section types
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION_A.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		this.record_officeBuilder_addFunction("SECTION_B.desk-two.NAMESPACE.INPUT", "OFFICE_TEAM");
		function.setNextFunction("SECTION_B.desk-two.NAMESPACE.INPUT", Integer.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures issue if {@link FunctionObject} not linked.
	 */
	public void testFunctionObjectNotLinked() throws Exception {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION", "OFFICE_TEAM");
		this.issues.recordIssue("FUNCTION", ManagedFunctionNodeImpl.class,
				"Object " + CompileManagedObject.class.getName() + " is not linked to a BoundManagedObjectNode");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} link the
	 * {@link FunctionObject} as a parameter.
	 */
	public void testLinkFunctionObjectAsParameter() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		function.linkParameter(0, CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} linking the
	 * {@link FunctionObject} to a {@link OfficeFloorManagedObject}.
	 */
	public void testLinkFunctionObjectToOfficeFloorManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		function.linkManagedObject(0, "MANAGED_OBJECT", CompileManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} linking the
	 * {@link FunctionObject} to an {@link OfficeFloorInputManagedObject}.
	 */
	public void testLinkFunctionObjectToOfficeFloorInputManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_registerTeam("OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		function.linkManagedObject(0, "INPUT_MANAGED_OBJECT", InputManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", InputManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MANAGED_OBJECT");
		managingOffice.linkProcess(0, "SECTION.NAMESPACE.FUNCTION");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} linking the
	 * {@link FunctionObject} to an {@link OfficeManagedObject}.
	 */
	public void testLinkFunctionObjectToOfficeManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		function.linkManagedObject(0, "OFFICE.MANAGED_OBJECT", CompileManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} linking the
	 * {@link FunctionObject} to a {@link SectionManagedObject}.
	 */
	public void testLinkFunctionObjectToSectionManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		office.registerManagedObjectSource("OFFICE.SECTION.MANAGED_OBJECT", "OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.MANAGED_OBJECT");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.DESK.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		function.linkManagedObject(0, "OFFICE.SECTION.MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} linking the
	 * {@link FunctionObject} to a {@link DeskManagedObject}.
	 */
	public void testLinkFunctionObjectToDeskManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.DESK.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		function.linkManagedObject(0, "OFFICE.SECTION.DESK.MANAGED_OBJECT", CompileManagedObject.class);
		office.registerManagedObjectSource("OFFICE.SECTION.DESK.MANAGED_OBJECT",
				"OFFICE.SECTION.DESK.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.SECTION.DESK.MANAGED_OBJECT",
				"OFFICE.SECTION.DESK.MANAGED_OBJECT");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.DESK.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures compiling a {@link FunctionObject} linking
	 * {@link OfficeManagedObject} that is through a parent {@link SubSection}.
	 */
	public void testLinkFunctionObjectThroughParentSection() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.DESK.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		function.linkManagedObject(0, "MANAGED_OBJECT", CompileManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} linking a {@link Escalation} to
	 * another {@link ManagedFunction} on the same {@link FunctionNamespace}.
	 */
	public void testLinkEscalationToFunctionOnSameFunctionNamespace() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION_A",
				"OFFICE_TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION_B", "OFFICE_TEAM");
		function.addEscalation(Exception.class, "SECTION.NAMESPACE.FUNCTION_B");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} linking a {@link Escalation} to
	 * a {@link ManagedFunction} in a different {@link OfficeSection}.
	 */
	public void testLinkEscalationToFunctionInDifferentOfficeSection() {

		// Record loading section types
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION_A.NAMESPACE.FUNCTION",
				"OFFICE_TEAM");
		this.record_officeBuilder_addFunction("SECTION_B.NAMESPACE.INPUT", "OFFICE_TEAM");
		function.addEscalation(Exception.class, "SECTION_B.NAMESPACE.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures issue if {@link ManagedFunctionEscalationType} not linked.
	 */
	public void testEscalationNotPropagatedToOffice() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION", "OFFICE_TEAM");
		this.issues.recordIssue("FUNCTION", ManagedFunctionNodeImpl.class,
				"Escalation " + Exception.class.getName() + " not handled by a Function nor propagated to the Office");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} that propagates the
	 * {@link Escalation} to the {@link Office}.
	 */
	public void testEscalationPropagatedToOffice() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE.FUNCTION", "OFFICE_TEAM");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} linking a {@link Flow} to a
	 * {@link ManagedFunction} in a different {@link OfficeSection}.
	 */
	public void testLinkStartToOfficeSectionInput() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addFunction("SECTION.NAMESPACE.INPUT", "OFFICE_TEAM");
		this.record_officeBuilder_addStartupFunction("SECTION.NAMESPACE.INPUT");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * s {@link FlowInterface} for {@link CompileFunctionFunctionNamespace}.
	 */
	@FlowInterface
	public static interface Flows {

		void flow(String parameter);
	}

	/**
	 * Class for {@link ClassManagedFunctionSource}.
	 */
	public static class CompileFunctionFunctionNamespace {

		public void simpleFunction() {
			fail("Should not be invoked in compiling");
		}

		public void flowFunction(Flows flows) {
			fail("Should not be invoked in compiling");
		}

		public Integer nextFunction() {
			fail("Should not be invoked in compiling");
			return null;
		}

		public void objectFunction(CompileManagedObject object) {
			fail("Should not be invoked in compiling");
		}

		public void inputObjectFunction(InputManagedObject object) {
			fail("Should not be invoked in compiling");
		}

		public void escalationFunction() throws Exception {
			fail("Should not be invoked in compiling");
		}
	}

	/**
	 * {@link ManagedFunctionSource} to load differentiator for
	 * {@link ManagedFunction}.
	 */
	public static class DifferentiatorManagedFunctionSource extends AbstractManagedFunctionSource {

		public static final String DIFFERENTIATOR = "DIFFERENTIATOR";

		/*
		 * ================== ManagedFunctionSource ==================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceBuilder,
				ManagedFunctionSourceContext context) throws Exception {
			ManagedFunctionTypeBuilder<Indexed, Indexed> function = namespaceBuilder.addManagedFunctionType("function",
					new ClassFunctionFactory(CompileFunctionFunctionNamespace.class.getConstructor(new Class[0]),
							CompileFunctionFunctionNamespace.class.getMethod("simpleFunction"), new ManagedFunctionParameterFactory[0]),
					Indexed.class, Indexed.class);
			function.setDifferentiator(DIFFERENTIATOR);
		}
	}

	/**
	 * Class for {@link ClassManagedObjectSource}.
	 */
	public static class CompileManagedObject {
		// No dependencies as focused on testing function
	}

	/**
	 * Class for {@link ClassManagedObjectSource}.
	 */
	public static class InputManagedObject {

		@FlowInterface
		public static interface InputProcesses {
			void doProcess(Integer parameter);
		}

		InputProcesses processes;
	}

}