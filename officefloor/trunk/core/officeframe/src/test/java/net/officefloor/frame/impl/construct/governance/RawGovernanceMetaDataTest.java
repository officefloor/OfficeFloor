/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.governance;

import java.net.URL;
import java.net.URLClassLoader;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.impl.execute.governance.ActivateGovernanceTask;
import net.officefloor.frame.impl.execute.governance.DisregardGovernanceTask;
import net.officefloor.frame.impl.execute.governance.EnforceGovernanceTask;
import net.officefloor.frame.impl.execute.governance.GovernGovernanceTask;
import net.officefloor.frame.impl.execute.governance.GovernanceTaskDependency;
import net.officefloor.frame.impl.execute.governance.GovernanceWork;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.ActiveGovernanceControl;
import net.officefloor.frame.internal.structure.ActiveGovernanceManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.source.GovernanceSource;
import net.officefloor.frame.spi.governance.source.GovernanceSourceContext;
import net.officefloor.frame.spi.governance.source.GovernanceSourceMetaData;
import net.officefloor.frame.spi.governance.source.GovernanceSourceSpecification;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.spi.source.UnknownResourceError;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;

/**
 * Tests the {@link RawGovernanceMetaDataFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawGovernanceMetaDataTest extends OfficeFrameTestCase {

	/**
	 * {@link GovernanceConfiguration}.
	 */
	private final GovernanceConfiguration<?, ?, ?> configuration = this
			.createMock(GovernanceConfiguration.class);

	/**
	 * {@link Governance} index within the {@link ProcessState}.
	 */
	private final int GOVERNANCE_INDEX = 3;

	/**
	 * {@link Governance} name.
	 */
	private final String GOVERNANCE_NAME = "GOVERNANCE";

	/**
	 * {@link Office} name.
	 */
	private final String OFFICE_NAME = "OFFICE";

	/**
	 * {@link OfficeBuilder}.
	 */
	private final OfficeBuilder officeBuilder = this
			.createMock(OfficeBuilder.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this
			.createMock(SourceContext.class);

	/**
	 * {@link GovernanceSourceMetaData}.
	 */
	private final GovernanceSourceMetaData<?, ?> metaData = this
			.createMock(GovernanceSourceMetaData.class);

	/**
	 * {@link GovernanceSource} instances to use in testing overriding the
	 * {@link MockGovernanceSource} {@link Class}.
	 */
	private GovernanceSource<?, ?> governanceSourceInstance = null;

	/**
	 * {@link OfficeMetaDataLocator}.
	 */
	private final OfficeMetaDataLocator officeMetaDataLocator = this
			.createMock(OfficeMetaDataLocator.class);

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory assetManagerFactory = this
			.createMock(AssetManagerFactory.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	@Override
	protected void setUp() throws Exception {
		// Reset the mock governance source state
		MockGovernanceSource.reset(this.metaData);
	}

	/**
	 * Ensures issue if no {@link GovernanceSource} name.
	 */
	public void testNoGovernanceSourceName() {

		// Record no name
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceName(), null);
		this.issues.addIssue(AssetType.OFFICE, OFFICE_NAME,
				"Governance added without a name");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link GovernanceSource} class.
	 */
	public void testNoGovernanceSourceClass() {

		// Record no class
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceSource(), null);
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceSourceClass(), null);
		this.record_issue("No GovernanceSource class provided");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if fail to instantiate {@link GovernanceSource}.
	 */
	public void testFailInstantiateGovernanceSource() {

		final Exception failure = new Exception("instantiate failure");

		// Record fail instantiate
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceSource(), null);
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceSourceClass(),
				MockGovernanceSource.class);
		this.record_issue(
				"Failed to instantiate " + MockGovernanceSource.class.getName(),
				failure);

		// Attempt to construct governance
		this.replayMockObjects();
		MockGovernanceSource.instantiateFailure = failure;
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if missing required property.
	 */
	public void testMissingProperty() {

		// Record fail instantiate due to missing property
		this.record_initGovernance();
		this.record_issue("Property 'required.property' must be specified");

		// Attempt to construct governance
		this.replayMockObjects();
		MockGovernanceSource.requiredPropertyName = "required.property";
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if missing {@link Class}.
	 */
	public void testClassLoaderAndMissingClass() {

		final ClassLoader classLoader = new URLClassLoader(new URL[0]);
		final String CLASS_NAME = "UNKNOWN CLASS";

		// Record fail instantiate due to missing class
		this.record_initGovernance();
		this.recordReturn(this.sourceContext,
				this.sourceContext.getClassLoader(), classLoader);
		this.sourceContext.loadClass(CLASS_NAME);
		this.control(this.sourceContext).setThrowable(
				new UnknownClassError("TEST ERROR", CLASS_NAME));
		this.record_issue("Can not load class '" + CLASS_NAME + "'");

		// Attempt to construct governance
		this.replayMockObjects();
		MockGovernanceSource.classLoader = classLoader;
		MockGovernanceSource.requiredClassName = CLASS_NAME;
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if missing a resource.
	 */
	public void testMissingResource() {

		final String RESOURCE_LOCATION = "RESOURCE LOCATION";

		// Record fail instantiate due to missing resource
		this.record_initGovernance();
		this.sourceContext.getResource(RESOURCE_LOCATION);
		this.control(this.sourceContext).setThrowable(
				new UnknownResourceError("TEST ERROR", RESOURCE_LOCATION));
		this.record_issue("Can not obtain resource at location '"
				+ RESOURCE_LOCATION + "'");

		// Attempt to construct governance
		this.replayMockObjects();
		MockGovernanceSource.requiredResourceLocation = RESOURCE_LOCATION;
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if failure in initialising {@link GovernanceSource}.
	 */
	public void testFailInitManagedObjectSource() {

		final Exception failure = new Exception("init failure");

		// Record fail instantiate
		this.record_initGovernance();
		this.record_issue(
				"Failed to initialise " + MockGovernanceSource.class.getName(),
				failure);

		// Attempt to construct governance
		this.replayMockObjects();
		MockGovernanceSource.initFailure = failure;
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if null {@link GovernanceSourceMetaData}.
	 */
	public void testNullMetaData() {

		// Record null meta-data
		this.record_initGovernance();
		this.record_issue("Must provide meta-data");

		// Attempt to construct governance
		this.replayMockObjects();
		MockGovernanceSource.metaData = null;
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no extension interface type.
	 */
	public void testNoExtensionInterfaceType() {

		// Record no object type
		this.record_initGovernance();
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				null);
		this.record_issue("No extension interface type provided");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to handle simple {@link Governance} without {@link Flow}.
	 */
	public void testSimpleGovernance() {

		// Record simple governance
		this.record_initGovernance();
		this.record_createRawMetaData(String.class);
		this.record_setupGovernanceTasks();
		TaskMetaData<?, ?, ?> activateTaskMetaData = this
				.record_linkGovernanceTask("ACTIVATE", true);
		TaskMetaData<?, ?, ?> governTaskMetaData = this
				.record_linkGovernanceTask("GOVERN", true);
		TaskMetaData<?, ?, ?> enforceTaskMetaData = this
				.record_linkGovernanceTask("ENFORCE", true);
		TaskMetaData<?, ?, ?> disregardTaskMetaData = this
				.record_linkGovernanceTask("DISREGARD", true);

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData rawMetaData = this
				.constructRawGovernanceMetaData(true);
		GovernanceMetaData<?, ?> governanceMetaData = rawMetaData
				.getGovernanceMetaData();
		rawMetaData.linkOfficeMetaData(this.officeMetaDataLocator,
				this.assetManagerFactory, this.issues);
		this.verifyMockObjects();

		// Verify the content of the raw meta data
		assertEquals("Incorrect governance name", GOVERNANCE_NAME,
				rawMetaData.getGovernanceName());
		assertEquals("Incorrect extension interface type", String.class,
				rawMetaData.getExtensionInterfaceType());

		// Verify governance meta-data
		assertEquals("Incorrect governance name", GOVERNANCE_NAME,
				governanceMetaData.getGovernanceName());
		assertEquals("Incorrect activate flow meta-data", activateTaskMetaData,
				governanceMetaData.getActivateFlowMetaData()
						.getInitialTaskMetaData());
		assertEquals("Incorrect enforce flow meta-data", enforceTaskMetaData,
				governanceMetaData.getEnforceFlowMetaData()
						.getInitialTaskMetaData());
		assertEquals("Incorrect disregard flow meta-data",
				disregardTaskMetaData, governanceMetaData
						.getDisregardFlowMetaData().getInitialTaskMetaData());

		// Validate correct govern meta-data
		ActiveGovernanceManager manager = governanceMetaData
				.createActiveGovernance(null, null, null, null);
		ActiveGovernance activeGovernance = manager.getActiveGovernance();
		assertEquals("Incorrect govern flow meta-data", governTaskMetaData,
				activeGovernance.getFlowMetaData().getInitialTaskMetaData());
	}

	/**
	 * Records initialising the {@link GovernanceSource}.
	 */
	private void record_initGovernance() {

		// Record instantiating governance
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceSource(),
				this.governanceSourceInstance);
		if (this.governanceSourceInstance == null) {
			this.recordReturn(this.configuration,
					this.configuration.getGovernanceSourceClass(),
					MockGovernanceSource.class);
		}

		// Record obtaining details from configuration to init
		this.recordReturn(this.configuration,
				this.configuration.getProperties(), new SourcePropertiesImpl());
	}

	/**
	 * Records the {@link Governance} {@link Task} setup.
	 */
	@SuppressWarnings("unchecked")
	private void record_setupGovernanceTasks() {

		final String TEAM_NAME = "TEAM";
		final WorkBuilder<GovernanceWork> workBuilder = this
				.createMock(WorkBuilder.class);
		final TaskBuilder<GovernanceWork, GovernanceTaskDependency, Indexed> activateTask = this
				.createMock(TaskBuilder.class);
		final TaskBuilder<GovernanceWork, GovernanceTaskDependency, Indexed> enforceTask = this
				.createMock(TaskBuilder.class);
		final TaskBuilder<GovernanceWork, GovernanceTaskDependency, Indexed> disregardTask = this
				.createMock(TaskBuilder.class);

		// Record obtaining the team name
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);

		// Record creating governance work
		this.recordReturn(this.officeBuilder, this.officeBuilder.addWork(
				"GOVERNANCE_" + GOVERNANCE_NAME, null), workBuilder,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertEquals("Incorrect work name", expected[0],
								actual[0]);
						assertTrue("Should have governance work",
								actual[1] instanceof GovernanceWork);
						return true;
					}
				});

		// Create matcher for governance task factory
		AbstractMatcher matcher = new AbstractMatcher() {

			int index = 0;

			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				String taskName = (String) actual[0];
				TaskFactory<?, ?, ?> taskFactory = (TaskFactory<?, ?, ?>) actual[1];
				switch (this.index) {
				case 0:
					assertEquals("Incorrect task name", "ACTIVATE", taskName);
					assertTrue("Incorrect task factory",
							taskFactory instanceof ActivateGovernanceTask);
					break;
				case 1:
					assertEquals("Incorrect task name", "GOVERN", taskName);
					assertTrue("Incorrect task factory",
							taskFactory instanceof GovernGovernanceTask);
					break;
				case 2:
					assertEquals("Incorrect task name", "ENFORCE", taskName);
					assertTrue("Incorrect task factory",
							taskFactory instanceof EnforceGovernanceTask);
					break;
				case 3:
					assertEquals("Incorrect task name", "DISREGARD", taskName);
					assertTrue("Incorrect task factory",
							taskFactory instanceof DisregardGovernanceTask);
					break;
				}
				if (taskName.equals(expected[0])) {
					this.index++; // next task as found expected
				}
				return true;
			}
		};

		// Record creating governance tasks
		this.recordReturn(workBuilder, workBuilder.addTask("ACTIVATE",
				new ActivateGovernanceTask<Indexed>()), activateTask, matcher);
		activateTask.setTeam(TEAM_NAME);
		activateTask.linkParameter(GovernanceTaskDependency.GOVERNANCE_CONTROL,
				GovernanceControl.class);
		this.recordReturn(workBuilder, workBuilder.addTask("GOVERN",
				new GovernGovernanceTask<Indexed>()), activateTask, matcher);
		activateTask.setTeam(TEAM_NAME);
		activateTask.linkParameter(GovernanceTaskDependency.GOVERNANCE_CONTROL,
				ActiveGovernanceControl.class);
		this.recordReturn(workBuilder, workBuilder.addTask("ENFORCE",
				new EnforceGovernanceTask<Indexed>()), enforceTask);
		enforceTask.setTeam(TEAM_NAME);
		enforceTask.linkParameter(GovernanceTaskDependency.GOVERNANCE_CONTROL,
				GovernanceControl.class);
		this.recordReturn(workBuilder, workBuilder.addTask("DISREGARD",
				new DisregardGovernanceTask<Indexed>()), disregardTask);
		disregardTask.setTeam(TEAM_NAME);
		disregardTask.linkParameter(
				GovernanceTaskDependency.GOVERNANCE_CONTROL,
				GovernanceControl.class);
	}

	/**
	 * Records linking a {@link Governance} {@link Task}.
	 */
	private TaskMetaData<?, ?, ?> record_linkGovernanceTask(String taskName,
			boolean isFound) {

		final TaskMetaData<?, ?, ?> taskMetaData = (isFound ? this
				.createMock(TaskMetaData.class) : null);

		// Record obtaining the task
		this.recordReturn(
				this.officeMetaDataLocator,
				this.officeMetaDataLocator.getTaskMetaData("GOVERNANCE_"
						+ GOVERNANCE_NAME, taskName), taskMetaData);

		// Should always be PARALLEL flow so no asset manager required

		// Return the task meta-data
		return taskMetaData;
	}

	/**
	 * Records creating the {@link RawGovernanceMetaData} after initialising.
	 * 
	 * @param managedObjectClass
	 *            {@link ManagedObject} class.
	 * @param timeout
	 *            Timeout for the {@link ManagedObjectSource}.
	 * @param processBoundName
	 *            Process bound name for {@link ManagedObject}.
	 * @param {@link ManagedObjectFlowMetaData} for the
	 *        {@link ManagedObjectSource}.
	 */
	private <I> void record_createRawMetaData(Class<I> extensionInterfaceType) {
		this.recordReturn(this.metaData, this.metaData.getExtensionInterface(),
				extensionInterfaceType);
	}

	/**
	 * Records an issue for the {@link Governance}.
	 * 
	 * @param issueDescription
	 *            Issue description.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.GOVERNANCE, GOVERNANCE_NAME,
				issueDescription);
	}

	/**
	 * Records an issue for the {@link Governance}.
	 * 
	 * @param issueDescription
	 *            Issue description.
	 * @param cause
	 *            Cause.
	 */
	private void record_issue(String issueDescription, Throwable cause) {
		this.issues.addIssue(AssetType.GOVERNANCE, GOVERNANCE_NAME,
				issueDescription, cause);
	}

	/**
	 * Creates the {@link RawGovernanceMetaData}.
	 * 
	 * @param isCreated
	 *            Indicates if expected to create the
	 *            {@link RawGovernanceMetaData}.
	 * @return {@link RawGovernanceMetaData}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RawGovernanceMetaData constructRawGovernanceMetaData(
			boolean isCreated) {

		// Create the raw governance meta-data
		RawGovernanceMetaData rawGovernanceMetaData = RawGovernanceMetaDataImpl
				.getFactory().createRawGovernanceMetaData(
						(GovernanceConfiguration) this.configuration,
						GOVERNANCE_INDEX, this.sourceContext, OFFICE_NAME,
						this.officeBuilder, this.issues);
		if (!isCreated) {
			// Ensure not created
			assertNull("Should not create the Raw Governance Meta-Data",
					rawGovernanceMetaData);

		} else {
			// Ensure created with correct index
			assertNotNull("Raw Governance Meta-Data should be created",
					rawGovernanceMetaData);
			assertEquals("Incorrect index for Governance", GOVERNANCE_INDEX,
					rawGovernanceMetaData.getGovernanceIndex());
		}

		// Return the raw governance meta-data
		return rawGovernanceMetaData;
	}

	/**
	 * Mock {@link GovernanceSource}.
	 */
	@TestSource
	@SuppressWarnings("rawtypes")
	public static class MockGovernanceSource implements GovernanceSource {

		/**
		 * Instantiate exception.
		 */
		public static Exception instantiateFailure = null;

		/**
		 * Name of required property.
		 */
		public static String requiredPropertyName = null;

		/**
		 * Name of required {@link Class}.
		 */
		public static String requiredClassName = null;

		/**
		 * Location of required resource.
		 */
		public static String requiredResourceLocation = null;

		/**
		 * {@link ClassLoader}.
		 */
		public static ClassLoader classLoader = null;

		/**
		 * Init exception.
		 */
		public static Exception initFailure = null;

		/**
		 * {@link GovernanceMetaData}.
		 */
		public static GovernanceSourceMetaData<?, ?> metaData = null;

		/**
		 * Resets state of {@link MockGovernanceSource} for testing.
		 * 
		 * @param metaData
		 *            {@link GovernanceSourceMetaData}.
		 */
		public static void reset(GovernanceSourceMetaData<?, ?> metaData) {
			instantiateFailure = null;
			requiredPropertyName = null;
			requiredClassName = null;
			requiredResourceLocation = null;
			classLoader = null;
			initFailure = null;
			MockGovernanceSource.metaData = metaData;
		}

		/**
		 * Instantiate.
		 * 
		 * @throws Exception
		 *             Possible instantiate failure.
		 */
		public MockGovernanceSource() throws Exception {
			if (instantiateFailure != null) {
				throw instantiateFailure;
			}
		}

		/*
		 * ==================== GovernanceSource ====================
		 */

		@Override
		public GovernanceSourceSpecification getSpecification() {
			fail("Should not call getSpecification");
			return null;
		}

		@Override
		public void init(GovernanceSourceContext context) throws Exception {

			// Obtain the required property
			if (requiredPropertyName != null) {
				context.getProperty(requiredPropertyName);
			}

			// Obtain class loader
			if (classLoader != null) {
				assertSame("Incorrect class loader", classLoader,
						context.getClassLoader());
			}

			// Load the required class
			if (requiredClassName != null) {
				context.loadClass(requiredClassName);
			}

			// Obtain the required resource
			if (requiredResourceLocation != null) {
				context.getResource(requiredResourceLocation);
			}

			// Ensure can obtain defaulted property
			assertEquals("Must default property", "DEFAULT",
					context.getProperty("property to default", "DEFAULT"));

			// Determine if failure in initialising
			if (initFailure != null) {
				throw initFailure;
			}
		}

		@Override
		public GovernanceSourceMetaData<?, ?> getMetaData() {
			return metaData;
		}

		@Override
		public Governance createGovernance() throws Throwable {
			fail("Should not be required to create Governance");
			return null;
		}
	}

}