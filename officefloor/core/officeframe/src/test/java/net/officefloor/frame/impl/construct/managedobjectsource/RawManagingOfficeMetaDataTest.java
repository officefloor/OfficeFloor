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
package net.officefloor.frame.impl.construct.managedobjectsource;

import java.util.Map;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.impl.execute.officefloor.ManagedObjectExecuteContextImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagingOfficeMetaData;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.CleanupSequence;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ProcessTicker;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

import org.easymock.AbstractMatcher;
import org.easymock.internal.AlwaysMatcher;

/**
 * Tests the {@link RawManagingOfficeMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawManagingOfficeMetaDataTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link ManagedObjectSource}.
	 */
	private static final String MANAGED_OBJECT_SOURCE_NAME = "MANAGED_OBJECT_SOURCE";

	/**
	 * Name of the managing {@link Office}.
	 */
	private static final String MANAGING_OFFICE_NAME = "MANAGING_OFFICE";

	/**
	 * Input name of the {@link ManagedObject}.
	 */
	private static final String INPUT_MANAGED_OBJECT_NAME = "INPUT_MANAGED_OBJECT_NAME";

	/**
	 * {@link RawManagedObjectMetaData}.
	 */
	@SuppressWarnings("rawtypes")
	private final RawManagedObjectMetaData rawMoMetaData = this
			.createMock(RawManagedObjectMetaData.class);

	/**
	 * {@link ManagedObjectMetaData}.
	 */
	final ManagedObjectMetaData<?> moMetaData = this
			.createMock(ManagedObjectMetaData.class);

	/**
	 * {@link ManagingOfficeConfiguration}.
	 */
	private final ManagingOfficeConfiguration<?> configuration = this
			.createMock(ManagingOfficeConfiguration.class);

	/**
	 * {@link InputManagedObjectConfiguration}.
	 */
	private final InputManagedObjectConfiguration<?> inputConfiguration = this
			.createMock(InputManagedObjectConfiguration.class);

	/**
	 * {@link OfficeMetaDataLocator}.
	 */
	private final OfficeMetaDataLocator metaDataLocator = this
			.createMock(OfficeMetaDataLocator.class);

	/**
	 * {@link Office} {@link TeamManagement} instances.
	 */
	@SuppressWarnings("unchecked")
	private final Map<String, TeamManagement> officeTeams = this
			.createMock(Map.class);

	/**
	 * Continue {@link TeamManagement}.
	 */
	private final TeamManagement continueTeamManagement = this
			.createMock(TeamManagement.class);

	/**
	 * Continue {@link Team}.
	 */
	private final Team continueTeam = this.createMock(Team.class);

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData = this
			.createMock(OfficeMetaData.class);

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
	 * Ensure issue if no {@link WorkMetaData} for recycle {@link Task}.
	 */
	public void testNoWorkForRecycleTask() {

		final String RECYCLE_WORK_NAME = "RECYCLE_WORK";

		// Record no work for recycle task
		this.record_managedObjectSourceName();
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(this.continueTeamManagement,
				this.continueTeamManagement.getTeam(), continueTeam);
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getWorkMetaData("RECYCLE_WORK"), null);
		this.record_issue("Recycle work 'RECYCLE_WORK' not found");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, RECYCLE_WORK_NAME, null);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no initial {@link FlowMetaData} for recycle {@link Task}.
	 */
	public void testNoInitialFlowForRecycleTask() {

		final String RECYCLE_WORK_NAME = "RECYCLE_WORK";

		final WorkMetaData<?> workMetaData = this
				.createMock(WorkMetaData.class);

		// Record no initial task
		this.record_managedObjectSourceName();
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(this.continueTeamManagement,
				this.continueTeamManagement.getTeam(), continueTeam);
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getWorkMetaData("RECYCLE_WORK"),
				workMetaData);
		this.recordReturn(workMetaData, workMetaData.getInitialFlowMetaData(),
				null);
		this.record_issue("No initial flow on work RECYCLE_WORK for recycle task");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, RECYCLE_WORK_NAME, null);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if incompatible parameter type for recycle {@link Task}.
	 */
	public void testIncompatibleRecycleTask() {

		final String RECYCLE_WORK_NAME = "RECYCLE_WORK";

		final WorkMetaData<?> workMetaData = this
				.createMock(WorkMetaData.class);
		final FlowMetaData<?> flowMetaData = this
				.createMock(FlowMetaData.class);
		final TaskMetaData<?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);

		// Record recycle task has incompatible parameter
		this.record_managedObjectSourceName();
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(this.continueTeamManagement,
				this.continueTeamManagement.getTeam(), continueTeam);
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getWorkMetaData("RECYCLE_WORK"),
				workMetaData);
		this.recordReturn(workMetaData, workMetaData.getInitialFlowMetaData(),
				flowMetaData);
		this.recordReturn(flowMetaData, flowMetaData.getInitialTaskMetaData(),
				taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(),
				Integer.class);
		this.recordReturn(taskMetaData, taskMetaData.getTaskName(), "TASK");
		this.record_issue("Incompatible parameter type for recycle task (parameter="
				+ Integer.class.getName()
				+ ", required type="
				+ RecycleManagedObjectParameter.class.getName()
				+ ", work=RECYCLE_WORK, task=TASK)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, RECYCLE_WORK_NAME, null);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link the recycle {@link Task} before managing.
	 */
	public void testLinkRecycleTaskBeforeManaging() {

		final String RECYCLE_WORK_NAME = "RECYCLE_WORK";

		final WorkMetaData<?> workMetaData = this
				.createMock(WorkMetaData.class);
		final FlowMetaData<?> flowMetaData = this
				.createMock(FlowMetaData.class);
		final TaskMetaData<?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);
		final ManagedObjectMetaDataImpl<?> moMetaData = this.createMoMetaData();
		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);
		final CleanupSequence cleanupSequence = this
				.createMock(CleanupSequence.class);

		// Record manage office
		this.record_managedObjectSourceName();
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(this.continueTeamManagement,
				this.continueTeamManagement.getTeam(), continueTeam);
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getWorkMetaData("RECYCLE_WORK"),
				workMetaData);
		this.recordReturn(workMetaData, workMetaData.getInitialFlowMetaData(),
				flowMetaData);
		this.recordReturn(flowMetaData, flowMetaData.getInitialTaskMetaData(),
				taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(),
				RecycleManagedObjectParameter.class);
		this.record_getFlowConfigurations();

		// Record instigating the recycle flow
		final JobNode recycleJobNode = this.record_createRecycleJobNode(
				flowMetaData, managedObject);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaDataImpl<?> rawOffice = this
				.createRawManagingOffice(RECYCLE_WORK_NAME);

		// Have managed before managed by office.
		// This would be the possible case that used by same office.
		rawOffice.manageManagedObject(moMetaData);

		rawOffice.manageByOffice(null, this.metaDataLocator, this.officeTeams,
				this.continueTeamManagement, this.assetManagerFactory,
				this.issues);
		JobNode jobNode = moMetaData.createRecycleJobNode(managedObject,
				cleanupSequence);
		this.verifyMockObjects();

		// Ensure correct recycle job
		assertEquals("Incorrect recycle job", recycleJobNode, jobNode);
	}

	/**
	 * Ensure able to link the recycle {@link Task} after managing.
	 */
	public void testLinkRecycleTaskAfterManaging() {

		final String RECYCLE_WORK_NAME = "RECYCLE_WORK";

		final WorkMetaData<?> workMetaData = this
				.createMock(WorkMetaData.class);
		final FlowMetaData<?> flowMetaData = this
				.createMock(FlowMetaData.class);
		final TaskMetaData<?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);
		final ManagedObjectMetaDataImpl<?> moMetaData = this.createMoMetaData();
		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);
		final CleanupSequence cleanupSequence = this
				.createMock(CleanupSequence.class);

		// Record managed office
		this.record_managedObjectSourceName();
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(this.continueTeamManagement,
				this.continueTeamManagement.getTeam(), continueTeam);
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getWorkMetaData("RECYCLE_WORK"),
				workMetaData);
		this.recordReturn(workMetaData, workMetaData.getInitialFlowMetaData(),
				flowMetaData);
		this.recordReturn(flowMetaData, flowMetaData.getInitialTaskMetaData(),
				taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), null);
		this.record_getFlowConfigurations();

		// Record instigating the recycle flow
		final JobNode recycleJobNode = this.record_createRecycleJobNode(
				flowMetaData, managedObject);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaDataImpl<?> rawOffice = this
				.createRawManagingOffice(RECYCLE_WORK_NAME);
		rawOffice.manageByOffice(null, this.metaDataLocator, this.officeTeams,
				this.continueTeamManagement, this.assetManagerFactory,
				this.issues);

		// Have managed after managed by office.
		// This would the be case when used by another office.
		rawOffice.manageManagedObject(moMetaData);
		JobNode jobNode = moMetaData.createRecycleJobNode(managedObject,
				cleanupSequence);
		this.verifyMockObjects();

		// Ensure correct recycle job
		assertEquals("Incorrect recycle job", recycleJobNode, jobNode);
	}

	/**
	 * Ensure able to not have a recycle {@link Task}.
	 */
	public void testNoRecycleTask() {

		final ManagedObjectMetaDataImpl<?> moMetaData = this.createMoMetaData();
		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);
		final CleanupSequence cleanupSequence = this
				.createMock(CleanupSequence.class);

		// Record no recycle task
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.record_getFlowConfigurations();

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaDataImpl<?> rawOffice = this.run_manageByOffice(
				true, null, null);

		// Ensure not obtain recycle job
		rawOffice.manageManagedObject(moMetaData);
		JobNode jobNode = moMetaData.createRecycleJobNode(managedObject,
				cleanupSequence);
		this.verifyMockObjects();

		// Ensure no recycle job
		assertNull("Should be no recycle job", jobNode);
	}

	/**
	 * Ensures issues if no {@link ProcessState} bound name for
	 * {@link ManagedObject}.
	 */
	public void testNoBoundInputManagedObjectName() {

		final ManagedObjectFlowMetaData<?> moFlowMetaData = this
				.createMock(ManagedObjectFlowMetaData.class);

		// Record not managed by office
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedObjectFlowConfiguration[0]);
		this.recordReturn(this.inputConfiguration,
				this.inputConfiguration.getBoundManagedObjectName(), null);
		this.record_issue("ManagedObjectSource invokes flows but does not provide input Managed Object binding name");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, null, moFlowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issues if {@link ManagedObject} is not managed by the
	 * {@link Office}.
	 */
	public void testNotManagedByOffice() {

		final ManagedObjectFlowMetaData<?> moFlowMetaData = this
				.createMock(ManagedObjectFlowMetaData.class);
		final RawBoundManagedObjectMetaData boundMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);

		// Record not managed by office
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedObjectFlowConfiguration[0]);
		this.recordReturn(this.inputConfiguration,
				this.inputConfiguration.getBoundManagedObjectName(),
				INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(boundMetaData,
				boundMetaData.getBoundManagedObjectName(), "NOT_MATCH");
		this.recordReturn(this.officeMetaData,
				this.officeMetaData.getOfficeName(), MANAGING_OFFICE_NAME);
		this.record_issue("ManagedObjectSource by input name '"
				+ INPUT_MANAGED_OBJECT_NAME + "' not managed by Office "
				+ MANAGING_OFFICE_NAME);

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null,
				new RawBoundManagedObjectMetaData[] { boundMetaData },
				moFlowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issues if no {@link RawBoundManagedObjectInstanceMetaData} for
	 * the {@link ManagedObjectSource}.
	 */
	public void testNoInstanceForManagedObjectSource() {

		final ManagedObjectFlowMetaData<?> moFlowMetaData = this
				.createMock(ManagedObjectFlowMetaData.class);
		final RawBoundManagedObjectMetaData boundMetaData = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = this
				.createMock(RawBoundManagedObjectInstanceMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMoMetaData = this
				.createMock(RawManagedObjectMetaData.class);

		// Record not managed by office
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedObjectFlowConfiguration[0]);
		this.recordReturn(this.inputConfiguration,
				this.inputConfiguration.getBoundManagedObjectName(),
				INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(boundMetaData,
				boundMetaData.getBoundManagedObjectName(),
				INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(
				boundMetaData,
				boundMetaData.getRawBoundManagedObjectInstanceMetaData(),
				new RawBoundManagedObjectInstanceMetaData[] { instanceMetaData });
		this.recordReturn(instanceMetaData,
				instanceMetaData.getRawManagedObjectMetaData(), rawMoMetaData);
		this.recordReturn(rawMoMetaData, rawMoMetaData.getManagedObjectName(),
				"NOT_MATCH");
		this.recordReturn(this.officeMetaData,
				this.officeMetaData.getOfficeName(), MANAGING_OFFICE_NAME);
		this.record_issue("ManagedObjectSource by input name '"
				+ INPUT_MANAGED_OBJECT_NAME + "' not managed by Office "
				+ MANAGING_OFFICE_NAME);

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null,
				new RawBoundManagedObjectMetaData[] { boundMetaData },
				moFlowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issues if {@link Flow} instances configured but no
	 * {@link Flow} instances required.
	 */
	public void testNoFlowsButFlowsConfigured() {

		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);

		// Record flows configured but none required
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(),
				new ManagedObjectFlowConfiguration[] { flowConfiguration });
		this.record_issue("ManagedObjectSourceMetaData specifies no flows but flows configured for it");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, null);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link Flow} is configured.
	 */
	public void testNoFlowConfigured() {

		final ManagedObjectFlowMetaData<?> flowMetaData = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);

		// Record no flow configured
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.record_getFlowConfigurations(flowConfiguration);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this
				.record_bindToProcess(0, INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(),
				Flows.WRONG_KEY);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), Flows.KEY);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
		this.record_issue("No flow configured for flow 0 (key=" + Flows.KEY
				+ ", label=<no label>)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, processBoundMetaData, flowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if no {@link TaskMetaData} for the {@link Flow}.
	 */
	public void testNoFlowTask() {

		final ManagedObjectFlowMetaData<?> flowMetaData = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);
		final TaskNodeReference taskReference = this
				.createMock(TaskNodeReference.class);

		// Record no flow task
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.record_getFlowConfigurations(flowConfiguration);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this
				.record_bindToProcess(0, INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(),
				Flows.KEY);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), Flows.KEY);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), "FLOW");
		this.recordReturn(flowConfiguration,
				flowConfiguration.getTaskNodeReference(), taskReference);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), null);
		this.recordReturn(taskReference, taskReference.getWorkName(), "WORK");
		this.recordReturn(taskReference, taskReference.getTaskName(), "TASK");
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getTaskMetaData("WORK", "TASK"), null);
		this.record_issue("Can not find task meta-data (work=WORK, task=TASK) for flow 0 (key="
				+ Flows.KEY + ", label=FLOW)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, processBoundMetaData, flowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if incompatible argument to {@link flow} {@link Task}.
	 */
	public void testIncompatibleFlowArgument() {

		final ManagedObjectFlowMetaData<?> flowMetaData = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);
		final TaskNodeReference taskReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);

		// Record incompatible argument
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.record_getFlowConfigurations(flowConfiguration);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this
				.record_bindToProcess(0, INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(),
				null);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), null);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), "");
		this.recordReturn(flowConfiguration,
				flowConfiguration.getTaskNodeReference(), taskReference);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(),
				Integer.class);
		this.recordReturn(taskReference, taskReference.getWorkName(), "WORK");
		this.recordReturn(taskReference, taskReference.getTaskName(), "TASK");
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getTaskMetaData("WORK", "TASK"),
				taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(),
				String.class);
		this.record_issue("Argument is not compatible with task parameter (argument="
				+ Integer.class.getName()
				+ ", parameter="
				+ String.class.getName()
				+ ", work=WORK, task=TASK) for flow 0 (key=<indexed>, label=<no label>)");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, processBoundMetaData, flowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures issue if extra {@link Flow} configured.
	 */
	public void testExtraFlowConfigured() {

		final ManagedObjectFlowMetaData<?> flowMetaData = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfigurationOne = this
				.createMock(ManagedObjectFlowConfiguration.class);
		final TaskNodeReference taskReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);
		final AssetManager assetManager = this.createMock(AssetManager.class);
		final ManagedObjectFlowConfiguration<?> flowConfigurationTwo = this
				.createMock(ManagedObjectFlowConfiguration.class);

		// Record extra flow configured
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.record_getFlowConfigurations(flowConfigurationOne,
				flowConfigurationTwo);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this
				.record_bindToProcess(0, INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfigurationOne,
				flowConfigurationOne.getFlowKey(), Flows.KEY);
		this.recordReturn(flowConfigurationTwo,
				flowConfigurationTwo.getFlowKey(), Flows.WRONG_KEY);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), Flows.KEY);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), "LABEL");
		this.recordReturn(flowConfigurationOne,
				flowConfigurationOne.getTaskNodeReference(), taskReference);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(), null);
		this.recordReturn(taskReference, taskReference.getWorkName(), "WORK");
		this.recordReturn(taskReference, taskReference.getTaskName(), "TASK");
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getTaskMetaData("WORK", "TASK"),
				taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(), null);
		this.recordReturn(this.assetManagerFactory, this.assetManagerFactory
				.createAssetManager(AssetType.MANAGED_OBJECT,
						MANAGED_OBJECT_SOURCE_NAME, "flow 0 (key=" + Flows.KEY
								+ ", label=LABEL)", this.issues), assetManager);
		this.record_issue("Extra flows configured than specified by ManagedObjectSourceMetaData");

		// Manage by office
		this.replayMockObjects();
		this.run_manageByOffice(false, null, processBoundMetaData, flowMetaData);
		this.verifyMockObjects();
	}

	/**
	 * Ensures able to construct {@link Flow} for a {@link ManagedObject}
	 * .
	 */
	public void testConstructFlow() {

		final ManagedObjectFlowMetaData<?> flowMetaData = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);
		final TaskNodeReference taskReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);
		final AssetManager assetManager = this.createMock(AssetManager.class);
		final String parameter = "PARAMETER";
		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);
		final JobNode jobNode = this.createMock(JobNode.class);
		final Flow jobSequence = this.createMock(Flow.class);
		final ThreadState thread = this.createMock(ThreadState.class);
		final ProcessState process = this.createMock(ProcessState.class);
		final ProcessFuture future = this.createMock(ProcessFuture.class);

		// TODO test configuring the escalation responsible team
		final TeamManagement escalationResponsibleTeam = this.continueTeamManagement;

		// Record construct flow
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.record_getFlowConfigurations(flowConfiguration);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this
				.record_bindToProcess(0, INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(),
				null);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), null);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
		this.recordReturn(flowConfiguration,
				flowConfiguration.getTaskNodeReference(), taskReference);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(),
				String.class);
		this.recordReturn(taskReference, taskReference.getWorkName(), "WORK");
		this.recordReturn(taskReference, taskReference.getTaskName(), "TASK");
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getTaskMetaData("WORK", "TASK"),
				taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(),
				Object.class);
		this.recordReturn(this.assetManagerFactory,
				this.assetManagerFactory
						.createAssetManager(AssetType.MANAGED_OBJECT,
								MANAGED_OBJECT_SOURCE_NAME,
								"flow 0 (key=<indexed>, label=<no label>)",
								this.issues), assetManager);

		// Record invoking the flow
		this.recordReturn(this.officeMetaData, this.officeMetaData
				.createProcess(null, parameter, null,
						escalationResponsibleTeam, this.continueTeam,
						managedObject, this.moMetaData, 0), jobNode,
				new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						FlowMetaData<?> flowMetaData = (FlowMetaData<?>) actual[0];
						assertEquals("Incorrect parameter", parameter,
								actual[1]);
						assertNull("Should not have escalation handler",
								actual[2]);
						assertEquals("Incorrect escalation responsible team",
								escalationResponsibleTeam, actual[3]);
						assertEquals(
								"Incorrect escalation continue team",
								RawManagingOfficeMetaDataTest.this.continueTeam,
								actual[4]);
						assertEquals("Incorrect managed object", managedObject,
								actual[5]);
						assertEquals("Incorrect managed object meta-data",
								RawManagingOfficeMetaDataTest.this.moMetaData,
								actual[6]);
						assertEquals("Incorrect process index", 0, actual[7]);

						// Validate flow meta-data
						assertEquals("Incorrect task meta-data", taskMetaData,
								flowMetaData.getInitialTaskMetaData());
						assertEquals("Always instigated asynchronously",
								FlowInstigationStrategyEnum.ASYNCHRONOUS,
								flowMetaData.getInstigationStrategy());
						assertEquals("Incorrect asset manager", assetManager,
								flowMetaData.getFlowManager());

						// Matches if at this point
						return true;
					}
				});

		// Record obtaining the process state
		this.recordReturn(jobNode, jobNode.getJobSequence(), jobSequence);
		this.recordReturn(jobSequence, jobSequence.getThreadState(), thread);
		this.recordReturn(thread, thread.getProcessState(), process);

		// Record activating job (without notifying process ticker)
		jobNode.activateJob(ManagedObjectExecuteContextImpl.INVOKE_PROCESS_TEAM);

		// Record providing the process future
		this.recordReturn(process, process.getProcessFuture(), future);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<?> rawOffice = this.run_manageByOffice(true,
				null, processBoundMetaData, flowMetaData);
		ProcessFuture actualFuture = rawOffice
				.getManagedObjectExecuteContextFactory()
				.createManagedObjectExecuteContext(null, null)
				.invokeProcess(0, parameter, managedObject, 0);
		this.verifyMockObjects();

		// Ensure correct future
		assertSame("Incorrect process future", future, actualFuture);
	}

	/**
	 * Ensures able to construct {@link Flow} for a {@link ManagedObject}
	 * that is not the first bound or first instance.
	 */
	public void testConstructFlowOfNotFirstBoundOrInstance() {

		final int processMoIndex = 2;
		final int instanceIndex = 3;

		final ManagedObjectFlowMetaData<?> flowMetaData = this
				.createMock(ManagedObjectFlowMetaData.class);
		final ManagedObjectFlowConfiguration<?> flowConfiguration = this
				.createMock(ManagedObjectFlowConfiguration.class);
		final TaskNodeReference taskReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?> taskMetaData = this
				.createMock(TaskMetaData.class);
		final AssetManager assetManager = this.createMock(AssetManager.class);
		final String parameter = "PARAMETER";
		final ManagedObject managedObject = this
				.createMock(ManagedObject.class);
		final JobNode jobNode = this.createMock(JobNode.class);
		final ProcessTicker processTicker = this
				.createMock(ProcessTicker.class);
		final Flow jobSequence = this.createMock(Flow.class);
		final ThreadState thread = this.createMock(ThreadState.class);
		final ProcessState process = this.createMock(ProcessState.class);
		final ProcessFuture future = this.createMock(ProcessFuture.class);

		// TODO test configuring the escalation responsible team
		final TeamManagement escalationResponsibleTeam = this.continueTeamManagement;

		// Record construct flow
		this.record_managedObjectSourceName();
		this.record_noRecycleTask();
		this.record_getFlowConfigurations(flowConfiguration);
		RawBoundManagedObjectMetaData[] processBoundMetaData = this
				.record_bindToProcess(instanceIndex, "NOT_MATCH_INDEX_0",
						"NOT_MATCH_INDEX_1", INPUT_MANAGED_OBJECT_NAME);
		this.recordReturn(flowConfiguration, flowConfiguration.getFlowKey(),
				null);
		this.recordReturn(flowMetaData, flowMetaData.getKey(), null);
		this.recordReturn(flowMetaData, flowMetaData.getLabel(), null);
		this.recordReturn(flowConfiguration,
				flowConfiguration.getTaskNodeReference(), taskReference);
		this.recordReturn(flowMetaData, flowMetaData.getArgumentType(),
				String.class);
		this.recordReturn(taskReference, taskReference.getWorkName(), "WORK");
		this.recordReturn(taskReference, taskReference.getTaskName(), "TASK");
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getTaskMetaData("WORK", "TASK"),
				taskMetaData);
		this.recordReturn(taskMetaData, taskMetaData.getParameterType(),
				Object.class);
		this.recordReturn(this.assetManagerFactory,
				this.assetManagerFactory
						.createAssetManager(AssetType.MANAGED_OBJECT,
								MANAGED_OBJECT_SOURCE_NAME,
								"flow 0 (key=<indexed>, label=<no label>)",
								this.issues), assetManager);

		// Record invoking the flow
		this.recordReturn(this.officeMetaData, this.officeMetaData
				.createProcess(null, parameter, null,
						escalationResponsibleTeam, this.continueTeam,
						managedObject, this.moMetaData, processMoIndex),
				jobNode, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						FlowMetaData<?> flowMetaData = (FlowMetaData<?>) actual[0];
						assertEquals("Incorrect parameter", parameter,
								actual[1]);
						assertNull("Should not have escalation handler",
								actual[2]);
						assertEquals("Incorrect escalation responsible team",
								escalationResponsibleTeam, actual[3]);
						assertEquals(
								"Incorrect escalation continue team",
								RawManagingOfficeMetaDataTest.this.continueTeam,
								actual[4]);
						assertEquals("Incorrect managed object", managedObject,
								actual[5]);
						assertEquals("Incorrect managed object meta-data",
								RawManagingOfficeMetaDataTest.this.moMetaData,
								actual[6]);
						assertEquals("Incorrect process index", processMoIndex,
								actual[7]);

						// Validate flow meta-data
						assertEquals("Incorrect task meta-data", taskMetaData,
								flowMetaData.getInitialTaskMetaData());
						assertEquals("Always instigated asynchronously",
								FlowInstigationStrategyEnum.ASYNCHRONOUS,
								flowMetaData.getInstigationStrategy());
						assertEquals("Incorrect asset manager", assetManager,
								flowMetaData.getFlowManager());

						// Matches if at this point
						return true;
					}
				});

		// Record obtaining the process state
		this.recordReturn(jobNode, jobNode.getJobSequence(), jobSequence);
		this.recordReturn(jobSequence, jobSequence.getThreadState(), thread);
		this.recordReturn(thread, thread.getProcessState(), process);

		// Record activating job (and subsequently process)
		processTicker.processStarted();
		process.registerProcessCompletionListener(processTicker);
		jobNode.activateJob(ManagedObjectExecuteContextImpl.INVOKE_PROCESS_TEAM);

		// Record providing the process future
		this.recordReturn(process, process.getProcessFuture(), future);

		// Manage by office
		this.replayMockObjects();
		RawManagingOfficeMetaData<?> rawOffice = this.run_manageByOffice(true,
				null, processBoundMetaData, flowMetaData);
		ProcessFuture actualFuture = rawOffice
				.getManagedObjectExecuteContextFactory()
				.createManagedObjectExecuteContext(processTicker, null)
				.invokeProcess(0, parameter, managedObject, 0);
		this.verifyMockObjects();

		// Ensure correct future
		assertSame("Incorrect process future", future, actualFuture);
	}

	/**
	 * {@link Flow} key.
	 */
	private enum Flows {
		KEY, WRONG_KEY
	}

	/**
	 * Records obtaining the {@link ManagedObjectSource} name.
	 */
	private void record_managedObjectSourceName() {
		this.recordReturn(this.rawMoMetaData,
				this.rawMoMetaData.getManagedObjectName(),
				MANAGED_OBJECT_SOURCE_NAME);
	}

	/**
	 * Records no recycle {@link Task}.
	 */
	private void record_noRecycleTask() {
		this.recordReturn(this.metaDataLocator,
				this.metaDataLocator.getOfficeMetaData(), this.officeMetaData);
		this.recordReturn(this.continueTeamManagement,
				this.continueTeamManagement.getTeam(), continueTeam);
	}

	/**
	 * Records creating the recycle {@link JobNode}.
	 * 
	 * @param recycleFlowMetaData
	 *            {@link FlowMetaData} for the recycle {@link JobNode}.
	 * @param managedObject
	 *            {@link ManagedObject} to be recycled.
	 * @return Recycle {@link JobNode} created.
	 */
	private JobNode record_createRecycleJobNode(
			final FlowMetaData<?> recycleFlowMetaData,
			final ManagedObject managedObject) {

		final JobNode recycleJob = this.createMock(JobNode.class);
		final Flow jobSequence = this.createMock(Flow.class);
		final ThreadState threadState = this.createMock(ThreadState.class);
		final ProcessState processState = this.createMock(ProcessState.class);

		// Record creating the job node
		this.recordReturn(this.officeMetaData, this.officeMetaData
				.createProcess(recycleFlowMetaData, null, null, null, null),
				recycleJob, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {

						// Ensure correct flow meta-data
						assertEquals("Incorrect recycle flow meta-data",
								recycleFlowMetaData, actual[0]);

						// Ensure correct recycle parameter
						RecycleManagedObjectParameter<?> parameter = (RecycleManagedObjectParameter<?>) actual[1];
						assertEquals("Incorrect managed object", managedObject,
								parameter.getManagedObject());

						// Ensure have recycle escalation handling
						assertNotNull("Should have recycle escalation handler",
								actual[2]);
						assertNotNull(
								"Should have recycle escalation responsible team",
								actual[3]);
						assertNotNull(
								"Should have recycle escalation continue team",
								actual[4]);
						return true;
					}
				});

		// Record adding process listener
		this.recordReturn(recycleJob, recycleJob.getJobSequence(), jobSequence);
		this.recordReturn(jobSequence, jobSequence.getThreadState(),
				threadState);
		this.recordReturn(threadState, threadState.getProcessState(),
				processState);
		processState.registerProcessCompletionListener(null);
		this.control(processState).setMatcher(new AlwaysMatcher());

		// Return the recycle job node
		return recycleJob;
	}

	/**
	 * Records obtaining the {@link ManagedObjectFlowConfiguration} instances.
	 * 
	 * @param flowConfigurations
	 *            {@link ManagedObjectFlowConfiguration} instances.
	 */
	private void record_getFlowConfigurations(
			ManagedObjectFlowConfiguration<?>... flowConfigurations) {
		// Record obtaining the flow configuration
		this.recordReturn(this.configuration,
				this.configuration.getFlowConfiguration(), flowConfigurations);
	}

	/**
	 * Records obtaining the {@link ProcessState} bound index for the
	 * {@link ManagedObject}.
	 * 
	 * @param flowConfigurations
	 *            {@link ManagedObjectFlowConfiguration} instances.
	 */
	private RawBoundManagedObjectMetaData[] record_bindToProcess(
			int instanceIndex, String... processBoundNames) {

		RawBoundManagedObjectMetaData[] processBoundMetaDatas = new RawBoundManagedObjectMetaData[processBoundNames.length];
		for (int i = 0; i < processBoundMetaDatas.length; i++) {
			processBoundMetaDatas[i] = this
					.createMock(RawBoundManagedObjectMetaData.class);
		}

		RawBoundManagedObjectInstanceMetaData<?>[] instanceMetaDatas = new RawBoundManagedObjectInstanceMetaData[instanceIndex + 1];
		RawManagedObjectMetaData<?, ?>[] rawMoMetaDatas = new RawManagedObjectMetaData[instanceMetaDatas.length];
		for (int i = 0; i < instanceMetaDatas.length; i++) {
			instanceMetaDatas[i] = this
					.createMock(RawBoundManagedObjectInstanceMetaData.class);
			rawMoMetaDatas[i] = this.createMock(RawManagedObjectMetaData.class);
		}

		// Record obtaining the process index and meta-data
		this.recordReturn(this.inputConfiguration,
				this.inputConfiguration.getBoundManagedObjectName(),
				INPUT_MANAGED_OBJECT_NAME);
		for (int b = 0; b < processBoundNames.length; b++) {
			String processBoundName = processBoundNames[b];
			RawBoundManagedObjectMetaData processBoundMetaData = processBoundMetaDatas[b];
			this.recordReturn(processBoundMetaData,
					processBoundMetaData.getBoundManagedObjectName(),
					processBoundName);
			if (INPUT_MANAGED_OBJECT_NAME.equals(processBoundName)) {
				this.recordReturn(processBoundMetaData, processBoundMetaData
						.getRawBoundManagedObjectInstanceMetaData(),
						instanceMetaDatas);
				for (int i = 0; i < instanceMetaDatas.length; i++) {
					RawBoundManagedObjectInstanceMetaData<?> instanceMetaData = instanceMetaDatas[i];
					RawManagedObjectMetaData<?, ?> rawMoMetaData = rawMoMetaDatas[i];
					this.recordReturn(instanceMetaData,
							instanceMetaData.getRawManagedObjectMetaData(),
							rawMoMetaData);
					if (i != instanceIndex) {
						this.recordReturn(rawMoMetaData,
								rawMoMetaData.getManagedObjectName(),
								"NOT_MATCH");
					} else {
						this.recordReturn(rawMoMetaData,
								rawMoMetaData.getManagedObjectName(),
								MANAGED_OBJECT_SOURCE_NAME);
						this.recordReturn(instanceMetaData,
								instanceMetaData.getManagedObjectMetaData(),
								this.moMetaData);
					}
				}
			}
		}

		// Return the process bound meta-data
		return processBoundMetaDatas;
	}

	/**
	 * Records an issue.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void record_issue(String issueDescription) {
		this.issues.addIssue(AssetType.MANAGED_OBJECT,
				MANAGED_OBJECT_SOURCE_NAME, issueDescription);
	}

	/**
	 * Creates the {@link RawManagingOfficeMetaDataImpl} for testing.
	 * 
	 * @param recycleWorkName
	 *            Recycle {@link Work} name.
	 * @param flowMetaData
	 *            {@link ManagedObjectFlowMetaData} listing.
	 * @return New {@link RawManagingOfficeMetaDataImpl}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private RawManagingOfficeMetaDataImpl<?> createRawManagingOffice(
			String recycleWorkName,
			ManagedObjectFlowMetaData<?>... flowMetaData) {
		// Create and return the raw managing office meta-data
		RawManagingOfficeMetaDataImpl<?> rawManagingOffice = new RawManagingOfficeMetaDataImpl(
				MANAGING_OFFICE_NAME, recycleWorkName, this.inputConfiguration,
				flowMetaData, this.configuration);
		rawManagingOffice.setRawManagedObjectMetaData(this.rawMoMetaData);
		return rawManagingOffice;
	}

	/**
	 * Creates a {@link ManagedObjectMetaDataImpl} for use in testing.
	 * 
	 * @return {@link ManagedObjectMetaDataImpl}.
	 */
	private ManagedObjectMetaDataImpl<?> createMoMetaData() {
		return new ManagedObjectMetaDataImpl<None>("BOUND", null, -1, null,
				null, false, null, false, null, false, null, 0, null);
	}

	/**
	 * Creates a {@link RawManagingOfficeMetaDataImpl} and runs the manage by
	 * office.
	 * 
	 * @param isCreateExecuteContext
	 *            <code>true</code> if {@link ManagedObjectExecuteContext}
	 *            should be available.
	 * @param recycleWorkName
	 *            Recycle {@link Work} name.
	 * @param processBoundMetaData
	 *            {@link ProcessState} bound
	 *            {@link RawBoundManagedObjectMetaData} for the {@link Office}.
	 * @param flowMetaData
	 *            {@link ManagedObjectFlowMetaData} listing.
	 * @return New {@link RawManagingOfficeMetaDataImpl} with manage by office
	 *         run.
	 */
	private RawManagingOfficeMetaDataImpl<?> run_manageByOffice(
			boolean isCreateExecuteContext, String recycleWorkName,
			RawBoundManagedObjectMetaData[] processBoundMetaData,
			ManagedObjectFlowMetaData<?>... flowMetaData) {

		// Create and manage by office
		RawManagingOfficeMetaDataImpl<?> rawOffice = this
				.createRawManagingOffice(recycleWorkName, flowMetaData);
		rawOffice.manageByOffice(processBoundMetaData, this.metaDataLocator,
				this.officeTeams, this.continueTeamManagement,
				this.assetManagerFactory, this.issues);

		// Validate creation of execute context
		if (isCreateExecuteContext) {
			assertNotNull("Should have execute context available",
					rawOffice.getManagedObjectExecuteContextFactory());
		} else {
			assertNull("Execute context should not be available",
					rawOffice.getManagedObjectExecuteContextFactory());
		}

		// Return the raw managing office meta-data
		return rawOffice;
	}

}