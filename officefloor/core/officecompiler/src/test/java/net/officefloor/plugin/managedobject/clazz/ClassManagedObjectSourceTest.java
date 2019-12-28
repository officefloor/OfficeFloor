/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedobject.clazz;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.logging.Logger;

import org.easymock.AbstractMatcher;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.clazz.FlowInterface;
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
		expected.addDependency("connection", Connection.class, null, 0, null);
		expected.addDependency("qualifiedDependency", String.class, MockQualifier.class.getName(), 1, null);
		expected.addDependency("unqualifiedDependency", String.class, null, 2, null);

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
		expected.addDependency("OverrideMockClass.connection", Integer.class, null, 0, null);
		expected.addDependency("ParentMockClass.connection", Connection.class, null, 1, null);

		// Processes
		expected.addFlow(OverrideMockClass.class.getName() + ".processes.doProcess", null, 0, null);
		expected.addFlow(OverrideMockClass.class.getName() + ".processes.parameterisedProcess", Integer.class, 1, null);
		expected.addFlow(ParentMockClass.class.getName() + ".processes.doProcess", null, 2, null);
		expected.addFlow(ParentMockClass.class.getName() + ".processes.parameterisedProcess", Integer.class, 3, null);

		// Verify extension interface
		expected.addExtensionInterface(OverrideMockClass.class);

		// Validate the managed object type
		ManagedObjectLoaderUtil.validateManagedObjectType(expected, ClassManagedObjectSource.class,
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, OverrideMockClass.class.getName());
	}

	/**
	 * Ensure issue if dependency with multiple qualifiers.
	 */
	@SuppressWarnings("rawtypes")
	public void testMultipleQualifiersOnDependency() {

		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Enable using compiler issues
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Record issue
		issues.recordIssue("Failed to init",
				new IllegalArgumentException("Dependency connection has more than one Qualifier"));

		// Test
		this.replayMockObjects();

		// Properties
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(MockMultipleQualifiedClass.class.getName());

		// Validate the managed object type
		ManagedObjectType type = compiler.getManagedObjectLoader().loadManagedObjectType(ClassManagedObjectSource.class,
				properties);
		assertNull("Should not load type", type);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Another {@link Qualifier}.
	 */
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MockAnotherQualifier {
	}

	/**
	 * Mock multiple {@link Qualifier} instances on dependency.
	 */
	public static class MockMultipleQualifiedClass {

		@MockQualifier
		@MockAnotherQualifier
		@Dependency
		Connection connection;
	}

	/**
	 * Ensures can inject {@link Dependency} instances into the object.
	 */
	@SuppressWarnings("unchecked")
	public void testInjectDependencies() throws Throwable {

		final String UNQUALIFIED_DEPENDENCY = "SELECT * FROM UNQUALIFIED";
		final String QUALIFIED_DEPENDENCY = "SELECT NAME FROM QUALIFIED";
		final Connection connection = this.createMock(Connection.class);
		final String MO_BOUND_NAME = "MO";
		final ObjectRegistry<Indexed> objectRegistry = this.createMock(ObjectRegistry.class);

		// Record obtaining the dependencies
		this.recordReturn(objectRegistry, objectRegistry.getObject(0), connection);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1), QUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(2), UNQUALIFIED_DEPENDENCY);

		// Replay mocks
		this.replayMockObjects();

		// Load the class managed object source
		ManagedObjectSourceStandAlone standAlone = new ManagedObjectSourceStandAlone();
		standAlone.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME, MockClass.class.getName());
		ClassManagedObjectSource source = standAlone.loadManagedObjectSource(ClassManagedObjectSource.class);

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
		mockClass.verifyDependencyInjection(UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY,
				Logger.getLogger(MO_BOUND_NAME), connection);

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
		this.recordReturn(objectRegistry, objectRegistry.getObject(0), connection);
		this.recordReturn(objectRegistry, objectRegistry.getObject(1), QUALIFIED_DEPENDENCY);
		this.recordReturn(objectRegistry, objectRegistry.getObject(2), UNQUALIFIED_DEPENDENCY);

		// Record invoking the processes
		this.recordReturn(executeContext, executeContext.invokeProcess(0, null, null, 0, null), null);
		this.control(executeContext).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				boolean isMatch = true;
				// Ensure process indexes match
				isMatch &= (expected[0].equals(actual[0]));

				// Ensure parameters match
				isMatch &= ((expected[1] == null ? "null" : expected[1])
						.equals((actual[1] == null ? "null" : actual[1])));

				// Ensure have a managed object
				assertNotNull("Must have managed object", actual[2]);
				assertTrue("Incorrect managed object type", actual[2] instanceof ClassManagedObject);

				// Ensure delay matches
				isMatch &= (expected[3].equals(actual[3]));

				// Return whether matched
				return isMatch;
			}
		});
		this.recordReturn(executeContext, executeContext.invokeProcess(1, PROCESS_PARAMETER, null, 0, null), null);

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
		mockClass.verifyDependencyInjection(UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY,
				Logger.getLogger(MO_BOUND_NAME), connection);

		// Verify the processes injected
		mockClass.verifyProcessInjection(PROCESS_PARAMETER);

		// Verify functionality
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to instantiate a new instances for unit testing.
	 */
	public void testNewInstance() throws Exception {

		final Connection connection = this.createMock(Connection.class);
		final String QUALIFIED_DEPENDENCY = "SELECT NAME FROM QUALIFIED";
		final String UNQUALIFIED_DEPENDENCY = "SELECT * FROM UNQUALIFIED";
		final MockProcessInterface processInterface = this.createMock(MockProcessInterface.class);
		final Integer PROCESS_PARAMETER = Integer.valueOf(200);
		final Logger logger = Logger.getLogger("MO");
		final ManagedObjectContext moContext = this.createMock(ManagedObjectContext.class);

		// Record obtaining logger
		this.recordReturn(moContext, moContext.getLogger(), logger);

		// Record invoking processes
		processInterface.doProcess();
		processInterface.parameterisedProcess(PROCESS_PARAMETER);

		// Replay mock objects
		this.replayMockObjects();

		// Create the instance
		MockClass mockClass = ClassManagedObjectSource.newInstance(MockClass.class, "unqualifiedDependency",
				UNQUALIFIED_DEPENDENCY, "qualifiedDependency", QUALIFIED_DEPENDENCY, "connection", connection,
				"processes", processInterface, "logger", logger, "context", moContext);

		// Verify the dependencies injected
		mockClass.verifyDependencyInjection(UNQUALIFIED_DEPENDENCY, QUALIFIED_DEPENDENCY, logger, connection);

		// Verify the process interfaces injected
		mockClass.verifyProcessInjection(PROCESS_PARAMETER);

		// Verify mock objects
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link FlowInterface}.
	 */
	@FlowInterface
	public static interface MockProcessInterface {

		/**
		 * Method to invoke a {@link ProcessState} without a parameter.
		 */
		void doProcess();

		/**
		 * Method to invoke a {@link ProcessState} with a parameter.
		 * 
		 * @param parameter Parameter to the {@link ProcessState}.
		 */
		void parameterisedProcess(Integer parameter);
	}

	/**
	 * {@link Qualifier} for the {@link Dependency}.
	 */
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	private @interface MockQualifier {
	}

	/**
	 * Mock class for testing.
	 */
	public static class MockClass extends ParentMockClass {

		/**
		 * Ensure can inject dependencies.
		 */
		@Dependency
		private String unqualifiedDependency;

		/**
		 * Qualified injected dependency.
		 */
		@MockQualifier
		@Dependency
		private String qualifiedDependency;

		/**
		 * {@link Logger}.
		 */
		@Dependency
		private Logger logger;

		/**
		 * {@link ManagedObjectContext}.
		 */
		@Dependency
		private ManagedObjectContext context;

		/**
		 * Verifies the dependencies.
		 * 
		 * @param unqualifiedDependency Unqualified dependency.
		 * @param qualifiedDependency   Qualified dependency.
		 * @param logger                {@link Logger}.
		 * @param connection            Expected {@link Connection}.
		 */
		public void verifyDependencyInjection(String unqualifiedDependency, String qualifiedDependency, Logger logger,
				Connection connection) {

			// Verify dependency injection
			assertNotNull("Expecting unqualified dependency", unqualifiedDependency);
			assertEquals("Incorrect unqualified dependency", unqualifiedDependency, this.unqualifiedDependency);
			assertNotNull("Expecting qualified dependency", qualifiedDependency);
			assertEquals("Incorrect qualified dependency", qualifiedDependency, this.qualifiedDependency);
			assertEquals("Incorrect logger", this.logger.getName(), logger.getName());
			assertNotNull("Should have managed object context", this.context);
			assertSame("Should be same logger from managed object context", this.logger, this.context.getLogger());

			// Verify parent dependencies
			super.verifyDependencyInjection(connection);
		}
	}

	/**
	 * Parent mock class for testing.
	 */
	public static class ParentMockClass {

		/**
		 * {@link Connection}.
		 */
		@Dependency
		private Connection connection;

		/**
		 * Ensure can invoke {@link ProcessState}.
		 */
		private MockProcessInterface processes;

		/**
		 * Field not a dependency.
		 */
		protected String notDependency;

		/**
		 * Verifies the dependencies injected.
		 * 
		 * @param connection Expected {@link Connection}.
		 */
		public void verifyDependencyInjection(Connection connection) {
			// Verify dependency injection
			TestCase.assertEquals("Incorrect connection", connection, this.connection);
		}

		/**
		 * Verifies the processes injected.
		 * 
		 * @param processParameter Parameter for the invoked processes.
		 */
		public void verifyProcessInjection(Integer processParameter) {
			// Verify can invoke processes
			this.processes.doProcess();
			this.processes.parameterisedProcess(processParameter);
		}
	}

	/**
	 * Override mock class.
	 */
	public static class OverrideMockClass extends ParentMockClass {

		/**
		 * Overriding connection field.
		 */
		@Dependency
		protected Integer connection;

		/**
		 * Overriding process field.
		 */
		protected MockProcessInterface processes;
	}

}