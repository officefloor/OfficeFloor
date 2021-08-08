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

package net.officefloor.compile.integrate.managedfunction;

import net.officefloor.compile.impl.structure.FunctionFlowNodeImpl;
import net.officefloor.compile.impl.structure.FunctionObjectNodeImpl;
import net.officefloor.compile.impl.structure.ManagedFunctionNodeImpl;
import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;
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
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.method.MethodFunctionFactory;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} with a responsible {@link Team}.
	 */
	public void testAssignFunctionTeam() {

		// Record loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addTeam("TEAM", new OnePersonTeamSource());
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION").setResponsibleTeam("OFFICE_TEAM");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} with an annotation.
	 */
	public void testAnnotatedFunction() {

		// Record loading section type
		this.issues.recordCaptureIssues(false);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		this.record_functionBuilder_addAnnotation(AnnotatedManagedFunctionSource.ANNOTATION);

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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		this.issues.recordIssue("OFFICE.SECTION.FUNCTION.flow", FunctionFlowNodeImpl.class,
				"Function Flow flow is not linked to a ManagedFunctionNode");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} linking a {@link Flow} to another
	 * {@link ManagedFunction} on the same {@link FunctionNamespace}.
	 */
	public void testLinkFlowToFunctionOnSameFunctionNamespace() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> functionOne = this.record_officeBuilder_addFunction("SECTION", "FUNCTION_A");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION_B");
		functionOne.linkFlow(0, "SECTION.FUNCTION_B", String.class, true);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} linking a {@link Flow} to different
	 * {@link FunctionNamespace} in the same {@link OfficeSection}.
	 */
	public void testLinkFlowToFunctionOnDifferentFunctionNamespaceInSameSection() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "FUNCTION_A");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION_B");
		function.linkFlow(0, "SECTION.FUNCTION_B", String.class, false);

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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.SUB_SECTION_A",
				"FUNCTION");
		this.record_officeBuilder_addFunction("SECTION.SUB_SECTION_B", "INPUT");
		function.linkFlow(0, "SECTION.SUB_SECTION_B.INPUT", String.class, true);

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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION_A", "FUNCTION");
		this.record_officeBuilder_addFunction("SECTION_B", "INPUT");
		function.linkFlow(0, "SECTION_B.INPUT", String.class, true);

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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "FUNCTION_A");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION_B");
		function.setNextFunction("SECTION.FUNCTION_B", Integer.class);

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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION_A", "FUNCTION");
		this.record_officeBuilder_addFunction("SECTION_B", "INPUT");
		function.setNextFunction("SECTION_B.INPUT", Integer.class);

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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION_A.section-one",
				"FUNCTION");
		this.record_officeBuilder_addFunction("SECTION_B", "INPUT");
		function.setNextFunction("SECTION_B.INPUT", Integer.class);

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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION_A", "FUNCTION");
		this.record_officeBuilder_addFunction("SECTION_B.section-two", "INPUT");
		function.setNextFunction("SECTION_B.section-two.INPUT", Integer.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensures issue if {@link FunctionObject} not linked.
	 */
	public void testFunctionObjectNotLinked() throws Exception {

		// Record loading section type
		CompilerIssue[] capturedIssues = this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor (fails on loading type, so no build)
		this.issues.recordIssue("OFFICE.SECTION.FUNCTION." + Node.escape(CompileManagedObject.class.getName()),
				FunctionObjectNodeImpl.class,
				"Function Object " + CompileManagedObject.class.getName() + " is not linked to a DependentObjectNode");
		this.issues.recordIssue("OFFICE", OfficeNodeImpl.class, "Failure loading OfficeSectionType from source SECTION",
				capturedIssues);

		// Compile the OfficeFloor
		this.compile(false);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} link the {@link FunctionObject} as
	 * a parameter.
	 */
	public void testLinkFunctionObjectAsParameter() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		function.linkParameter(0, CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} linking the {@link FunctionObject}
	 * to a {@link OfficeFloorManagedObject}.
	 */
	public void testLinkFunctionObjectToOfficeFloorManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		function.linkManagedObject(0, "MANAGED_OBJECT", CompileManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} linking the {@link FunctionObject}
	 * to an {@link OfficeFloorInputManagedObject}.
	 */
	public void testLinkFunctionObjectToOfficeFloorInputManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		function.linkManagedObject(0, "INPUT_MANAGED_OBJECT", InputManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", InputManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MANAGED_OBJECT");
		managingOffice.linkFlow(0, "SECTION.FUNCTION");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} linking the {@link FunctionObject}
	 * to an {@link OfficeManagedObject}.
	 */
	public void testLinkFunctionObjectToOfficeManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		function.linkManagedObject(0, "OFFICE.MANAGED_OBJECT", CompileManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject("OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} linking the {@link FunctionObject}
	 * to a {@link SectionManagedObject}.
	 */
	public void testLinkFunctionObjectToParentSectionManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("OFFICE.SECTION.MANAGED_OBJECT", "OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.MANAGED_OBJECT");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.SECTION", "FUNCTION");
		function.linkManagedObject(0, "OFFICE.SECTION.MANAGED_OBJECT", CompileManagedObject.class);

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link ManagedFunction} linking the {@link FunctionObject}
	 * to a {@link DeskManagedObject}.
	 */
	public void testLinkFunctionObjectToSectionManagedObject() {

		// Record loading section type
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.SECTION", "FUNCTION");
		function.linkManagedObject(0, "OFFICE.SECTION.SECTION.MANAGED_OBJECT", CompileManagedObject.class);
		office.registerManagedObjectSource("OFFICE.SECTION.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.SECTION.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("OFFICE.SECTION.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.SECTION.MANAGED_OBJECT");
		this.record_officeFloorBuilder_addManagedObject("OFFICE.SECTION.SECTION.MANAGED_OBJECT_SOURCE",
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
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice("OFFICE");
		office.registerManagedObjectSource("MANAGED_OBJECT", "MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT", "MANAGED_OBJECT");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION.SECTION", "FUNCTION");
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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION", "FUNCTION_A");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION_B");
		function.addEscalation(Exception.class, "SECTION.FUNCTION_B");

		// Compile the OfficeFloor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link ManagedFunction} linking a {@link Escalation} to a
	 * {@link ManagedFunction} in a different {@link OfficeSection}.
	 */
	public void testLinkEscalationToFunctionInDifferentOfficeSection() {

		// Record loading section types
		this.issues.recordCaptureIssues(true);
		this.issues.recordCaptureIssues(true);

		// Record building the OfficeFloor
		this.record_init();
		this.record_officeFloorBuilder_addOffice("OFFICE");
		ManagedFunctionBuilder<?, ?> function = this.record_officeBuilder_addFunction("SECTION_A", "FUNCTION");
		this.record_officeBuilder_addFunction("SECTION_B", "INPUT");
		function.addEscalation(Exception.class, "SECTION_B.INPUT");

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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");
		this.issues.recordIssue("OFFICE.SECTION.FUNCTION", ManagedFunctionNodeImpl.class,
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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "FUNCTION");

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
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_addFunction("SECTION", "INPUT");
		this.record_officeBuilder_addStartupFunction("SECTION.INPUT", null);

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
	public static class CompileFunctionClass {

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
	 * {@link ManagedFunctionSource} to load annotation for {@link ManagedFunction}.
	 */
	public static class AnnotatedManagedFunctionSource extends AbstractManagedFunctionSource {

		public static final String ANNOTATION = "ANNOTATION";

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
					Indexed.class, Indexed.class);
			function.setFunctionFactory(new MethodFunctionFactory((objectContext) -> new CompileFunctionClass(),
					CompileFunctionClass.class.getMethod("simpleFunction"), new ClassDependencyFactory[0]));
			function.addAnnotation(ANNOTATION);
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
