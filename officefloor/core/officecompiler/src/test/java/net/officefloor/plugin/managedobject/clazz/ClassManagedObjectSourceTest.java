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

package net.officefloor.plugin.managedobject.clazz;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.logging.Logger;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.FlowSuccessful;
import net.officefloor.plugin.clazz.InvalidConfigurationError;
import net.officefloor.plugin.clazz.Qualifier;

/**
 * Tests the {@link ClassManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensures specification correct.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(ClassManagedObjectSource.class,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, "Class");
	}

	/**
	 * Ensure able to load the {@link ManagedObjectType} for the
	 * {@link ClassManagedObjectSource}.
	 */
	public void testManagedObjectType() {

		// Create the managed object type builder for the expected type
		ManagedObjectTypeBuilder expected = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();

		// Ensure correct object type
		expected.setObjectClass(MockClass.class);

		// Has input flow
		expected.setInput(true);

		// Dependencies
		expected.addDependency(String.class.getName(), String.class, null, 0, null, Dependency.class);
		expected.addDependency(MockQualifier.class.getName() + "-" + String.class.getName(), String.class,
				MockQualifier.class.getName(), 1, null, MockQualifier.class, Dependency.class);
		expected.addDependency(Connection.class.getName(), Connection.class, null, 2, null, Dependency.class);

		// Processes
		expected.addFlow("doProcess", null, 0, null);
		expected.addFlow("parameterisedProcess", Integer.class, 1, null);

		// Class should be the extension interface to allow administration
		// (Allows implemented interfaces to also be extension interfaces)
		expected.addExtensionInterface(MockClass.class);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(expected, ClassManagedObjectSource.class,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());
	}

	/**
	 * Ensure can inject using the single constructor.
	 */
	public void testSingleConstructorInject() throws Throwable {
		this.doInjectDependenciesTest(MockClass.class);
	}

	/**
	 * Ensure can specify the {@link Constructor} to use if multiple.
	 */
	public void testSpecifyConstructorInject() throws Throwable {
		this.doInjectDependenciesTest(SpecifyConstructorMockClass.class);
	}

	public static class SpecifyConstructorMockClass extends MockClass {

		public SpecifyConstructorMockClass() {
			super(null, null);
		}

		@Dependency
		public SpecifyConstructorMockClass(String unqualifiedConstructorDependency,
				@MockQualifier String qualifiedConstructorDependency) {
			super(unqualifiedConstructorDependency, qualifiedConstructorDependency);
		}

		public SpecifyConstructorMockClass(String unused) {
			super(unused, null);
		}
	}

	/**
	 * Ensures can inject {@link Dependency} instances into the object.
	 * 
	 * @param clazz Class to instantiate and inject dependencies.
	 */
	@SuppressWarnings("unchecked")
	public void doInjectDependenciesTest(Class<?> clazz) throws Throwable {

		final String UNQUALIFIED_DEPENDENCY = "SELECT * FROM UNQUALIFIED";
		final String QUALIFIED_DEPENDENCY = "SELECT NAME FROM QUALIFIED";
		final Connection connection = this.createMock(Connection.class);
		final String MO_BOUND_NAME = "MO";
		final ObjectRegistry<Indexed> objectRegistry = this.createMock(ObjectRegistry.class);

		// Record obtaining the dependencies
		this.recordReturn(objectRegistry, objectRegistry.getObject(0), UNQUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1), QUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(2), connection);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1), QUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(0), UNQUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(0), UNQUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1), QUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(0), UNQUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1), QUALIFIED_DEPENDENCY);

		// Replay mocks
		this.replayMockObjects();

		// Load the class managed object source
		ManagedObjectSourceStandAlone standAlone = new ManagedObjectSourceStandAlone();
		standAlone.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, clazz.getName());
		ClassManagedObjectSource source = standAlone.loadManagedObjectSource(ClassManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setBoundManagedObjectName(MO_BOUND_NAME);
		user.setObjectRegistry(objectRegistry);
		ManagedObject managedObject = user.sourceManagedObject(source);
		assertTrue("Managed object must be coordinating", managedObject instanceof CoordinatingManagedObject);

		// Obtain the object and validate correct type
		Object object = managedObject.getObject();
		assertTrue("Incorrect object type: " + (object == null ? null : object.getClass().getName()),
				object instanceof MockClass);
		MockClass mockClass = (MockClass) object;

		// Verify the dependencies injected
		mockClass.verifyDependencyInjection(UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY, UNQUALIFIED_DEPENDENCY,
				QUALIFIED_DEPENDENCY, UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY, Logger.getLogger(MO_BOUND_NAME),
				connection, UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure can inject the {@link FlowInterface} with compiled implementations.
	 */
	public void testInjectProcessInterfacesWithCompiling() throws Throwable {
		SourceContext sourceContext = OfficeFloorCompiler.newOfficeFloorCompiler(this.getClass().getClassLoader())
				.createRootSourceContext();
		assertNotNull("Ensure Java compiler available", OfficeFloorJavaCompiler.newInstance(sourceContext));
		this.doInjectProcessInterfacesTest();
	}

	/**
	 * Ensure can fallback to {@link Proxy} implementation of {@link FlowInterface}
	 * if no Java compiler.
	 */
	public void testInjectProcessInterfacesWithDynamicProxy() throws Throwable {
		OfficeFloorJavaCompiler.runWithoutCompiler(() -> this.doInjectProcessInterfacesTest());
	}

	/**
	 * Ensures can inject the {@link FlowInterface} instances into the object.
	 */
	@SuppressWarnings("unchecked")
	public void doInjectProcessInterfacesTest() throws Throwable {

		final String QUALIFIED_DEPENDENCY = "SELECT NAME FROM QUALIFIED";
		final String UNQUALIFIED_DEPENDENCY = "SELECT * FROM UNQUALIFIED";
		final Connection connection = this.createMock(Connection.class);
		final String MO_BOUND_NAME = "MO";
		final ObjectRegistry<Indexed> objectRegistry = this.createMock(ObjectRegistry.class);
		final ManagedObjectExecuteContext<Indexed> executeContext = this.createMock(ManagedObjectExecuteContext.class);
		final Integer PROCESS_PARAMETER = Integer.valueOf(100);

		// Record obtaining the dependencies
		this.recordReturn(objectRegistry, objectRegistry.getObject(0), UNQUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1), QUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(2), connection);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1), QUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(0), UNQUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(0), UNQUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1), QUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(0), UNQUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1), QUALIFIED_DEPENDENCY);

		// Record invoking the processes
		this.recordReturn(executeContext, executeContext.invokeProcess(this.param(0), this.param(null),
				this.paramType(ClassManagedObject.class), this.param(0L), this.param(null)), null);
		this.recordReturn(executeContext, executeContext.invokeProcess(this.param(1), this.param(PROCESS_PARAMETER),
				this.paramType(ClassManagedObject.class), this.param(0L), this.param(null)), null);

		// Replay mocks
		this.replayMockObjects();

		// Load the class managed object source
		ManagedObjectSourceStandAlone standAlone = new ManagedObjectSourceStandAlone();
		standAlone.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());
		ClassManagedObjectSource source = standAlone.initManagedObjectSource(ClassManagedObjectSource.class);
		source.start(executeContext);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.setBoundManagedObjectName(MO_BOUND_NAME);
		user.setObjectRegistry(objectRegistry);
		ManagedObject managedObject = user.sourceManagedObject(source);
		assertTrue("Managed object must be coordinating", managedObject instanceof CoordinatingManagedObject);

		// Obtain the object and validate correct type
		Object object = managedObject.getObject();
		assertTrue("Incorrect object type", object instanceof MockClass);
		MockClass mockClass = (MockClass) object;

		// Verify the dependencies injected
		mockClass.verifyDependencyInjection(UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY, UNQUALIFIED_DEPENDENCY,
				QUALIFIED_DEPENDENCY, UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY, Logger.getLogger(MO_BOUND_NAME),
				connection, UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY);

		// Verify the processes injected
		mockClass.verifyProcessInjection(PROCESS_PARAMETER);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if multiple constructors and one not specified.
	 */
	@SuppressWarnings("rawtypes")
	public void testIssueIfMultipleConstructors() {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Enable using compiler issues
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue("Failed to init",
				new InvalidConfigurationError("Must specify one of the constructors with Dependency for "
						+ MultipleConstructorsClass.class.getName()));

		// Test
		this.replayMockObjects();

		// Properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(MultipleConstructorsClass.class.getName());

		// Validate the managed object type
		ManagedObjectType type = compiler.getManagedObjectLoader().loadManagedObjectType(ClassManagedObjectSource.class,
				properties);
		assertNull("Should not load type", type);

		// Verify
		this.verifyMockObjects();
	}

	public class MultipleConstructorsClass {

		public MultipleConstructorsClass() {
			// Test
		}

		public MultipleConstructorsClass(Integer dependency) {
			// Test
		}

		public MultipleConstructorsClass(String notDependency) {
			// Test
		}
	}

	/**
	 * Ensure issue if specify multiple constructors for dependency injection.
	 */
	@SuppressWarnings("rawtypes")
	public void testIssueIfMultipleSpecifiedConstructors() {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Enable using compiler issues
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue("Failed to init",
				new InvalidConfigurationError("Multiple constructors annotated with Dependency for "
						+ MultipleSpecifiedConstructorsClass.class.getName()));

		// Test
		this.replayMockObjects();

		// Properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(MultipleSpecifiedConstructorsClass.class.getName());

		// Validate the managed object type
		ManagedObjectType type = compiler.getManagedObjectLoader().loadManagedObjectType(ClassManagedObjectSource.class,
				properties);
		assertNull("Should not load type", type);

		// Verify
		this.verifyMockObjects();
	}

	public class MultipleSpecifiedConstructorsClass {

		@Dependency
		public MultipleSpecifiedConstructorsClass() {
			// Test
		}

		@Dependency
		public MultipleSpecifiedConstructorsClass(Integer dependency) {
			// Test
		}

		public MultipleSpecifiedConstructorsClass(String notDependency) {
			// Test
		}
	}

	/**
	 * Ensure issue if constructor dependency with multiple qualifiers.
	 */
	@SuppressWarnings("rawtypes")
	public void testIssueIfMultipleQualifiersOnConstructorDependency() {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Enable using compiler issues
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue("Failed to init",
				new InvalidConfigurationError("Constructor parameter 0 has more than one Qualifier"));

		// Test
		this.replayMockObjects();

		// Properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(MockMultipleQualifiedConstructorDependencyClass.class.getName());

		// Validate the managed object type
		ManagedObjectType type = compiler.getManagedObjectLoader().loadManagedObjectType(ClassManagedObjectSource.class,
				properties);
		assertNull("Should not load type", type);

		// Verify
		this.verifyMockObjects();
	}

	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MockAnotherQualifier {
	}

	public static class MockMultipleQualifiedConstructorDependencyClass {

		public MockMultipleQualifiedConstructorDependencyClass(
				@MockQualifier @MockAnotherQualifier Connection connection) {
			// Test constructor
		}
	}

	/**
	 * Ensure issue if field dependency with multiple qualifiers.
	 */
	@SuppressWarnings("rawtypes")
	public void testIssueIfMultipleQualifiersOnFieldDependency() {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Enable using compiler issues
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue("Failed to init",
				new InvalidConfigurationError("Field connection has more than one Qualifier"));

		// Test
		this.replayMockObjects();

		// Properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(MockMultipleQualifiedFieldDependencyClass.class.getName());

		// Validate the managed object type
		ManagedObjectType type = compiler.getManagedObjectLoader().loadManagedObjectType(ClassManagedObjectSource.class,
				properties);
		assertNull("Should not load type", type);

		// Verify
		this.verifyMockObjects();
	}

	public static class MockMultipleQualifiedFieldDependencyClass {

		@MockQualifier
		@MockAnotherQualifier
		private @Dependency Connection connection;
	}

	/**
	 * Ensure able to load the {@link ManagedObjectType} when child class has same
	 * field name.
	 */
	public void testOverrideField() {

		// Create the managed object type builder for the expected type
		ManagedObjectTypeBuilder expected = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();

		// Ensure correct object type
		expected.setObjectClass(OverrideMockClass.class);

		// Has input flow
		expected.setInput(true);

		// Dependencies
		expected.addDependency(Integer.class.getName(), Integer.class, null, 0, null);
		expected.addDependency(Connection.class.getName(), Connection.class, null, 1, null);

		// Processes
		expected.addFlow("doProcess", null, 0, null);
		expected.addFlow("parameterisedProcess", Integer.class, 1, null);
		expected.addFlow("doOverride", String.class, 2, null);

		// Verify extension interface
		expected.addExtensionInterface(OverrideMockClass.class);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(expected, ClassManagedObjectSource.class,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, OverrideMockClass.class.getName());
	}

	public static class OverrideMockClass extends ParentMockClass {

		protected @Dependency Integer connection;

		protected OverrideMockProcessInterface processes;
	}

	@FlowInterface
	public static interface OverrideMockProcessInterface {

		void doOverride(String parameter, FlowSuccessful successful);

		// Same flows
		void doProcess(FlowCallback callback);

		void parameterisedProcess(Integer parameter);
	}

}