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

package net.officefloor.plugin.section.clazz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.structure.FunctionNamespaceNodeImpl;
import net.officefloor.compile.impl.structure.ManagedObjectSourceNodeImpl;
import net.officefloor.compile.impl.structure.SectionNodeImpl;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.section.FunctionObject;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.compile.test.officefloor.CompileVar;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.CompleteFlowCallback;
import net.officefloor.frame.test.MockTestSupport;
import net.officefloor.frame.test.TestSupportExtension;
import net.officefloor.frame.test.ThreadedTestSupport;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.InvalidConfigurationError;
import net.officefloor.plugin.clazz.NonFunctionMethod;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObject;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.singleton.Singleton;
import net.officefloor.plugin.section.clazz.ClassSectionSource.SectionClassManagedFunctionSource;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableOfficeExtensionService;

/**
 * Tests the {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(TestSupportExtension.class)
public class ClassSectionSourceTest {

	private final MockTestSupport mocks = new MockTestSupport();

	private final ThreadedTestSupport threading = new ThreadedTestSupport();

	/**
	 * {@link SectionManagedObject} for the {@link ClassManagedObject}.
	 */
	private SectionManagedObject objectManagedObject;

	/**
	 * Ensure correct specification.
	 */
	@Test
	public void specification() {
		// No specification as uses location for class
		SectionLoaderUtil.validateSpecification(ClassSectionSource.class);
	}

	/**
	 * Ensure can provide {@link SectionInput}.
	 */
	@Test
	public void input() {
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
			// Testing type
		}
	}

	/**
	 * Ensure ignore methods annotated with {@link NonFunctionMethod}.
	 */
	@Test
	public void ignoreNonFunctionMethods() {
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
			// Testing type
		}

		@NonFunctionMethod
		public void nonIncludedInput() {
			// Testing type
		}

		@NonFunctionMethod
		public void nonIncludedStaticInput() {
			// Testing type
		}
	}

	/**
	 * Ensure inherit methods by name.
	 */
	@Test
	public void inheritFunctionMethods() {

		// Ensure inheritance
		assertTrue((new MockChildSection()) instanceof MockParentSection, "Invalid test if not extending");

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
			// Testing type
		}
	}

	/**
	 * Ensure provide {@link SectionOutput}.
	 */
	@Test
	public void output() {
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
		@Next("doOutput")
		public void doInput() {
			// Testing type
		}
	}

	/**
	 * Ensure provide single {@link SectionOutput} from multiple methods.
	 */
	@Test
	public void outputsToSameOutput() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockOutputsToSameOutputSection.class,
				(designer, namespace) -> {
					this.addClassSectionFunction(designer, namespace, "one", "one");
					this.addClassSectionFunction(designer, namespace, "two", "two");
				});
		expected.addSectionInput("one", null);
		expected.addSectionInput("two", null);
		expected.addSectionOutput("doOutput", null, false);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockOutputsToSameOutputSection.class.getName());
	}

	/**
	 * Section with an output.
	 */
	public static class MockOutputsToSameOutputSection {
		@Next("doOutput")
		public void one() {
			// Testing type
		}

		@Next("doOutput")
		public void two() {
			// Testing type
		}
	}

	/**
	 * Ensure can provide {@link SectionOutput} via {@link FlowInterface}.
	 */
	@Test
	public void flowInterface() {
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
			// Testing type
		}
	}

	/**
	 * Ensure can provide parameter types for {@link FlowInterface}.
	 */
	@Test
	public void flowInterfaceParameterTypes() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockParameterFlowInterfaceSection.class,
				this.configureClassSectionFunction("doInput"));
		expected.addSectionInput("doInput", null);
		expected.addSectionOutput("doArray", Object[].class.getName(), false);
		expected.addSectionOutput("doBoxedPrimitive", Character.class.getName(), false);
		expected.addSectionOutput("doBoxedPrimitiveArray", Character[].class.getName(), false);
		expected.addSectionOutput("doObject", Object.class.getName(), false);
		expected.addSectionOutput("doPrimitive", char.class.getName(), false);
		expected.addSectionOutput("doPrimitiveArray", char[].class.getName(), false);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockParameterFlowInterfaceSection.class.getName());
	}

	/**
	 * Mock {@link FlowInterface} for the {@link MockFlowInterfaceSection}.
	 */
	@FlowInterface
	public static interface MockParameterFlowInterface {
		void doObject(Object param);

		void doPrimitive(char param);

		void doBoxedPrimitive(Character param);

		void doArray(Object[] param);

		void doPrimitiveArray(char[] param);

		void doBoxedPrimitiveArray(Character[] param);
	}

	/**
	 * Section with a parameter {@link FlowInterface}.
	 */
	public static class MockParameterFlowInterfaceSection {
		public void doInput(MockParameterFlowInterface flows) {
			// Testing type
		}
	}

	/**
	 * Ensure can provide {@link SectionOutput} via {@link Spawn}
	 * {@link FlowInterface}.
	 */
	@Test
	public void spawnFlowInterface() {
		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockSpawnFlowInterfaceSection.class,
				this.configureClassSectionFunction("doInput"));
		expected.addSectionInput("doInput", null);
		expected.addSectionOutput("doOutput", null, false);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockSpawnFlowInterfaceSection.class.getName());
	}

	/**
	 * Mock {@link FlowInterface} for the {@link MockFlowInterfaceSection}.
	 */
	@FlowInterface
	public static interface MockSpawnFlowInterface {
		@Spawn
		void doOutput();
	}

	/**
	 * Section with an {@link FlowInterface}.
	 */
	public static class MockSpawnFlowInterfaceSection {
		public void doInput(MockFlowInterface flows) {
			// Testing type
		}
	}

	/**
	 * Ensure can provide {@link SectionOutput} for escalation.
	 */
	@Test
	public void escalation() {
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
			// Testing type
		}
	}

	/**
	 * Ensure can provide parameter and argument types.
	 */
	@Test
	public void parameterArgument() {
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
		@Next("doOutput")
		public Integer doInput(@Parameter String parameter) {
			return null;
		}
	}

	/**
	 * Ensure can provide {@link SectionObject}.
	 */
	@Test
	public void object() {

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
			// Testing type
		}
	}

	/**
	 * Ensure can provide {@link SectionObject} on static {@link Method}.
	 */
	@Test
	public void objectOnStaticMethod() {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockObjectOnStaticMethodSection.class,
				(designer, namespace) -> {
					SectionFunction function = namespace.addSectionFunction("doInput", "doInput");
					FunctionObject functionObject = function.getFunctionObject(Connection.class.getName());
					SectionObject sectionObject = designer.addSectionObject(Connection.class.getName(),
							Connection.class.getName());
					designer.link(functionObject, sectionObject);
				});
		expected.addSectionInput("doInput", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockObjectOnStaticMethodSection.class.getName());
	}

	/**
	 * Section with object on static {@link Method}.
	 */
	public static class MockObjectOnStaticMethodSection {
		public static void doInput(Connection connection) {
			// Testing type
		}
	}

	/**
	 * Ensure can provide qualified {@link SectionObject}.
	 */
	@Test
	public void qualifiedObject() {

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
			// Testing type
		}
	}

	/**
	 * Ensure can provide qualified {@link SectionObject} by {@link Qualifier} name.
	 */
	@Test
	public void qualifiedObjectByName() {

		final String QUALIFIED_NAME = "test-" + Connection.class.getName();
		final String UNQUALIFIED_NAME = Connection.class.getName();

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockQualifiedObjectByNameSection.class,
				(designer, namespace) -> {
					SectionFunction function = this.addClassSectionFunction(designer, namespace, "doInput", "doInput");

					// Qualified dependency
					FunctionObject qualifiedFunctionObject = function.getFunctionObject(QUALIFIED_NAME);
					SectionObject qualifiedSectionObject = designer.addSectionObject(QUALIFIED_NAME,
							Connection.class.getName());
					qualifiedSectionObject.setTypeQualifier("test");
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
				MockQualifiedObjectByNameSection.class.getName());
	}

	public static class MockQualifiedObjectByNameSection {
		public void doInput(@Qualified("test") Connection qualified, Connection unqualified) {
			// Testing type
		}
	}

	/**
	 * Ensure can provide same {@link Qualifier} on {@link SectionObject} instances
	 * of different types.
	 */
	@Test
	public void sameQualifierOnDifferentObjectTypes() {

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
			// Testing type
		}
	}

	/**
	 * Ensure issue if qualified {@link SectionObject} with more than one
	 * {@link Qualifier}.
	 */
	@Test
	public void multipleQualifiedObject() {

		final MockCompilerIssues issues = new MockCompilerIssues(this.mocks);

		// Enable recording issue
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordCaptureIssues(true);
		CompilerIssue[] cause = issues.recordCaptureIssues(true);
		issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, "NAMESPACE"), FunctionNamespaceNodeImpl.class,
				"Failed to source FunctionNamespaceType definition from ManagedFunctionSource "
						+ SectionClassManagedFunctionSource.class.getName(),
				new InvalidConfigurationError("Method doInput parameter 0 has more than one Qualifier"));
		issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				"Failure loading FunctionNamespaceType from source "
						+ SectionClassManagedFunctionSource.class.getName(),
				cause);

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockMultipleQualifiedObjectSection.class,
				(designer, namespace) -> {
					SectionFunction function = this.addClassSectionFunction(designer, namespace, "doInput", "doInput");
					function.getFunctionObject("Connection");
					designer.addSectionObject(MockQualification.class.getName(), Connection.class.getName());
				});
		expected.addSectionInput("doInput", null);

		// Test
		this.mocks.replayMockObjects();

		// Validate section
		SectionType type = compiler.getSectionLoader().loadSectionType(ClassSectionSource.class,
				MockMultipleQualifiedObjectSection.class.getName(), compiler.createPropertyList());
		assertNull(type, "Should not load type as multiple qualifiers");

		// Verify
		this.mocks.verifyMockObjects();
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
			// Testing type
		}
	}

	/**
	 * Ensure can provide {@link SectionObject} via {@link Dependency}.
	 */
	@Test
	public void dependency() {
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
			// Testing type
		}
	}

	/**
	 * Ensure provide {@link SectionObject} via qualified {@link Dependency}.
	 */
	@Test
	public void qualifiedDependency() {

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
			// Testing type
		}
	}

	/**
	 * Ensure provide {@link SectionObject} via {@link Qualified} name.
	 */
	@Test
	public void qualifiedDependencyByName() {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockQualifiedDependencyByNameSection.class,
				this.configureClassSectionFunction("doInput"));
		expected.addSectionInput("doInput", null);
		SectionObject object = expected.addSectionObject("test-" + Connection.class.getName(),
				Connection.class.getName());
		object.setTypeQualifier("test");

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockQualifiedDependencyByNameSection.class.getName());
	}

	/**
	 * Section with {@link Qualified} {@link Dependency}.
	 */
	public static class MockQualifiedDependencyByNameSection {
		@Qualified("test")
		@Dependency
		Connection connection;

		public void doInput() {
			// Testing type
		}
	}

	/**
	 * Ensure issue if provide {@link SectionObject} via multiple qualifiers for
	 * {@link Dependency}.
	 */
	@Test
	public void mulipleQualifiedDependency() {

		final MockCompilerIssues issues = new MockCompilerIssues(this.mocks);

		// Enable loading with compiler issues
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		CompilerIssue[] cause = issues.recordCaptureIssues(true);
		issues.recordIssue(Node.qualify(OfficeFloorCompiler.TYPE, ClassSectionSource.CLASS_OBJECT_NAME),
				ManagedObjectSourceNodeImpl.class, "Failed to init",
				new InvalidConfigurationError("Field connection has more than one Qualifier"));
		issues.recordIssue(OfficeFloorCompiler.TYPE, SectionNodeImpl.class,
				"Failure loading ManagedObjectType from source " + ClassManagedObjectSource.class.getName(), cause);

		// Test
		this.mocks.replayMockObjects();

		// Validate section
		compiler.getSectionLoader().loadSectionType(ClassSectionSource.class,
				MockMultipleQualifiedDependencySection.class.getName(), compiler.createPropertyList());

		// Verify
		this.mocks.verifyMockObjects();
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
			// Testing type
		}
	}

	/**
	 * Ensure able to access {@link ManagedFunctionContext}.
	 */
	@Test
	public void managedFunctionContext() throws Exception {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockManagedFunctionContextSection.class,
				this.configureClassSectionFunction("function"));
		expected.addSectionInput("function", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockManagedFunctionContextSection.class.getName());
	}

	public static class MockManagedFunctionContextSection {
		public void function(ManagedFunctionContext<?, ?> context) {
			// Testing
		}
	}

	/**
	 * Ensure able to access {@link Logger}.
	 */
	@Test
	public void logger() throws Exception {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockLoggerSection.class,
				this.configureClassSectionFunction("function"));
		expected.addSectionInput("function", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class, MockLoggerSection.class.getName());
	}

	public static class MockLoggerSection {
		public void function(Logger logger) {
			// Testing
		}
	}

	/**
	 * Ensure able to access {@link AsynchronousFlow}.
	 */
	@Test
	public void asynchronousFlow() throws Exception {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockAsynchronousFlowSection.class,
				this.configureClassSectionFunction("function"));
		expected.addSectionInput("function", null);

		// Validate section
		SectionLoaderUtil.validateSection(expected, ClassSectionSource.class,
				MockAsynchronousFlowSection.class.getName());
	}

	public static class MockAsynchronousFlowSection {
		public void function(AsynchronousFlow flowOne, AsynchronousFlow flowTwo) {
			// Testing
		}
	}

	/**
	 * Ensure {@link Var} dependencies are not exposed. They are managed via
	 * {@link VariableOfficeExtensionService}.
	 */
	@Test
	public void variablesNotExposed() throws Exception {

		// Create the expected section
		SectionDesigner expected = this.createSectionDesigner(MockVariableSection.class,
				this.configureClassSectionFunction("variables"));
		expected.addSectionInput("variables", null);

		// Validate section
		SectionLoaderUtil.validateSectionType(expected, ClassSectionSource.class, MockVariableSection.class.getName());
	}

	public static class MockVariableSection {
		public void variables(@Val Long value, In<Integer> in, Var<String> variable, Out<Character> out,
				@MockQualification @Val Long namedValue, @MockQualification In<Integer> namedIn,
				@MockQualification Var<String> namedVariable, @MockQualification Out<Character> namedOut) {
			// Testing
		}
	}

	/**
	 * Ensure can configure a {@link ManagedObject}.
	 */
	@Test
	public void managedObject() throws Exception {

		// Managed object internal, so must run to test
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((architect, context) -> {
			architect.addOfficeSection("test", ClassSectionSource.class.getName(),
					MockManagedObjectSection.class.getName());
		});

		try {

			// Run to ensure obtained message
			ReturnValue returnValue = new ReturnValue();
			officeFloor.getOffice("OFFICE").getFunctionManager("test.doInput").invokeProcess(returnValue, null);
			assertEquals("test", returnValue.value, "Incorrect value from managed object");

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
				@PropertyValue(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockManagedObject.class) })
		private MockManagedObject managedObject;

		public void doInput(@Parameter ReturnValue returnValue) {
			returnValue.value = this.managedObject.getMessage();
		}
	}

	/**
	 * Ensure can configure a {@link ManagedObject} with a dependency.
	 */
	@Test
	public void managedObjectWithDependency() throws Exception {

		// Managed object internal, so must run to test
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((architect, context) -> {
			architect.addOfficeSection("test", ClassSectionSource.class.getName(),
					MockManagedObjectWithDependencySection.class.getName());
		});

		try {

			// Run to ensure obtained message
			ReturnValue returnValue = new ReturnValue();
			officeFloor.getOffice("OFFICE").getFunctionManager("test.doInput").invokeProcess(returnValue, null);
			assertEquals("test", returnValue.value, "Incorrect value from managed object");

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
				@PropertyValue(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockManagedObjectWithDependency.class) })
		private MockManagedObjectWithDependency managedObjectWithDependency;

		@ManagedObject(source = ClassManagedObjectSource.class, properties = {
				@PropertyValue(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockManagedObject.class) })
		MockManagedObject managedObject;

		public void doInput(@Parameter ReturnValue returnValue) {
			returnValue.value = this.managedObjectWithDependency.getMessage();
		}
	}

	/**
	 * Ensure can qualify the {@link ManagedObject}.
	 */
	@Test
	public void qualifiedManagedObject() {

		// Create the expected section type
		SectionDesigner type = this.createSectionDesigner(MockQualifiedManagedObjectSection.class,
				this.configureClassSectionFunction("function"));
		type.addSectionInput("function", null);
		String moName = MockQualifier.class.getName() + "-" + String.class.getName();
		SectionManagedObjectSource mos = type.addSectionManagedObjectSource(moName,
				ClassManagedObjectSource.class.getName());
		mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, MockQualifiedManagedObject.class.getName());
		SectionManagedObject mo = mos.addSectionManagedObject(moName, ManagedObjectScope.PROCESS);
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
						@PropertyValue(name = ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, valueClass = MockQualifiedManagedObject.class) })
		MockQualifiedManagedObject managedObject;

		public void function() {
		}
	}

	/**
	 * Ensure can internally invoke flows within the section.
	 */
	@Test
	public void internalFlow() throws Exception {

		// Triggering flows, so must run to test
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((architect, context) -> {
			architect.addOfficeSection("test", ClassSectionSource.class.getName(),
					MockInternalFlowSection.class.getName());
		});

		try {

			// Run to ensure obtained message
			ReturnValue returnValue = new ReturnValue();
			officeFloor.getOffice("OFFICE").getFunctionManager("test.doFirst").invokeProcess(returnValue, null);
			assertEquals("one-two-three", returnValue.value, "Incorrect value from flow");

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

		@Next("doSecond")
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
	 * Ensure spawn {@link ThreadState}.
	 */
	@Test
	public void spawnFlow() throws Exception {

		// Triggering flows, so must run to test
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((architect, context) -> {
			architect.addOfficeSection("test", ClassSectionSource.class.getName(),
					MockSpawnFlowSection.class.getName());

			// Provide thread bound managed object to determine spawning
			OfficeManagedObjectSource mos = architect.addOfficeManagedObjectSource("MO",
					ClassManagedObjectSource.class.getName());
			mos.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, SpawnDependency.class.getName());
			mos.addOfficeManagedObject("MO", ManagedObjectScope.THREAD);
			architect.enableAutoWireObjects();
		});

		try {

			// Run to ensure obtained message
			SpawnReturnValue returnValue = new SpawnReturnValue();
			CompleteFlowCallback complete = new CompleteFlowCallback();
			officeFloor.getOffice("OFFICE").getFunctionManager("test.doSpawnTrigger").invokeProcess(returnValue,
					complete);
			complete.assertComplete(this.threading);

			// Ensure appropriately spawned
			assertNotNull(returnValue.trigger, "Should have trigger dependency");
			assertNotNull(returnValue.spawned, "Should have spawn dependency");
			assertNotNull(returnValue.notSpawned, "Should have not spawn dependency");

			// Ensure appropriately spawned
			assertSame(returnValue.trigger, returnValue.notSpawned, "Should not spawn");
			assertNotSame(returnValue.trigger, returnValue.spawned, "Should spawn (so new thread scoped dependency)");

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Spawn dependency to track if {@link ThreadState} spawned.
	 */
	public static class SpawnDependency {
	}

	/**
	 * Tracks the {@link SpawnDependency}.
	 */
	public static class SpawnReturnValue {

		public SpawnDependency trigger;

		public SpawnDependency spawned;

		public SpawnDependency notSpawned;
	}

	/**
	 * Mock {@link FlowInterface} for internal flows.
	 */
	@FlowInterface
	public static interface MockSpawnFlows {

		void notSpawned(SpawnReturnValue returnValue);

		@Spawn
		void spawned(SpawnReturnValue returnValue);
	}

	/**
	 * Section to undertake internal flows.
	 */
	public static class MockSpawnFlowSection {

		public void doSpawnTrigger(@Parameter SpawnReturnValue returnValue, SpawnDependency dependency,
				MockSpawnFlows flows) {
			returnValue.trigger = dependency;
			flows.spawned(returnValue);
			flows.notSpawned(returnValue);
		}

		public void spawned(@Parameter SpawnReturnValue returnValue, SpawnDependency dependency) {
			returnValue.spawned = dependency;
		}

		public void notSpawned(@Parameter SpawnReturnValue returnValue, SpawnDependency dependency) {
			returnValue.notSpawned = dependency;
		}
	}

	/**
	 * Ensure able to handle an escalation internally.
	 */
	@Test
	public void escalationHandling() throws Exception {

		// Triggering flows, so must run to test
		ReturnValue returnValue = new ReturnValue();
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((architect, context) -> {
			architect.enableAutoWireObjects();
			Singleton.load(architect, returnValue);
			architect.addOfficeSection("test", ClassSectionSource.class.getName(),
					MockEscalationHandlingSection.class.getName());
		});

		try {

			// Run to ensure obtained message
			officeFloor.getOffice("OFFICE").getFunctionManager("test.triggerEscalation").invokeProcess(null, null);
			assertEquals("test", returnValue.value, "Incorrect value from handling escalation");

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
	 * Ensure that an escalation method can not handle its own {@link Escalation}.
	 */
	@Test
	public void avoidCyclicEscalationHandling() throws Exception {

		final IOException[] escalated = new IOException[1];

		// Configure to handle escalation
		CompileOffice compile = new CompileOffice();
		compile.getOfficeFloorCompiler().setEscalationHandler(new EscalationHandler() {
			@Override
			public void handleEscalation(Throwable escalation) throws Throwable {
				escalated[0] = (IOException) escalation;
			}
		});
		OfficeFloor officeFloor = compile.compileAndOpenOffice((architect, context) -> {
			architect.addOfficeSection("test", ClassSectionSource.class.getName(),
					MockAvoidCyclicEscalationHandling.class.getName());
		});

		try {

			// Run triggering escalation
			final IOException escalation = new IOException("TEST");
			officeFloor.getOffice("OFFICE").getFunctionManager("test.handleEscalation").invokeProcess(escalation, null);

			// Ensure not handling itself (escalated to OfficeFloor level)
			assertEquals(escalation, escalated[0], "Incorrect escalation");

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
	 * Ensure can pass parameters via {@link FlowInterface}.
	 */
	@Test
	public void flowInterfaceParameter() throws Throwable {

		// Configure flows
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		CompileVar<Object> object = new CompileVar<>();
		CompileVar<Character> primitive = new CompileVar<>();
		CompileVar<Character> boxedPrimitive = new CompileVar<>();
		CompileVar<Object[]> objectArray = new CompileVar<>();
		CompileVar<char[]> primitiveArray = new CompileVar<>();
		CompileVar<Character[]> boxedPrimitiveArray = new CompileVar<>();
		compiler.office((context) -> {
			context.addSection("SECTION", MockPassParameterFlowInterfaceSection.class);
			context.variable(null, Object.class, object);
			context.variable("primitive", Character.class, primitive);
			context.variable(null, Character.class, boxedPrimitive);
			context.variable(null, Object[].class, objectArray);
			context.variable(null, char[].class, primitiveArray);
			context.variable(null, Character[].class, boxedPrimitiveArray);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {

			// Object
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", "doObject");
			assertEquals("1", object.getValue(), "Incorrect object");

			// Primitive
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", "doPrimitive");
			assertEquals(Character.valueOf('2'), primitive.getValue(), "Incorrect primitive");

			// Boxed primitive
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", "doBoxedPrimitive");
			assertEquals(Character.valueOf('3'), boxedPrimitive.getValue(), "Incorrect primitive");

			// Object array
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", "doObjectArray");
			assertEquals("4", objectArray.getValue()[0], "Incorrect object array");

			// Primitive array
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", "doPrimitiveArray");
			assertEquals('5', primitiveArray.getValue()[0], "Incorrect primitive");

			// Boxed primitive
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", "doBoxedPrimitiveArray");
			assertEquals(Character.valueOf('6'), boxedPrimitiveArray.getValue()[0], "Incorrect primitive");
		}
	}

	public static class MockPassParameterFlowInterfaceSection {

		public void service(@Parameter String option, ParameterFlowInterface flows) {
			switch (option) {
			case "doObject":
				flows.doObject("1");
				break;
			case "doPrimitive":
				flows.doPrimitive('2');
				break;
			case "doBoxedPrimitive":
				flows.doBoxedPrimitive('3');
				break;
			case "doObjectArray":
				flows.doObjectArray(new Object[] { "4" });
				break;
			case "doPrimitiveArray":
				flows.doPrimitiveArray(new char[] { '5' });
				break;
			case "doBoxedPrimitiveArray":
				flows.doBoxedPrimitiveArray(new Character[] { '6' });
				break;
			default:
				fail("Unknown input: " + option);
				break;
			}
		}

		public void doObject(@Parameter Object value, Out<Object> out) {
			out.set(value);
		}

		public void doPrimitive(@Parameter char value, @Qualified("primitive") Out<Character> out) {
			out.set(value);
		}

		public void doBoxedPrimitive(@Parameter Character value, Out<Character> out) {
			out.set(value);
		}

		public void doObjectArray(@Parameter Object[] value, Out<Object[]> out) {
			out.set(value);
		}

		public void doPrimitiveArray(@Parameter char[] value, Out<char[]> out) {
			out.set(value);
		}

		public void doBoxedPrimitiveArray(@Parameter Character[] value, Out<Character[]> out) {
			out.set(value);
		}
	}

	@FlowInterface
	public static interface ParameterFlowInterface {
		void doObject(Object param);

		void doPrimitive(char param);

		void doBoxedPrimitive(Character param);

		void doObjectArray(Object[] param);

		void doPrimitiveArray(char[] param);

		void doBoxedPrimitiveArray(Character[] param);
	}

	/**
	 * Ensure can pass primitive parameters.
	 */
	@Test
	public void primitiveParameters() throws Throwable {

		// Configure flows
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		CompileVar<Boolean> result = new CompileVar<>();
		compiler.office((context) -> {
			context.addSection("SECTION", PrimitiveParametersSection.class);
			context.variable(null, Boolean.class, result);
		});
		try (OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor()) {
			CompileOfficeFloor.invokeProcess(officeFloor, "SECTION.service", Byte.valueOf((byte) 1));
			assertTrue(result.getValue(), "Should pass all the way through");
		}
	}

	public static class PrimitiveParametersSection {

		@Next("doByte")
		public byte service(@Parameter byte param) {
			return param;
		}

		@Next("doShort")
		public short doByte(@Parameter byte param) {
			return param;
		}

		@Next("doChar")
		public char doShort(@Parameter short param) {
			return (char) param;
		}

		@Next("doInt")
		public int doChar(@Parameter char param) {
			return param;
		}

		@Next("doLong")
		public long doInt(@Parameter int param) {
			return param;
		}

		@Next("doFloat")
		public float doLong(@Parameter long param) {
			return param;
		}

		@Next("doDouble")
		public double doFloat(@Parameter float param) {
			return param;
		}

		@Next("doBoolean")
		public boolean doDouble(@Parameter double param) {
			return Math.abs(param - 1) < 0.0000001; // avoid rounding issues
		}

		public void doBoolean(@Parameter boolean param, Out<Boolean> out) {
			out.set(param);
		}
	}

	/**
	 * Ensure can configure a {@link SubSection}.
	 */
	@Test
	public void subSection() throws Exception {

		// Triggering sub section, so must run to test
		ReturnValue returnValue = new ReturnValue();
		OfficeFloor officeFloor = new CompileOffice().compileAndOpenOffice((architect, context) -> {
			architect.enableAutoWireObjects();
			architect.addOfficeSection("test", ClassSectionSource.class.getName(),
					MockInvokeSubSection.class.getName());
			Singleton.load(architect, returnValue);
		});

		try {

			// Run to ensure obtained message
			officeFloor.getOffice("OFFICE").getFunctionManager("test.doFirst").invokeProcess(null, null);
			assertEquals("sub section", returnValue.value, "Incorrect value from sub section");

		} finally {
			// Ensure closed
			officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Mock {@link SectionInterface} for invoking a {@link SubSection}.
	 */
	@SectionInterface(source = ClassSectionSource.class, locationClass = MockSubSection.class, outputs = {
			@SectionOutputLink(name = "output", link = "doLast") })
	public static interface MockSectionInterface {
		void doSubSectionInput();
	}

	/**
	 * Section to invoke sub section.
	 */
	public static class MockSubSection {
		@Next("output")
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
		 * @param designer  {@link SectionDesigner}.
		 * @param namespace {@link SectionFunctionNamespace} to configure.
		 */
		void configureNamespace(SectionDesigner designer, SectionFunctionNamespace namespace);
	}

	/**
	 * Creates the expected {@link SectionDesigner} with pre-populated details.
	 * 
	 * @param sectionClass        Section class.
	 * @param namespaceConfigurer {@link NamespaceConfigurer}.
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
	 * Convenience method to add {@link ClassSectionSource} {@link SectionFunction}.
	 * 
	 * @param functionName {@link SectionFunction} and {@link ManagedFunctionType}
	 *                     name.
	 * @return {@link NamespaceConfigurer}.
	 */
	public NamespaceConfigurer configureClassSectionFunction(String functionName) {
		return this.configureClassSectionFunction(functionName, functionName);
	}

	/**
	 * Convenience method to add {@link ClassSectionSource} {@link SectionFunction}.
	 * 
	 * @param functionName     {@link SectionFunction} name.
	 * @param functionTypeName {@link ManagedFunctionType} name.
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
	 * @param designer         {@link SectionDesigner}.
	 * @param namespace        {@link SectionFunctionNamespace}.
	 * @param functionName     {@link SectionFunction} name.
	 * @param functionTypeName {@link ManagedFunctionType} name.
	 * @return {@link SectionFunction}.
	 */
	public SectionFunction addClassSectionFunction(SectionDesigner designer, SectionFunctionNamespace namespace,
			String functionName, String functionTypeName) {
		SectionFunction function = namespace.addSectionFunction(functionName, functionTypeName);
		FunctionObject functionObject = function.getFunctionObject(ClassSectionSource.CLASS_OBJECT_NAME);
		designer.link(functionObject, this.objectManagedObject);
		return function;
	}

}
