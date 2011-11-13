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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.GovernanceFactory;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.governance.ActivateGovernanceActivity;
import net.officefloor.frame.impl.execute.governance.DisregardGovernanceActivity;
import net.officefloor.frame.impl.execute.governance.EnforceGovernanceActivity;
import net.officefloor.frame.impl.execute.governance.GovernGovernanceActivity;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceEscalationConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.ActiveGovernanceManager;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

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
	 * {@link Team} name.
	 */
	private final String TEAM_NAME = "TEAM";

	/**
	 * {@link Team}. May be set to <code>null</code> to not be available.
	 */
	private Team team = this.createMock(Team.class);

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
	 * Ensure issue if no {@link Team} name.
	 */
	public void testNoTeamName() {

		// Record no team name
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceFactory(),
				this.governanceFactory);
		this.recordReturn(this.configuration,
				this.configuration.getExtensionInterface(), String.class);
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				null);
		this.record_issue("Must specify Team responsible for Governance activities");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no corresponding {@link Team}.
	 */
	public void testNoTeam() {

		// No team
		this.team = null;

		// Record no team name
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceName(), GOVERNANCE_NAME);
		this.recordReturn(this.configuration,
				this.configuration.getGovernanceFactory(),
				this.governanceFactory);
		this.recordReturn(this.configuration,
				this.configuration.getExtensionInterface(), String.class);
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
		this.record_issue("Can not find Team by name '" + TEAM_NAME + "'");

		// Attempt to construct governance
		this.replayMockObjects();
		this.constructRawGovernanceMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to handle simple {@link Governance} without
	 * {@link JobSequence}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testSimpleGovernance() {

		final GovernanceControl governanceControl = this
				.createMock(GovernanceControl.class);

		// Record simple governance
		this.record_initGovernance();
		this.record_escalations();

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
		assertTrue(
				"Incorrect activate activity",
				governanceMetaData.createActivateActivity(governanceControl) instanceof ActivateGovernanceActivity);
		assertTrue(
				"Incorrect enforce activity",
				governanceMetaData.createEnforceActivity(governanceControl) instanceof EnforceGovernanceActivity);
		assertTrue(
				"Incorrect disregard activity",
				governanceMetaData.createDisregardActivity(governanceControl) instanceof DisregardGovernanceActivity);

		// Validate correct govern meta-data
		ActiveGovernanceManager<?, ?> manager = governanceMetaData
				.createActiveGovernance(null, null, null, null, null, 0);
		ActiveGovernance<?, ?> activeGovernance = manager.getActiveGovernance();
		assertTrue(
				"Incorrect govern activity",
				activeGovernance.createGovernActivity() instanceof GovernGovernanceActivity);
	}

	/**
	 * Ensure issue if no cause type for {@link Escalation}.
	 */
	public void testNoEscalationTypeOfCause() {

		final GovernanceEscalationConfiguration escalation = this
				.createMock(GovernanceEscalationConfiguration.class);

		// Record simple governance
		this.record_initGovernance();
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new GovernanceEscalationConfiguration[] { escalation });
		this.recordReturn(escalation, escalation.getTypeOfCause(), null);
		this.record_issue("No escalation type for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this
				.constructRawGovernanceMetaData(true);
		rawMetaData.linkOfficeMetaData(this.officeMetaDataLocator,
				this.assetManagerFactory, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Task} name for {@link Escalation}.
	 */
	public void testNoEscalationTaskName() {

		final GovernanceEscalationConfiguration escalation = this
				.createMock(GovernanceEscalationConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final String WORK_NAME = "WORK";

		// Record simple governance
		this.record_initGovernance();
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new GovernanceEscalationConfiguration[] { escalation });
		this.recordReturn(escalation, escalation.getTypeOfCause(),
				SQLException.class);
		this.recordReturn(escalation, escalation.getTaskNodeReference(),
				taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				WORK_NAME);
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				null);
		this.record_issue("No task name provided for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this
				.constructRawGovernanceMetaData(true);
		rawMetaData.linkOfficeMetaData(this.officeMetaDataLocator,
				this.assetManagerFactory, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Work} name for {@link Escalation}.
	 */
	public void testNoEscalationWorkName() {

		final GovernanceEscalationConfiguration escalation = this
				.createMock(GovernanceEscalationConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record simple governance
		this.record_initGovernance();
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new GovernanceEscalationConfiguration[] { escalation });
		this.recordReturn(escalation, escalation.getTypeOfCause(),
				SQLException.class);
		this.recordReturn(escalation, escalation.getTaskNodeReference(),
				taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null);
		this.record_issue("No work name provided for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this
				.constructRawGovernanceMetaData(true);
		rawMetaData.linkOfficeMetaData(this.officeMetaDataLocator,
				this.assetManagerFactory, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Task} for {@link Escalation}.
	 */
	public void testNoEscalationTask() {

		final GovernanceEscalationConfiguration escalation = this
				.createMock(GovernanceEscalationConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final String WORK_NAME = "WORK";
		final String TASK_NAME = "TASK";

		// Record simple governance
		this.record_initGovernance();
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new GovernanceEscalationConfiguration[] { escalation });
		this.recordReturn(escalation, escalation.getTypeOfCause(),
				SQLException.class);
		this.recordReturn(escalation, escalation.getTaskNodeReference(),
				taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				WORK_NAME);
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.officeMetaDataLocator,
				this.officeMetaDataLocator
						.getTaskMetaData(WORK_NAME, TASK_NAME), null);
		this.record_issue("Can not find task meta-data (work=" + WORK_NAME
				+ ", task=" + TASK_NAME + ") for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this
				.constructRawGovernanceMetaData(true);
		rawMetaData.linkOfficeMetaData(this.officeMetaDataLocator,
				this.assetManagerFactory, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if parameter type {@link Task} is incorrect for
	 * {@link Escalation}.
	 */
	public void testEscalationTaskIncorrectParameter() {

		final GovernanceEscalationConfiguration escalation = this
				.createMock(GovernanceEscalationConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final String WORK_NAME = "WORK";
		final String TASK_NAME = "TASK";
		final TaskMetaData<?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);

		// Record simple governance
		this.record_initGovernance();
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(),
				new GovernanceEscalationConfiguration[] { escalation });
		this.recordReturn(escalation, escalation.getTypeOfCause(),
				SQLException.class);
		this.recordReturn(escalation, escalation.getTaskNodeReference(),
				taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				WORK_NAME);
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.officeMetaDataLocator,
				this.officeMetaDataLocator
						.getTaskMetaData(WORK_NAME, TASK_NAME), taskMetaData);
		this.recordReturn(taskNodeReference,
				taskNodeReference.getArgumentType(), SQLException.class);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(),
				RuntimeException.class);
		this.record_issue("Argument is not compatible with task parameter (argument="
				+ SQLException.class.getName()
				+ ", parameter="
				+ RuntimeException.class.getName()
				+ ", work="
				+ WORK_NAME
				+ ", task=" + TASK_NAME + ") for escalation index 0");

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this
				.constructRawGovernanceMetaData(true);
		rawMetaData.linkOfficeMetaData(this.officeMetaDataLocator,
				this.assetManagerFactory, this.issues);
		this.verifyMockObjects();
	}

	/**
	 * Ensure configure in {@link EscalationProcedure} for the
	 * {@link Governance}.
	 */
	public void testGovernanceEscalation() {

		final SQLException exception = new SQLException("TEST");

		// Record simple governance
		this.record_initGovernance();
		TaskMetaData<?, ?, ?> taskMetaData = this.record_escalations(exception)[0];

		// Attempt to construct governance
		this.replayMockObjects();
		RawGovernanceMetaData<?, ?> rawMetaData = this
				.constructRawGovernanceMetaData(true);
		rawMetaData.linkOfficeMetaData(this.officeMetaDataLocator,
				this.assetManagerFactory, this.issues);
		this.verifyMockObjects();

		// Verify escalation
		GovernanceMetaData<?, ?> governanceMetaData = rawMetaData
				.getGovernanceMetaData();
		EscalationProcedure escalationProcedure = governanceMetaData
				.getEscalationProcedure();
		EscalationFlow flow = escalationProcedure.getEscalation(exception);
		assertEquals("Incorrect type of cause", SQLException.class,
				flow.getTypeOfCause());
		assertEquals("Incorrect task meta-data", taskMetaData,
				flow.getTaskMetaData());

		// Ensure not handle unknown escalation
		assertNull("Should not handle unknown escalation",
				escalationProcedure.getEscalation(new RuntimeException(
						"UNKNOWN")));
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
		this.recordReturn(this.configuration, this.configuration.getTeamName(),
				TEAM_NAME);
	}

	/**
	 * Records the {@link EscalationProcedure}.
	 * 
	 * @param exceptions
	 *            {@link Throwable} instances to be handled.
	 * @return {@link TaskMetaData} for the {@link Throwable} instances.
	 */
	private TaskMetaData<?, ?, ?>[] record_escalations(Throwable... exceptions) {

		// Create the mocks for the exceptions
		GovernanceEscalationConfiguration[] escalations = new GovernanceEscalationConfiguration[exceptions.length];
		TaskMetaData<?, ?, ?>[] taskMetaDatas = new TaskMetaData<?, ?, ?>[exceptions.length];
		for (int i = 0; i < exceptions.length; i++) {
			escalations[i] = this
					.createMock(GovernanceEscalationConfiguration.class);
			taskMetaDatas[i] = this.createMock(TaskMetaData.class);
		}

		// Record obtain the escalations
		this.recordReturn(this.configuration,
				this.configuration.getEscalations(), escalations);

		// Record configuring each escalation
		for (int i = 0; i < exceptions.length; i++) {
			Throwable exception = exceptions[i];
			GovernanceEscalationConfiguration escalation = escalations[i];
			TaskMetaData<?, ?, ?> taskMetaData = taskMetaDatas[i];

			final TaskNodeReference taskNodeReference = this
					.createMock(TaskNodeReference.class);
			final String WORK_NAME = "WORK" + i;
			final String TASK_NAME = "TASK" + i;

			// Record configuring the escalation
			this.recordReturn(escalation, escalation.getTypeOfCause(),
					exception.getClass());
			this.recordReturn(escalation, escalation.getTaskNodeReference(),
					taskNodeReference);
			this.recordReturn(taskNodeReference,
					taskNodeReference.getWorkName(), WORK_NAME);
			this.recordReturn(taskNodeReference,
					taskNodeReference.getTaskName(), TASK_NAME);
			this.recordReturn(this.officeMetaDataLocator,
					this.officeMetaDataLocator.getTaskMetaData(WORK_NAME,
							TASK_NAME), taskMetaData);
			this.recordReturn(taskNodeReference,
					taskNodeReference.getArgumentType(), exception.getClass());
			this.recordReturn(taskMetaData, taskMetaData.getParameterType(),
					exception.getClass());
		}

		// Return the task meta-data
		return taskMetaDatas;
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

		// Create the map of office teams
		Map<String, Team> officeTeams = new HashMap<String, Team>();
		if (this.team != null) {
			officeTeams.put(TEAM_NAME, this.team);
		}

		// Create the raw governance meta-data
		RawGovernanceMetaData rawGovernanceMetaData = RawGovernanceMetaDataImpl
				.getFactory().createRawGovernanceMetaData(
						(GovernanceConfiguration) this.configuration,
						GOVERNANCE_INDEX, this.sourceContext, officeTeams,
						OFFICE_NAME, this.officeBuilder, this.issues);
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