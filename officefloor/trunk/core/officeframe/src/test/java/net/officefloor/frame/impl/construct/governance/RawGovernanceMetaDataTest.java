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

import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
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
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.source.SourceContext;
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
	private final GovernanceConfiguration<?, ?> configuration = this
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
	 * {@link GovernanceFactory}.
	 */
	private final GovernanceFactory<?, ?> governanceFactory = this
			.createMock(GovernanceFactory.class);

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
	 * Ensures issue if no {@link GovernanceFactory}.
	 */
	public void testNoGovernanceFactory() {

		// Record no class
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceFactory(), null);
		this.record_issue("No GovernanceFactory provided");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no extension interface.
	 */
	public void testNoExtensionInterface() {

		// Record no class
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceFactory(),
				this.governanceFactory);
		this.recordReturn(this.configuration,
				this.configuration.getExtensionInterface(), null);
		this.record_issue("No extension interface type provided");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to handle simple {@link Governance} without {@link JobSequence}.
	 */
	public void testSimpleGovernance() {

		// Record simple governance
		this.record_initGovernance();
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
		RawGovernanceMetaData<?, ?> rawMetaData = this
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
				.createActiveGovernance(null, null, null, null, 0);
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
				this.configuration.getGovernanceFactory(),
				this.governanceFactory);
		this.recordReturn(this.configuration,
				this.configuration.getExtensionInterface(), String.class);
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

}