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
import java.util.HashMap;
import java.util.Map;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireOfficeFloor;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.autowire.impl.AutoWireOfficeFloorSource;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.spi.section.SectionWork;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.work.clazz.ClassWorkSource;
import net.officefloor.plugin.work.clazz.FlowInterface;
import net.officefloor.plugin.work.clazz.NonTaskMethod;
import net.officefloor.plugin.work.clazz.Qualifier;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassSectionSourceTest extends OfficeFrameTestCase {

	/**
	 * <p>
	 * Expected {@link SectionTask} instances by name.
	 * <p>
	 * This is loaded via creation of the {@link SectionDesigner}.
	 */
	private final Map<String, SectionTask> expectedTasks = new HashMap<String, SectionTask>();

	/**
	 * {@link ClassSectionSource} used for testing. Allows for overriding with
	 * child implementation.
	 */
	private Class<? extends ClassSectionSource> classSectionSource = ClassSectionSource.class;

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
		SectionDesigner expected = this.createSectionDesigner(
				MockInputSection.class, "doInput");
		expected.addSectionInput("doInput", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockInputSection.class.getName());
	}

	/**
	 * Section with only an input.
	 */
	public static class MockInputSection {
		public void doInput() {
		}
	}

	/**
	 * Ensure ignore methods annotated with {@link NonTaskMethod}.
	 */
	public void testIgnoreNonTaskMethods() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(
				MockIgnoreInputSection.class, "includedInput");
		expected.addSectionInput("includedInput", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockIgnoreInputSection.class.getName());
	}

	/**
	 * Section with methods to not be {@link Task} instances.
	 */
	public static class MockIgnoreInputSection {
		public void includedInput() {
		}

		@NonTaskMethod
		public void nonIncludedInput() {
		}

		@NonTaskMethod
		public void nonIncludedStaticInput() {
		}
	}

	/**
	 * Ensure and provide {@link SectionOutput}.
	 */
	public void testOutput() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(
				MockOutputSection.class, "doInput");
		expected.addSectionInput("doInput", null);
		expected.addSectionOutput("doOutput", null, false);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockOutputSection.class.getName());
	}

	/**
	 * Section with an output.
	 */
	public static class MockOutputSection {
		@NextTask("doOutput")
		public void doInput() {
		}
	}

	/**
	 * Ensure can provide {@link SectionOutput} via {@link FlowInterface}.
	 */
	public void testFlowInterface() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(
				MockFlowInterfaceSection.class, "doInput");
		expected.addSectionInput("doInput", null);
		expected.addSectionOutput("doOutput", null, false);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockFlowInterfaceSection.class.getName());
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
		SectionDesigner expected = this.createSectionDesigner(
				MockEscalationSection.class, "doInput");
		expected.addSectionInput("doInput", null);
		expected.addSectionOutput(SQLException.class.getName(),
				SQLException.class.getName(), true);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockEscalationSection.class.getName());
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
		SectionDesigner expected = this.createSectionDesigner(
				MockParameterArgumentSection.class, "doInput");
		this.expectedTasks.get("doInput").getTaskObject(String.class.getName());
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
		@NextTask("doOutput")
		public Integer doInput(@Parameter String parameter) {
			return null;
		}
	}

	/**
	 * Ensure can provide {@link SectionObject}.
	 */
	public void testObject() {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(
				MockObjectSection.class, "doInput");
		this.expectedTasks.get("doInput").getTaskObject(
				Connection.class.getName());
		expected.addSectionInput("doInput", null);
		expected.addSectionObject(Connection.class.getName(),
				Connection.class.getName());

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockObjectSection.class.getName());
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

		final String QUALIFIED_NAME = MockQualification.class.getName() + "-"
				+ Connection.class.getName();
		final String UNQUALIFIED_NAME = Connection.class.getName();

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(
				MockObjectSection.class, "doInput");
		SectionTask task = this.expectedTasks.get("doInput");
		task.getTaskObject(QUALIFIED_NAME);
		task.getTaskObject(UNQUALIFIED_NAME);
		expected.addSectionInput("doInput", null);
		expected.addSectionObject(QUALIFIED_NAME, Connection.class.getName())
				.setTypeQualifier(MockQualification.class.getName());
		expected.addSectionObject(UNQUALIFIED_NAME, Connection.class.getName());

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
		public void doInput(@MockQualification Connection qualified,
				Connection unqualified) {
		}
	}

	/**
	 * Ensure can provide same {@link Qualifier} on {@link SectionObject}
	 * instances of different types.
	 */
	public void testSameQualifierOnDifferentObjectTypes() {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(
				MockObjectSection.class, "doInput");
		SectionTask task = this.expectedTasks.get("doInput");
		task.getTaskObject(MockQualification.class.getName() + "-"
				+ Connection.class.getName());
		task.getTaskObject(MockQualification.class.getName() + "-"
				+ String.class.getName());
		expected.addSectionInput("doInput", null);
		expected.addSectionObject(
				MockQualification.class.getName() + "-"
						+ Connection.class.getName(),
				Connection.class.getName()).setTypeQualifier(
				MockQualification.class.getName());
		expected.addSectionObject(
				MockQualification.class.getName() + "-"
						+ String.class.getName(), String.class.getName())
				.setTypeQualifier(MockQualification.class.getName());

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockSameQualifierObjectSection.class.getName());
	}

	/**
	 * Section with same {@link Qualifier} on objects of different types.
	 */
	public static class MockSameQualifierObjectSection {
		public void doInput(@MockQualification Connection connection,
				@MockQualification String string) {
		}
	}

	/**
	 * Ensure issue if qualified {@link SectionObject} with more than one
	 * {@link Qualifier}.
	 */
	public void testMultipleQualifiedObject() {

		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Enable recording issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.addIssue(
				LocationType.SECTION,
				MockMultipleQualifiedObjectSection.class.getName(),
				AssetType.WORK,
				"loadWorkType",
				"Failed to source WorkType definition from WorkSource "
						+ SectionClassWorkSource.class.getName(),
				new IllegalArgumentException(
						"Method doInput parameter 0 has more than one Qualifier"));
		this.control(issues).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {

				// Match initial parameters
				for (int i = 0; i < 5; i++) {
					Object e = expected[i];
					Object a = actual[i];
					if ((e == null) && (a == null)) {
						continue; // match on null
					} else if ((e != null) && (e.equals(actual[i]))) {
						continue; // match not null
					} else {
						return false; // not match
					}
				}

				// Match exception
				IllegalArgumentException eEx = (IllegalArgumentException) expected[5];
				IllegalArgumentException aEx = (IllegalArgumentException) actual[5];
				if (!eEx.getMessage().equals(aEx.getMessage())) {
					return false; // not match
				}

				// As here, matches
				return true;
			}
		});
		issues.addIssue(LocationType.SECTION,
				MockMultipleQualifiedObjectSection.class.getName(), null, null,
				"Failure loading WorkType from source "
						+ SectionClassWorkSource.class.getName());

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(
				MockObjectSection.class, "doInput");
		this.expectedTasks.get("doInput").getTaskObject("Connection");
		expected.addSectionInput("doInput", null);
		expected.addSectionObject(MockQualification.class.getName(),
				Connection.class.getName());

		// Test
		this.replayMockObjects();

		// Validate section
		SectionType type = compiler.getSectionLoader().loadSectionType(
				ClassSectionSource.class,
				MockMultipleQualifiedObjectSection.class.getName(),
				compiler.createPropertyList());
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
		public void doInput(
				@MockAnotherQualification @MockQualification Connection connection) {
		}
	}

	/**
	 * Ensure can provide {@link SectionObject} via {@link Dependency}.
	 */
	public void testDependency() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(
				MockDependencySection.class, "doInput");
		expected.addSectionInput("doInput", null);
		expected.addSectionObject(Connection.class.getName(),
				Connection.class.getName());

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockDependencySection.class.getName());
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
		SectionDesigner expected = this.createSectionDesigner(
				MockDependencySection.class, "doInput");
		expected.addSectionInput("doInput", null);
		SectionObject object = expected.addSectionObject(
				MockQualification.class.getName() + "-"
						+ Connection.class.getName(),
				Connection.class.getName());
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

		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Enable loading with compiler issues
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.addIssue(LocationType.SECTION,
				MockMultipleQualifiedDependencySection.class.getName(), null,
				null, "Dependency connection has more than one Qualifier");

		// Test
		this.replayMockObjects();

		// Validate section
		compiler.getSectionLoader().loadSectionType(ClassSectionSource.class,
				MockMultipleQualifiedDependencySection.class.getName(),
				compiler.createPropertyList());

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
	 * Ensure able to handle changing the {@link Task} name.
	 */
	public void testChangeTaskName() {

		// Override to change class name
		this.classSectionSource = MockChangeTaskNameClassSectionSource.class;

		// Create the expected type
		SectionDesigner expected = this.createSectionDesigner(
				MockInputSection.class, "newName");
		expected.addSectionInput("newName", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected,
				MockChangeTaskNameClassSectionSource.class,
				MockChangeTaskNameSection.class.getName());
	}

	/**
	 * Section with only task.
	 */
	public static class MockChangeTaskNameSection {
		public void oldName() {
		}
	}

	/**
	 * {@link ClassSectionSource} to change {@link Task} name.
	 */
	public static class MockChangeTaskNameClassSectionSource extends
			ClassSectionSource {
		@Override
		protected String getTaskName(TaskType<?, ?, ?> taskType) {
			String taskTypeName = taskType.getTaskName();
			return ("oldName".equals(taskTypeName) ? "newName" : taskTypeName);
		}
	}

	/**
	 * Ensure able to handle changing the {@link Task} name along with keeping
	 * links working.
	 */
	public void testChangeTaskNameAndEnsureCorrectLinkedType() {

		// Override to change class name
		this.classSectionSource = MockChangeTaskNameClassSectionSource.class;

		// Create the expected type
		SectionDesigner expected = this.createSectionDesigner(
				MockInputSection.class, "doInput", "newName", "finished");
		SectionTask doInput = this.expectedTasks.get("doInput");
		doInput.getTaskObject(ReturnValue.class.getName());
		doInput.getTaskObject(Boolean.class.getName());
		SectionTask newName = this.expectedTasks.get("newName");
		newName.getTaskObject(ReturnValue.class.getName());
		newName.getTaskObject(String.class.getName());
		newName.getTaskObject(Connection.class.getName());
		SectionTask finished = this.expectedTasks.get("finished");
		finished.getTaskObject(ReturnValue.class.getName());

		// Inputs
		expected.addSectionInput("doInput", Boolean.class.getName());
		expected.addSectionInput("newName", String.class.getName());
		expected.addSectionInput("finished", null);

		// Outputs
		expected.addSectionOutput("externalFlow", null, false);
		expected.addSectionOutput("java.sql.SQLException",
				SQLException.class.getName(), true);

		// Objects
		expected.addSectionObject(ReturnValue.class.getName(),
				ReturnValue.class.getName());
		expected.addSectionObject(Connection.class.getName(),
				Connection.class.getName());

		// Validate section
		SectionLoaderUtil.validateSection(expected,
				MockChangeTaskNameClassSectionSource.class,
				MockChangeTaskNameWithLinksSection.class.getName());
	}

	/**
	 * Ensure able to handle changing the {@link Task} name and continue to
	 * execute.
	 */
	public void testChangeTaskNameAndEnsureCorrectLinkedExecution()
			throws Exception {

		final Connection connection = this.createMock(Connection.class);
		final ReturnValue returnValue = new ReturnValue();

		// Managed object internal, so must run to test
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		AutoWireSection section = source.addSection("test",
				MockChangeTaskNameClassSectionSource.class.getName(),
				MockChangeTaskNameWithLinksSection.class.getName());
		source.addObject(returnValue, new AutoWire(ReturnValue.class));
		source.addObject(connection, new AutoWire(Connection.class));
		source.link(section, "externalFlow", section, "finished");

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run invoking flow
			officeFloor.invokeTask("test.WORK", "doInput", new Boolean(true));
			assertEquals("Incorrect value on invoking flow",
					"doInput -> oldName(Flow) -> finished", returnValue.value);

			// Run using next task
			officeFloor.invokeTask("test.WORK", "doInput", null);
			assertEquals("Incorrect value on next task",
					"doInput -> oldName(null) -> finished", returnValue.value);

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock {@link FlowInterface} for linking to old method name even after
	 * {@link Task} name change.
	 */
	@FlowInterface
	public static interface MockChangeNameFlows {
		void oldName(String parameter);
	}

	/**
	 * Section with only task.
	 */
	public static class MockChangeTaskNameWithLinksSection {

		// even with name change, should still link by method name
		@NextTask("oldName")
		public void doInput(MockChangeNameFlows flow, ReturnValue returnValue,
				@Parameter Boolean isInvokeFlow) {

			// Flag invoked
			returnValue.value = "doInput";

			// Determine if invoke flow
			if (isInvokeFlow == null ? false : isInvokeFlow.booleanValue()) {
				// Invoke the flow
				flow.oldName("Flow");
			}
		}

		@NextTask("externalFlow")
		public void oldName(ReturnValue returnValue,
				@Parameter String parameter, Connection connection)
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
		source.addSection("test", ClassSectionSource.class.getName(),
				MockManagedObjectSection.class.getName());

		// Open the OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run to ensure obtained message
			ReturnValue returnValue = new ReturnValue();
			officeFloor.invokeTask("test.WORK", "doInput", returnValue);
			assertEquals("Incorrect value from managed object", "test",
					returnValue.value);

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

		@ManagedObject(source = ClassManagedObjectSource.class, properties = { @Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockManagedObject.class) })
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
			officeFloor.invokeTask("test.WORK", "doInput", returnValue);
			assertEquals("Incorrect value from managed object", "test",
					returnValue.value);

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

		@ManagedObject(source = ClassManagedObjectSource.class, properties = { @Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockManagedObjectWithDependency.class) })
		private MockManagedObjectWithDependency managedObjectWithDependency;

		@ManagedObject(source = ClassManagedObjectSource.class, properties = { @Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockManagedObject.class) })
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
		SectionDesigner type = this.createSectionDesigner(
				MockQualifiedManagedObjectSection.class, "task");
		type.addSectionInput("task", null);
		SectionManagedObjectSource mos = type.addSectionManagedObjectSource(
				"managedObject", ClassManagedObjectSource.class.getName());
		mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				MockQualifiedManagedObject.class.getName());
		SectionManagedObject mo = mos.addSectionManagedObject("managedObject",
				ManagedObjectScope.PROCESS);
		mo.addTypeQualification(MockQualifier.class.getName(),
				String.class.getName());
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
				@TypeQualifier(type = Integer.class) }, properties = { @Property(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockQualifiedManagedObject.class) })
		MockQualifiedManagedObject managedObject;

		public void task() {
		}
	}

	/**
	 * Ensure can internally invoke flows within the section.
	 */
	public void testInternalFlow() throws Exception {

		// Triggering flows, so must run to test
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		source.addSection("test", ClassSectionSource.class.getName(),
				MockInternalFlowSection.class.getName());

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run to ensure obtained message
			ReturnValue returnValue = new ReturnValue();
			officeFloor.invokeTask("test.WORK", "doFirst", returnValue);
			assertEquals("Incorrect value from flow", "one-two-three",
					returnValue.value);

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

		@NextTask("doSecond")
		public ReturnValue doFirst(@Parameter ReturnValue returnValue) {
			returnValue.value = "one";
			return returnValue;
		}

		public void doSecond(@Parameter ReturnValue returnValue,
				MockInternalFlows flows) {
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
		source.addSection("test", ClassSectionSource.class.getName(),
				MockEscalationHandlingSection.class.getName());

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run to ensure obtained message
			officeFloor.invokeTask("test.WORK", "triggerEscalation", null);
			assertEquals("Incorrect value from handling escalation", "test",
					returnValue.value);

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

		public void handleEscalation(@Parameter IOException escalation,
				ReturnValue value) {
			value.value = escalation.getMessage();
		}
	}

	/**
	 * Ensure can configure a {@link SubSection}.
	 */
	public void testSubSection() throws Exception {

		// Triggering sub section, so must run to test
		AutoWireOfficeFloorSource source = new AutoWireOfficeFloorSource();
		source.addSection("test", ClassSectionSource.class.getName(),
				MockInvokeSubSection.class.getName());
		ReturnValue returnValue = new ReturnValue();
		source.addObject(returnValue, new AutoWire(ReturnValue.class));

		// Open OfficeFloor
		AutoWireOfficeFloor officeFloor = source.openOfficeFloor();
		try {

			// Run to ensure obtained message
			officeFloor.invokeTask("test.WORK", "doFirst", null);
			assertEquals("Incorrect value from sub section", "sub section",
					returnValue.value);

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock {@link SectionInterface} for invoking a {@link SubSection}.
	 */
	@SectionInterface(source = ClassSectionSource.class, locationClass = MockSubSection.class, outputs = { @FlowLink(name = "output", method = "doLast") })
	public static interface MockSectionInterface {
		void doSubSectionInput();
	}

	/**
	 * Section to invoke sub section.
	 */
	public static class MockSubSection {
		@NextTask("output")
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
	 * Creates the expected {@link SectionDesigner} with pre-populated details.
	 * 
	 * @param sectionClass
	 *            Section class.
	 * @param taskNames
	 *            Names of the {@link Task} instances.
	 * @return {@link SectionDesigner}.
	 */
	private SectionDesigner createSectionDesigner(Class<?> sectionClass,
			String... taskNames) {

		// Create the section designer
		SectionDesigner designer = SectionLoaderUtil
				.createSectionDesigner(this.classSectionSource);
		SectionManagedObjectSource managedObjectSource = designer
				.addSectionManagedObjectSource("OBJECT",
						ClassManagedObjectSource.class.getName());
		managedObjectSource.addProperty(
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				sectionClass.getName());
		SectionWork work = designer.addSectionWork("WORK",
				ClassWorkSource.class.getName());
		for (String taskName : taskNames) {

			// Create the section task with the section object
			SectionTask task = work.addSectionTask(taskName, taskName);
			task.getTaskObject("OBJECT");

			// Register the expected task
			this.expectedTasks.put(taskName, task);
		}

		// Return the section designer
		return designer;
	}

}