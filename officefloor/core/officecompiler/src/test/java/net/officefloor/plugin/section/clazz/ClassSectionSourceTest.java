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
package net.officefloor.plugin.section.clazz;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;
import java.sql.SQLException;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.managedfunction.clazz.NonFunctionMethod;
import net.officefloor.plugin.managedfunction.clazz.Qualifier;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObject;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;

/**
 * Tests the {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * {@link SectionManagedObject} for the {@link ClassManagedObject}.
	 */
	private SectionManagedObject objectManagedObject;

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		// No specification as uses location for class
		SectionLoaderUtil.validateSpecification(ClassSectionSource.class);
	}

	/**
	 * Ensure can provide {@link SectionInput}.
	 */
	public void testInput() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockInputSection.class,
				this.configureClassSectionFunction("doInput"));
		expected.addSectionInput("doInput", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class, MockInputSection.class.getName());
	}

	/**
	 * Section with only an input.
	 */
	public static class MockInputSection {
		public void doInput() {
		}
	}

	/**
	 * Ensure ignore methods annotated with {@link NonFunctionMethod}.
	 */
	public void testIgnoreNonFunctionMethods() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockIgnoreInputSection.class,
				this.configureClassSectionFunction("includedInput"));
		expected.addSectionInput("includedInput", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class, MockIgnoreInputSection.class.getName());
	}

	/**
	 * Section with methods to not be {@link ManagedFunction} instances.
	 */
	public static class MockIgnoreInputSection {
		public void includedInput() {
		}

		@NonFunctionMethod
		public void nonIncludedInput() {
		}

		@NonFunctionMethod
		public void nonIncludedStaticInput() {
		}
	}

	/**
	 * Ensure inherit methods by name.
	 */
	public void testInheritFunctionMethods() {

		// Ensure inheritance
		assertTrue("Invalid test if not extending", (new MockChildSection()) instanceof MockParentSection);

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockChildSection.class, (designer, namespace) -> {
			SectionFunction function = this.addClassSectionFunction(designer, namespace, "function", "function");
			function.getFunctionObject(Integer.class.getName()).flagAsParameter();
		});
		expected.addSectionInput("function", Integer.class.getName());

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class, MockChildSection.class.getName());
	}

	/**
	 * Parent section.
	 */
	public static class MockParentSection {
		public String function(@Parameter String parameter) {
			return parameter;
		}
	}

	/**
	 * Child section.
	 */
	public static class MockChildSection extends MockParentSection {
		public void function(@Parameter Integer parameter) {
		}
	}

	/**
	 * Ensure and provide {@link SectionOutput}.
	 */
	public void testOutput() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockOutputSection.class,
				this.configureClassSectionFunction("doInput"));
		expected.addSectionInput("doInput", null);
		expected.addSectionOutput("doOutput", null, false);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class, MockOutputSection.class.getName());
	}

	/**
	 * Section with an output.
	 */
	public static class MockOutputSection {
		@NextFunction("doOutput")
		public void doInput() {
		}
	}

	/**
	 * Ensure can provide {@link SectionOutput} via {@link FlowInterface}.
	 */
	public void testFlowInterface() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockFlowInterfaceSection.class,
				this.configureClassSectionFunction("doInput"));
		expected.addSectionInput("doInput", null);
		expected.addSectionOutput("doOutput", null, false);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class, MockFlowInterfaceSection.class.getName());
	}

	/**
	 * Mock {@link FlowInterface} for the {@link MockFlowInterfaceSection}.
	 */
	@FlowInterface
	public static interface MockFlowInterface {
		void doOutput();
	}

	/**
	 * Section with an {@link FlowInterface}.
	 */
	public static class MockFlowInterfaceSection {
		public void doInput(MockFlowInterface flows) {
		}
	}

	/**
	 * Ensure can provide {@link SectionOutput} for escalation.
	 */
	public void testEscalation() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockEscalationSection.class,
				this.configureClassSectionFunction("doInput", "doInput"));
		expected.addSectionInput("doInput", null);
		expected.addSectionOutput(SQLException.class.getName(), SQLException.class.getName(), true);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class, MockEscalationSection.class.getName());
	}

	/**
	 * Section with an escalation.
	 */
	public static class MockEscalationSection {
		public void doInput() throws SQLException {
		}
	}

	/**
	 * Ensure can provide parameter and argument types.
	 */
	public void testParameterArgument() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockParameterArgumentSection.class,
				(designer, namespace) -> {
					SectionFunction function = this.addClassSectionFunction(designer, namespace, "doInput", "doInput");
					function.getFunctionObject(String.class.getName()).flagAsParameter();
				});
		expected.addSectionInput("doInput", String.class.getName());
		expected.addSectionOutput("doOutput", Integer.class.getName(), false);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockParameterArgumentSection.class.getName());
	}

	/**
	 * Section with parameter and arguments.
	 */
	public static class MockParameterArgumentSection {
		@NextFunction("doOutput")
		public Integer doInput(@Parameter String parameter) {
			return null;
		}
	}

	/**
	 * Ensure can provide {@link SectionObject}.
	 */
	public void testObject() {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockObjectSection.class, (designer, namespace) -> {
			SectionFunction function = this.addClassSectionFunction(designer, namespace, "doInput", "doInput");
			FunctionObject functionObject = function.getFunctionObject(Connection.class.getName());
			SectionObject sectionObject = designer.addSectionObject(Connection.class.getName(),
					Connection.class.getName());
			designer.link(functionObject, sectionObject);
		});
		expected.addSectionInput("doInput", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class, MockObjectSection.class.getName());
	}

	/**
	 * Section with object.
	 */
	public static class MockObjectSection {
		public void doInput(Connection connection) {
		}
	}

	/**
	 * Ensure can provide qualified {@link SectionObject}.
	 */
	public void testQualifiedObject() {

		final String QUALIFIED_NAME = MockQualification.class.getName() + "-" + Connection.class.getName();
		final String UNQUALIFIED_NAME = Connection.class.getName();

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockQualifiedObjectSection.class,
				(designer, namespace) -> {
					SectionFunction function = this.addClassSectionFunction(designer, namespace, "doInput", "doInput");

					// Qualified dependency
					FunctionObject qualifiedFunctionObject = function.getFunctionObject(QUALIFIED_NAME);
					SectionObject qualifiedSectionObject = designer.addSectionObject(QUALIFIED_NAME,
							Connection.class.getName());
					qualifiedSectionObject.setTypeQualifier(MockQualification.class.getName());
					designer.link(qualifiedFunctionObject, qualifiedSectionObject);

					// Unqualified dependency
					FunctionObject unqualifiedFunctionObject = function.getFunctionObject(UNQUALIFIED_NAME);
					SectionObject unqualifiedSectionObject = designer.addSectionObject(UNQUALIFIED_NAME,
							Connection.class.getName());
					designer.link(unqualifiedFunctionObject, unqualifiedSectionObject);
				});
		expected.addSectionInput("doInput", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockQualifiedObjectSection.class.getName());
	}

	/**
	 * Mock qualification.
	 */
	@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface MockQualification {
	}

	/**
	 * Section with qualified object.
	 */
	public static class MockQualifiedObjectSection {
		public void doInput(@MockQualification Connection qualified, Connection unqualified) {
		}
	}

	/**
	 * Ensure can provide same {@link Qualifier} on {@link SectionObject}
	 * instances of different types.
	 */
	public void testSameQualifierOnDifferentObjectTypes() {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockSameQualifierObjectSection.class,
				(designer, namespace) -> {
					SectionFunction function = this.addClassSectionFunction(designer, namespace, "doInput", "doInput");

					// First qualified object
					FunctionObject firstFunctionObject = function
							.getFunctionObject(MockQualification.class.getName() + "-" + Connection.class.getName());
					SectionObject firstSectionObject = designer.addSectionObject(
							MockQualification.class.getName() + "-" + Connection.class.getName(),
							Connection.class.getName());
					firstSectionObject.setTypeQualifier(MockQualification.class.getName());
					designer.link(firstFunctionObject, firstSectionObject);

					// Second qualified object
					FunctionObject secondFunctionObject = function
							.getFunctionObject(MockQualification.class.getName() + "-" + String.class.getName());
					SectionObject secondSectionObject = designer.addSectionObject(
							MockQualification.class.getName() + "-" + String.class.getName(), String.class.getName());
					secondSectionObject.setTypeQualifier(MockQualification.class.getName());
					designer.link(secondFunctionObject, secondSectionObject);
				});
		expected.addSectionInput("doInput", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockSameQualifierObjectSection.class.getName());
	}

	/**
	 * Section with same {@link Qualifier} on objects of different types.
	 */
	public static class MockSameQualifierObjectSection {
		public void doInput(@MockQualification Connection connection, @MockQualification String string) {
		}
	}

	/**
	 * Ensure issue if qualified {@link SectionObject} with more than one
	 * {@link Qualifier}.
	 */
	public void testMultipleQualifiedObject() {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Enable recording issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		CompilerIssue[] cause = issues.recordCaptureIssues(true);
		issues.recordIssue("Type", SectionNodeImpl.class,
				"Failed to source FunctionNamespaceType definition from ManagedFunctionSource "
						+ SectionClassManagedFunctionSource.class.getName(),
				new IllegalArgumentException("Method doInput parameter 0 has more than one Qualifier"));
		issues.recordIssue("Type", SectionNodeImpl.class, "Failure loading FunctionNamespaceType from source "
				+ SectionClassManagedFunctionSource.class.getName(), cause);

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockMultipleQualifiedObjectSection.class,
				(designer, namespace) -> {
					SectionFunction function = this.addClassSectionFunction(designer, namespace, "doInput", "doInput");
					function.getFunctionObject("Connection");
					designer.addSectionObject(MockQualification.class.getName(), Connection.class.getName());
				});
		expected.addSectionInput("doInput", null);

		// Test
		this.replayMockObjects();

		// Validate section
		SectionType type = compiler.getSectionLoader().loadSectionType(ClassSectionSource.class,
				MockMultipleQualifiedObjectSection.class.getName(), compiler.createPropertyList());
		assertNull("Should not load type as multiple qualifiers", type);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Mock another qualification.
	 */
	@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface MockAnotherQualification {
	}

	/**
	 * Section with qualified object.
	 */
	public static class MockMultipleQualifiedObjectSection {
		public void doInput(@MockAnotherQualification @MockQualification Connection connection) {
		}
	}

	/**
	 * Ensure can provide {@link SectionObject} via {@link Dependency}.
	 */
	public void testDependency() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockDependencySection.class,
				this.configureClassSectionFunction("doInput"));
		expected.addSectionInput("doInput", null);
		expected.addSectionObject(Connection.class.getName(), Connection.class.getName());

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class, MockDependencySection.class.getName());
	}

	/**
	 * Section with {@link Dependency}.
	 */
	public static class MockDependencySection {
		@Dependency
		Connection connection;

		public void doInput() {
		}
	}

	/**
	 * Ensure provide {@link SectionObject} via qualified {@link Dependency}.
	 */
	public void testQualifiedDependency() {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockQualifiedDependencySection.class,
				this.configureClassSectionFunction("doInput"));
		expected.addSectionInput("doInput", null);
		SectionObject object = expected.addSectionObject(
				MockQualification.class.getName() + "-" + Connection.class.getName(), Connection.class.getName());
		object.setTypeQualifier(MockQualification.class.getName());

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockQualifiedDependencySection.class.getName());
	}

	/**
	 * Section with qualified {@link Dependency}.
	 */
	public static class MockQualifiedDependencySection {
		@MockQualification
		@Dependency
		Connection connection;

		public void doInput() {
		}
	}

	/**
	 * Ensure issue if provide {@link SectionObject} via multiple qualifiers for
	 * {@link Dependency}.
	 */
	public void testMulipleQualifiedDependency() {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Enable loading with compiler issues
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue("Type", SectionNodeImpl.class, "Unable to obtain type qualifier for dependency connection",
				new IllegalArgumentException("Dependency connection has more than one Qualifier"));

		// Test
		this.replayMockObjects();

		// Validate section
		compiler.getSectionLoader().loadSectionType(ClassSectionSource.class,
				MockMultipleQualifiedDependencySection.class.getName(), compiler.createPropertyList());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Section with multiple qualifiers for {@link Dependency}.
	 */
	public static class MockMultipleQualifiedDependencySection {
		@MockQualification
		@MockAnotherQualification
		@Dependency
		Connection connection;

		public void doInput() {
		}
	}

	/**
	 * Ensure able to handle changing the {@link ManagedFunction} name.
	 */
	public void testChangeFunctionName() {

		// Create the expected type
		SectionDesigner expected = this.createSectionDesigner(MockChangeFunctionNameSection.class,
				this.configureClassSectionFunction("newName", "oldName"));
		expected.addSectionInput("newName", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, MockChangeFunctionNameClassSectionSource.class,
				MockChangeFunctionNameSection.class.getName());
	}

	/**
	 * Section with only function.
	 */
	public static class MockChangeFunctionNameSection {
		public void oldName() {
		}
	}

	/**
	 * {@link ClassSectionSource} to change {@link ManagedFunction} name.
	 */
	public static class MockChangeFunctionNameClassSectionSource extends ClassSectionSource {
		@Override
		protected String getFunctionName(ManagedFunctionType<?, ?> functionType) {
			String functionTypeName = functionType.getFunctionName();
			return ("oldName".equals(functionTypeName) ? "newName" : functionTypeName);
		}
	}

	/**
	 * Ensure able to handle changing the {@link ManagedFunction} name along
	 * with keeping links working.
	 */
	public void testChangeFunctionNameAndEnsureCorrectLinkedType() {

		// Create the expected type
		SectionDesigner expected = this.createSectionDesigner(MockChangeFunctionNameWithLinksSection.class,
				(designer, namespace) -> {
					SectionFunction doInput = this.addClassSectionFunction(designer, namespace, "doInput", "doInput");
					FunctionObject doInputReturnValue = doInput.getFunctionObject(ReturnValue.class.getName());
					SectionObject returnSectionObject = designer.addSectionObject(ReturnValue.class.getName(),
							ReturnValue.class.getName());
					designer.link(doInputReturnValue, returnSectionObject);
					doInput.getFunctionObject(Boolean.class.getName()).flagAsParameter();

					SectionFunction newName = this.addClassSectionFunction(designer, namespace, "newName", "oldName");
					FunctionObject newNameReturnValue = newName.getFunctionObject(ReturnValue.class.getName());
					designer.link(newNameReturnValue, returnSectionObject);
					newName.getFunctionObject(String.class.getName()).flagAsParameter();
					FunctionObject newNameConnection = newName.getFunctionObject(Connection.class.getName());
					SectionObject connectionSectionObject = designer.addSectionObject(Connection.class.getName(),
							Connection.class.getName());
					designer.link(newNameConnection, connectionSectionObject);

					SectionFunction finished = this.addClassSectionFunction(designer, namespace, "finished",
							"finished");
					FunctionObject finishedReturnValue = finished.getFunctionObject(ReturnValue.class.getName());
					designer.link(finishedReturnValue, returnSectionObject);
				});

		// Inputs
		expected.addSectionInput("doInput", Boolean.class.getName());
		expected.addSectionInput("newName", String.class.getName());
		expected.addSectionInput("finished", null);

		// Outputs
		expected.addSectionOutput("externalFlow", null, false);
		expected.addSectionOutput("java.sql.SQLException", SQLException.class.getName(), true);

		// Validate section
		SectionLoaderUtil.validateSection(expected, MockChangeFunctionNameClassSectionSource.class,
				MockChangeFunctionNameWithLinksSection.class.getName());
	}

	/**
	 * Ensure able to handle changing the {@link ManagedFunction} name and
	 * continue to execute.
	 */
	public void testChangeFunctionNameAndEnsureCorrectLinkedExecution() throws Exception {

		final Connection connection = this.createMock(Connection.class);
		final ReturnValue returnValue = new ReturnValue();

		// Managed object internal, so must run to test
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		AutoWireSection section = source.addSection("test", MockChangeFunctionNameClassSectionSource.class.getName(),
				MockChangeFunctionNameWithLinksSection.class.getName());
		source.addObject(returnValue, new AutoWire(ReturnValue.class));
		source.addObject(connection, new AutoWire(Connection.class));
		source.link(section, "externalFlow", section, "finished");

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run invoking flow
			officeFloor.invokeFunction("test.NAMESPACE.doInput", new Boolean(true));
			assertEquals("Incorrect value on invoking flow", "doInput -> oldName(Flow) -> finished", returnValue.value);

			// Run using next function
			officeFloor.invokeFunction("test.NAMESPACE.doInput", null);
			assertEquals("Incorrect value on next function", "doInput -> oldName(null) -> finished", returnValue.value);

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock {@link FlowInterface} for linking to old method name even after
	 * {@link ManagedFunction} name change.
	 */
	@FlowInterface
	public static interface MockChangeNameFlows {
		void oldName(String parameter);
	}

	/**
	 * Section with only function.
	 */
	public static class MockChangeFunctionNameWithLinksSection {

		// even with name change, should still link by method name
		@NextFunction("oldName")
		public void doInput(MockChangeNameFlows flow, ReturnValue returnValue, @Parameter Boolean isInvokeFlow) {

			// Flag invoked
			returnValue.value = "doInput";

			// Determine if invoke flow
			if (isInvokeFlow == null ? false : isInvokeFlow.booleanValue()) {
				// Invoke the flow
				flow.oldName("Flow");
			}
		}

		@NextFunction("externalFlow")
		public void oldName(ReturnValue returnValue, @Parameter String parameter, Connection connection)
				throws SQLException {
			// Indicate invoked
			returnValue.value += " -> oldName(" + parameter + ")";
		}

		public void finished(ReturnValue returnValue) {
			returnValue.value += " -> finished";
		}
	}

	/**
	 * Ensure can configure a {@link ManagedObject}.
	 */
	public void testManagedObject() throws Exception {

		// Managed object internal, so must run to test
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		source.addSection("test", ClassSectionSource.class.getName(), MockManagedObjectSection.class.getName());

		// Open the OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run to ensure obtained message
			ReturnValue returnValue = new ReturnValue();
			officeFloor.invokeFunction("test.NAMESPACE.doInput", returnValue);
			assertEquals("Incorrect value from managed object", "test", returnValue.value);

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Allows returning a value from the {@link OfficeFloor}.
	 */
	public static class ReturnValue {
		public String value = null;
	}

	/**
	 * Mock {@link ManagedObject}.
	 */
	public static class MockManagedObject {
		public String getMessage() {
			return "test";
		}
	}

	/**
	 * Section with {@link ManagedObject}.
	 */
	public static class MockManagedObjectSection {

		@ManagedObject(source = ClassManagedObjectSource.class, properties = {
				@Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockManagedObject.class) })
		private MockManagedObject managedObject;

		public void doInput(@Parameter ReturnValue returnValue) {
			returnValue.value = this.managedObject.getMessage();
		}
	}

	/**
	 * Ensure can configure a {@link ManagedObject} with a dependency.
	 */
	public void testManagedObjectWithDependency() throws Exception {

		// Managed object internal, so must run to test
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		source.addSection("test", ClassSectionSource.class.getName(),
				MockManagedObjectWithDependencySection.class.getName());

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run to ensure obtained message
			ReturnValue returnValue = new ReturnValue();
			officeFloor.invokeFunction("test.NAMESPACE.doInput", returnValue);
			assertEquals("Incorrect value from managed object", "test", returnValue.value);

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock {@link ManagedObject} with dependency.
	 */
	public static class MockManagedObjectWithDependency {

		@Dependency
		private MockManagedObject dependency;

		public String getMessage() {
			return this.dependency.getMessage();
		}
	}

	/**
	 * Section with {@link ManagedObject} with dependency.
	 */
	public static class MockManagedObjectWithDependencySection {

		@ManagedObject(source = ClassManagedObjectSource.class, properties = {
				@Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockManagedObjectWithDependency.class) })
		private MockManagedObjectWithDependency managedObjectWithDependency;

		@ManagedObject(source = ClassManagedObjectSource.class, properties = {
				@Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockManagedObject.class) })
		MockManagedObject managedObject;

		public void doInput(@Parameter ReturnValue returnValue) {
			returnValue.value = this.managedObjectWithDependency.getMessage();
		}
	}

	/**
	 * Ensure can qualify the {@link ManagedObject}.
	 */
	public void testQualifiedManagedObject() {

		// Create the expected section type
		SectionDesigner type = this.createSectionDesigner(MockQualifiedManagedObjectSection.class,
				this.configureClassSectionFunction("function"));
		type.addSectionInput("function", null);
		SectionManagedObjectSource mos = type.addSectionManagedObjectSource("managedObject",
				ClassManagedObjectSource.class.getName());
		mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, MockQualifiedManagedObject.class.getName());
		SectionManagedObject mo = mos.addSectionManagedObject("managedObject", ManagedObjectScope.PROCESS);
		mo.addTypeQualification(MockQualifier.class.getName(), String.class.getName());
		mo.addTypeQualification(null, Integer.class.getName());

		// Validate the section type
		SectionLoaderUtil.validateSection(type, ClassSectionSource.class,
				MockQualifiedManagedObjectSection.class.getName());
	}

	/**
	 * Mock qualifier.
	 */
	public @interface MockQualifier {
	}

	/**
	 * Mock qualified {@link ManagedObject}.
	 */
	public static class MockQualifiedManagedObject {
	}

	/**
	 * Section with qualified {@link ManagedObject}.
	 */
	public static class MockQualifiedManagedObjectSection {

		@ManagedObject(source = ClassManagedObjectSource.class, qualifiers = {
				@TypeQualifier(qualifier = MockQualifier.class, type = String.class),
				@TypeQualifier(type = Integer.class) }, properties = {
						@Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockQualifiedManagedObject.class) })
		MockQualifiedManagedObject managedObject;

		public void function() {
		}
	}

	/**
	 * Ensure can internally invoke flows within the section.
	 */
	public void testInternalFlow() throws Exception {

		// Triggering flows, so must run to test
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		source.addSection("test", ClassSectionSource.class.getName(), MockInternalFlowSection.class.getName());

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run to ensure obtained message
			ReturnValue returnValue = new ReturnValue();
			officeFloor.invokeFunction("test.NAMESPACE.doFirst", returnValue);
			assertEquals("Incorrect value from flow", "one-two-three", returnValue.value);

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock {@link FlowInterface} for internal flows.
	 */
	@FlowInterface
	public static interface MockInternalFlows {
		void doThird(ReturnValue returnValue);
	}

	/**
	 * Section to undertake internal flows.
	 */
	public static class MockInternalFlowSection {

		@NextFunction("doSecond")
		public ReturnValue doFirst(@Parameter ReturnValue returnValue) {
			returnValue.value = "one";
			return returnValue;
		}

		public void doSecond(@Parameter ReturnValue returnValue, MockInternalFlows flows) {
			returnValue.value = returnValue.value + "-two";
			flows.doThird(returnValue);
		}

		public void doThird(@Parameter ReturnValue returnValue) {
			returnValue.value = returnValue.value + "-three";
		}
	}

	/**
	 * Ensure able to handle an escalation internally.
	 */
	public void testEscalationHandling() throws Exception {

		// Triggering flows, so must run to test
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		ReturnValue returnValue = new ReturnValue();
		source.addObject(returnValue, new AutoWire(ReturnValue.class));
		source.addSection("test", ClassSectionSource.class.getName(), MockEscalationHandlingSection.class.getName());

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run to ensure obtained message
			officeFloor.invokeFunction("test.NAMESPACE.triggerEscalation", null);
			assertEquals("Incorrect value from handling escalation", "test", returnValue.value);

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Section to undertake handling of escalation.
	 */
	public static class MockEscalationHandlingSection {

		public void triggerEscalation() throws IOException {
			throw new IOException("test");
		}

		public void handleEscalation(@Parameter IOException escalation, ReturnValue value) {
			value.value = escalation.getMessage();
		}
	}

	/**
	 * Ensure that an escalation method can not handle its own
	 * {@link Escalation}.
	 */
	public void testAvoidCyclicEscalationHandling() throws Exception {

		final IOException[] escalated = new IOException[1];

		// Configure to handle escalation
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		source.addSection("test", ClassSectionSource.class.getName(),
				MockAvoidCyclicEscalationHandling.class.getName());
		source.getOfficeFloorCompiler().setEscalationHandler(new EscalationHandler() {
			@Override
			public void handleEscalation(Throwable escalation) throws Throwable {
				escalated[0] = (IOException) escalation;
			}
		});

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run triggering escalation
			final IOException escalation = new IOException("TEST");
			officeFloor.invokeFunction("test.NAMESPACE.handleEscalation", escalation);

			// Ensure not handling itself (escalated to OfficeFloor level)
			assertEquals("Incorrect escalation", escalation, escalated[0]);

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}

	}

	/**
	 * Section to ensure not handling own escalation causing cycle.
	 */
	public static class MockAvoidCyclicEscalationHandling {

		public void handleEscalation(@Parameter IOException escalation) throws IOException {
			throw escalation;
		}
	}

	/**
	 * Ensure can configure a {@link SubSection}.
	 */
	public void testSubSection() throws Exception {

		// Triggering sub section, so must run to test
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		source.addSection("test", ClassSectionSource.class.getName(), MockInvokeSubSection.class.getName());
		ReturnValue returnValue = new ReturnValue();
		source.addObject(returnValue, new AutoWire(ReturnValue.class));

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run to ensure obtained message
			officeFloor.invokeFunction("test.NAMESPACE.doFirst", null);
			assertEquals("Incorrect value from sub section", "sub section", returnValue.value);

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock {@link SectionInterface} for invoking a {@link SubSection}.
	 */
	@SectionInterface(source = ClassSectionSource.class, locationClass = MockSubSection.class, outputs = {
			@FlowLink(name = "output", method = "doLast") })
	public static interface MockSectionInterface {
		void doSubSectionInput();
	}

	/**
	 * Section to invoke sub section.
	 */
	public static class MockSubSection {
		@NextFunction("output")
		public void doSubSectionInput(ReturnValue returnValue) {
			returnValue.value = "sub";
		}
	}

	/**
	 * Mock section containing the {@link SubSection}.
	 */
	public static class MockInvokeSubSection {
		public void doFirst(MockSectionInterface subSection) {
			subSection.doSubSectionInput();
		}

		public void doLast(ReturnValue returnValue) {
			returnValue.value = returnValue.value + " section";
		}
	}

	/**
	 * {@link SectionFunctionNamespace} configurer.
	 */
	private static interface NamespaceConfigurer {

		/**
		 * Configures the {@link SectionFunctionNamespace}.
		 * 
		 * @param designer
		 *            {@link SectionDesigner}.
		 * @param namespace
		 *            {@link SectionFunctionNamespace} to configure.
		 */
		void configureNamespace(SectionDesigner designer, SectionFunctionNamespace namespace);
	}

	/**
	 * Creates the expected {@link SectionDesigner} with pre-populated details.
	 * 
	 * @param sectionClass
	 *            Section class.
	 * @param namespaceConfigurer
	 *            {@link NamespaceConfigurer}.
	 * @return {@link SectionDesigner}.
	 */
	private SectionDesigner createSectionDesigner(Class<?> sectionClass, NamespaceConfigurer namespaceConfigurer) {

		// Create the section designer
		SectionDesigner designer = SectionLoaderUtil.createSectionDesigner();
		SectionManagedObjectSource managedObjectSource = designer.addSectionManagedObjectSource("OBJECT",
				ClassManagedObjectSource.class.getName());
		managedObjectSource.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, sectionClass.getName());
		this.objectManagedObject = managedObjectSource.addSectionManagedObject("OBJECT", ManagedObjectScope.THREAD);
		SectionFunctionNamespace namespace = designer.addSectionFunctionNamespace("NAMESPACE",
				SectionClassManagedFunctionSource.class.getName());
		namespace.addProperty(ClassManagedFunctionSource.CLASS_NAME_PROPERTY_NAME, sectionClass.getName());
		namespaceConfigurer.configureNamespace(designer, namespace);

		// Return the section designer
		return designer;
	}

	/**
	 * Convenience method to add {@link ClassSectionSource}
	 * {@link SectionFunction}.
	 * 
	 * @param functionName
	 *            {@link SectionFunction} and {@link ManagedFunctionType} name.
	 * @return {@link NamespaceConfigurer}.
	 */
	public NamespaceConfigurer configureClassSectionFunction(String functionName) {
		return this.configureClassSectionFunction(functionName, functionName);
	}

	/**
	 * Convenience method to add {@link ClassSectionSource}
	 * {@link SectionFunction}.
	 * 
	 * @param functionName
	 *            {@link SectionFunction} name.
	 * @param functionTypeName
	 *            {@link ManagedFunctionType} name.
	 * @return {@link NamespaceConfigurer}.
	 */
	public NamespaceConfigurer configureClassSectionFunction(String functionName, String functionTypeName) {
		return (designer, namespace) -> this.addClassSectionFunction(designer, namespace, functionName,
				functionTypeName);
	}

	/**
	 * Convenience method to add a {@link ClassSectionSource}
	 * {@link SectionFunction}.
	 * 
	 * @param designer
	 *            {@link SectionDesigner}.
	 * @param namespace
	 *            {@link SectionFunctionNamespace}.
	 * @param functionName
	 *            {@link SectionFunction} name.
	 * @param functionTypeName
	 *            {@link ManagedFunctionType} name.
	 * @return {@link SectionFunction}.
	 */
	public SectionFunction addClassSectionFunction(SectionDesigner designer, SectionFunctionNamespace namespace,
			String functionName, String functionTypeName) {
		SectionFunction function = namespace.addSectionFunction(functionName, functionTypeName);
		FunctionObject functionObject = function.getFunctionObject(ClassSectionSource.CLASS_OBJECT_NAME);
		designer.link(functionObject, objectManagedObject);
		return function;
	}

}