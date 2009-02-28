/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.construct.task;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.impl.construct.officefloor.RawOfficeFloorMetaData;
import net.officefloor.frame.impl.construct.work.RawWorkAdministratorMetaData;
import net.officefloor.frame.impl.construct.work.RawWorkManagedObjectMetaData;
import net.officefloor.frame.impl.construct.work.RawWorkMetaData;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.FlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link RawTaskMetaDataImpl}.
 * 
 * @author Daniel
 */
public class RawTaskMetaDataTest<P, W extends Work, M extends Enum<M>, F extends Enum<F>>
		extends OfficeFrameTestCase {

	/**
	 * Name of the {@link Task}.
	 */
	private static final String TASK_NAME = "TASK";

	/**
	 * {@link TaskConfiguration}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskConfiguration<P, W, M, F> configuration = this
			.createMock(TaskConfiguration.class);

	/**
	 * {@link OfficeFloorIssues}.
	 */
	private final OfficeFloorIssues issues = this
			.createMock(OfficeFloorIssues.class);

	/**
	 * {@link RawWorkMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final RawWorkMetaData<W> rawWorkMetaData = this
			.createMock(RawWorkMetaData.class);

	/**
	 * {@link TaskFactory}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskFactory<P, W, M, F> taskFactory = this
			.createMock(TaskFactory.class);

	/**
	 * {@link RawOfficeMetaData}.
	 */
	private final RawOfficeMetaData rawOfficeMetaData = this
			.createMock(RawOfficeMetaData.class);

	/**
	 * {@link Team}.
	 */
	private final Team team = this.createMock(Team.class);

	/**
	 * {@link WorkMetaData}.
	 */
	@SuppressWarnings("unchecked")
	private final WorkMetaData<W> workMetaData = this
			.createMock(WorkMetaData.class);

	/**
	 * {@link RawWorkMetaData} instances by their {@link Work} name.
	 */
	private final Map<String, RawWorkMetaData<?>> workRegistry = new HashMap<String, RawWorkMetaData<?>>();

	/**
	 * {@link RawOfficeFloorMetaData}.
	 */
	private final RawOfficeFloorMetaData rawOfficeFloorMetaData = this
			.createMock(RawOfficeFloorMetaData.class);

	/**
	 * {@link OfficeFloor} {@link EscalationProcedure}.
	 */
	private final EscalationProcedure officeFloorEscalationProcedure = this
			.createMock(EscalationProcedure.class);

	/**
	 * Ensure issue if not {@link Task} name.
	 */
	public void testNoTaskName() {

		// Record no task name
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				null);
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getWorkName(), "WORK");
		this.issues.addIssue(AssetType.WORK, "WORK", "Task added without name");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link TaskFactory}.
	 */
	public void testNoTaskFactory() {

		// Record no task factory
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTaskFactory(), null);
		this.record_taskIssue("No TaskFactory provided");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Team} name.
	 */
	public void testNoTeamName() {

		// Record no team name
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTaskFactory(), this.taskFactory);
		this.recordReturn(this.configuration, this.configuration
				.getOfficeTeamName(), null);
		this.record_taskIssue("No team name provided for task");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Team}.
	 */
	public void testUnknownTeam() {

		// Record unknown team
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTaskFactory(), this.taskFactory);
		this.recordReturn(this.configuration, this.configuration
				.getOfficeTeamName(), "TEAM");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getRawOfficeMetaData(), this.rawOfficeMetaData);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData
				.getTeams(), new HashMap<String, Team>());
		this.record_taskIssue("Unknown team 'TEAM' responsible for task");

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(false);
		this.verifyMockObjects();
	}

	/**
	 * Ensure {@link Task} name, {@link TaskFactory} and {@link Team} are
	 * available.
	 */
	public void testTaskInitialDetails() {

		// Record initial task details
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Verify initial details
		assertEquals("Incorrect task name", TASK_NAME, metaData.getTaskName());
		assertEquals("Incorrect raw work meta-data", this.rawWorkMetaData,
				metaData.getRawWorkMetaData());
		assertEquals("Incorect task factory", this.taskFactory, metaData
				.getTaskMetaData().getTaskFactory());
		assertEquals("Incorrect team", this.team, metaData.getTaskMetaData()
				.getTeam());
	}

	/**
	 * Ensure issue if no {@link ManagedObject} name.
	 */
	public void testNoManagedObjectName() {

		TaskManagedObjectConfiguration moConfiguration = this
				.createMock(TaskManagedObjectConfiguration.class);

		// Record no managed object name
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectConfiguration(),
				new TaskManagedObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration
				.getWorkManagedObjectName(), null);
		this.record_taskIssue("No name for managed object at index 0");
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link ManagedObject}.
	 */
	public void testUnknownManagedObject() {

		TaskManagedObjectConfiguration moConfiguration = this
				.createMock(TaskManagedObjectConfiguration.class);

		// Record unknown managed object
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectConfiguration(),
				new TaskManagedObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration
				.getWorkManagedObjectName(), "MO");
		this
				.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
						.constructRawWorkManagedObjectMetaData("MO",
								this.issues), null);
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure no managed objects
		assertEquals("Should be no managed objects", 0, metaData
				.getTaskMetaData().getRequiredManagedObjects().length);
	}

	/**
	 * Ensure able to link to a {@link ManagedObject}.
	 */
	public void testLinkManagedObject() {

		TaskManagedObjectConfiguration moConfiguration = this
				.createMock(TaskManagedObjectConfiguration.class);
		RawWorkManagedObjectMetaData rawWorkMo = this
				.createMock(RawWorkManagedObjectMetaData.class);
		final int WORK_MO_INDEX = 3; // managed objects added by other tasks

		// Record unknown managed object
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectConfiguration(),
				new TaskManagedObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration
				.getWorkManagedObjectName(), "MO");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.constructRawWorkManagedObjectMetaData("MO", this.issues),
				rawWorkMo);
		this.recordReturn(rawWorkMo, rawWorkMo.getWorkManagedObjectIndex(),
				WORK_MO_INDEX);
		this.recordReturn(rawWorkMo, rawWorkMo.getDependencies(),
				new RawWorkManagedObjectMetaData[0]);
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure have managed object
		int[] requiredManagedObjects = metaData.getTaskMetaData()
				.getRequiredManagedObjects();
		assertEquals("Should have managed objects", 1,
				requiredManagedObjects.length);
		assertEquals("Incorrect managed object", WORK_MO_INDEX,
				requiredManagedObjects[0]);

		// Ensure can translate
		assertEquals("Incorrect task managed object", WORK_MO_INDEX, metaData
				.getTaskMetaData().translateManagedObjectIndexForWork(0));
	}

	/**
	 * Ensure able to link in {@link ManagedObject} dependency.
	 */
	public void testManagedObjectDependency() {

		TaskManagedObjectConfiguration moConfiguration = this
				.createMock(TaskManagedObjectConfiguration.class);
		RawWorkManagedObjectMetaData rawWorkMo = this
				.createMock(RawWorkManagedObjectMetaData.class);
		final int WORK_MO_INDEX = 3; // managed objects added by other tasks
		RawWorkManagedObjectMetaData dependencyWorkMo = this
				.createMock(RawWorkManagedObjectMetaData.class);
		final int DEPENDENCY_MO_INDEX = 2; // another task using directly

		// Record unknown managed object
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectConfiguration(),
				new TaskManagedObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration
				.getWorkManagedObjectName(), "MO");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.constructRawWorkManagedObjectMetaData("MO", this.issues),
				rawWorkMo);
		this.recordReturn(rawWorkMo, rawWorkMo.getWorkManagedObjectIndex(),
				WORK_MO_INDEX);
		this.recordReturn(rawWorkMo, rawWorkMo.getDependencies(),
				new RawWorkManagedObjectMetaData[] { dependencyWorkMo });
		this.recordReturn(dependencyWorkMo, dependencyWorkMo
				.getWorkManagedObjectIndex(), DEPENDENCY_MO_INDEX);
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure have managed object
		int[] requiredManagedObjects = metaData.getTaskMetaData()
				.getRequiredManagedObjects();
		assertEquals("Should have managed objects", 2,
				requiredManagedObjects.length);
		assertEquals("Incorrect dependency managed object",
				DEPENDENCY_MO_INDEX, requiredManagedObjects[0]);
		assertEquals("Incorrect required managed object", WORK_MO_INDEX,
				requiredManagedObjects[1]);

		// Ensure can translate
		assertEquals("Incorrect task managed object", WORK_MO_INDEX, metaData
				.getTaskMetaData().translateManagedObjectIndexForWork(0));
	}

	/**
	 * Ensure issue if no {@link Administrator} name.
	 */
	public void testNoAdministratorName() {

		TaskDutyConfiguration<?> dutyConfiguration = this
				.createMock(TaskDutyConfiguration.class);

		// Record no administrator name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration, this.configuration
				.getPreTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration, dutyConfiguration
				.getWorkAdministratorName(), null);
		this.record_taskIssue("No administrator name for pre-task at index 0");
		this.recordReturn(this.configuration, this.configuration
				.getPostTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// No duties
		assertEquals("Should not have duties", 0, metaData.getTaskMetaData()
				.getPreAdministrationMetaData().length);
	}

	/**
	 * Ensure issue if unknown {@link Administrator}.
	 */
	public void testUnknownAdministrator() {

		TaskDutyConfiguration<?> dutyConfiguration = this
				.createMock(TaskDutyConfiguration.class);

		// Record unknown administrator
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration, this.configuration
				.getPreTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration, dutyConfiguration
				.getWorkAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.constructRawWorkAdministratorMetaData("ADMIN", this.issues),
				null);
		this.recordReturn(this.configuration, this.configuration
				.getPostTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// No duties
		assertEquals("Should not have duties", 0, metaData.getTaskMetaData()
				.getPreAdministrationMetaData().length);
	}

	/**
	 * Ensure issue if no {@link Duty} key.
	 */
	public void testNoDutyKey() {

		TaskDutyConfiguration<?> dutyConfiguration = this
				.createMock(TaskDutyConfiguration.class);
		RawWorkAdministratorMetaData rawWorkAdmin = this
				.createMock(RawWorkAdministratorMetaData.class);

		// Record no duty key
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration, this.configuration
				.getPreTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration, dutyConfiguration
				.getWorkAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.constructRawWorkAdministratorMetaData("ADMIN", this.issues),
				rawWorkAdmin);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDuty(), null);
		this.record_taskIssue("No duty key for pre-task at index 0");
		this.recordReturn(this.configuration, this.configuration
				.getPostTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// No duties
		assertEquals("Should not have duties", 0, metaData.getTaskMetaData()
				.getPreAdministrationMetaData().length);
	}

	/**
	 * Ensure able to link {@link Administrator} {@link Duty}.
	 */
	public void testLinkAdministrator() {

		TaskDutyConfiguration<?> dutyConfiguration = this
				.createMock(TaskDutyConfiguration.class);
		RawWorkAdministratorMetaData rawWorkAdmin = this
				.createMock(RawWorkAdministratorMetaData.class);
		final int WORK_ADMIN_INDEX = 2; // other tasks added admins

		// Record link administrator duty (do post as previous tests did pre)
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration, this.configuration
				.getPreTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getPostTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration, dutyConfiguration
				.getWorkAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.constructRawWorkAdministratorMetaData("ADMIN", this.issues),
				rawWorkAdmin);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDuty(),
				DutyKey.KEY);
		this.recordReturn(rawWorkAdmin, rawWorkAdmin
				.getWorkAdministratorIndex(), WORK_ADMIN_INDEX);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure post-task duty only
		assertEquals("Should not have pre-task duties", 0, metaData
				.getTaskMetaData().getPreAdministrationMetaData().length);
		TaskDutyAssociation<?>[] postDuties = metaData.getTaskMetaData()
				.getPostAdministrationMetaData();
		assertEquals("Should have post-task duties", 1, postDuties.length);
		TaskDutyAssociation<?> postDuty = postDuties[0];
		assertEquals("Incorrect admin index for duty", WORK_ADMIN_INDEX,
				postDuty.getAdministratorIndex());
		assertEquals("Incorrect key for duty", DutyKey.KEY, postDuty
				.getDutyKey());
	}

	/**
	 * Ensure issue if no {@link Flow} {@link TaskNodeReference}.
	 */
	public void testNoFlowTaskNodeReference() {

		final FlowConfiguration flowConfiguration = this
				.createMock(FlowConfiguration.class);

		// Record no task node reference
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new FlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), null);
		this.record_taskIssue("No task referenced for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Flow} {@link Task} name.
	 */
	public void testNoFlowTaskName() {

		final FlowConfiguration flowConfiguration = this
				.createMock(FlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record no task name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new FlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				null);
		this.record_taskIssue("No task name provided for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Work} containing the {@link Flow}
	 * {@link Task}.
	 */
	public void testUnknownFlowWork() {

		final FlowConfiguration flowConfiguration = this
				.createMock(FlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record unknown work
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new FlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"ANOTHER WORK");
		this.record_taskIssue("Unknown work 'ANOTHER WORK' for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if unknown {@link Flow} {@link Task}.
	 */
	public void testUnknownFlowTask() {

		final FlowConfiguration flowConfiguration = this
				.createMock(FlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record unknown task
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new FlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getRawTaskMetaData("TASK"), null);
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getWorkName(), "WORK");
		this
				.record_taskIssue("Unknown task 'TASK' on work WORK for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link FlowInstigationStrategyEnum}.
	 */
	public void testNoFlowInstigationStrategy() {

		final FlowConfiguration flowConfiguration = this
				.createMock(FlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final RawTaskMetaData<?, ?, ?, ?> flowRawTaskMetaData = this
				.createMock(RawTaskMetaData.class);
		final TaskMetaData<?, ?, ?, ?> flowTaskMetaData = this
				.createMock(TaskMetaData.class);

		// Record no instigation strategy
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new FlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getRawTaskMetaData("TASK"), flowRawTaskMetaData);
		this.recordReturn(flowRawTaskMetaData, flowRawTaskMetaData
				.getTaskMetaData(), flowTaskMetaData);
		this.recordReturn(flowConfiguration, flowConfiguration
				.getInstigationStrategy(), null);
		this
				.record_taskIssue("No instigation strategy provided for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link Flow}.
	 */
	public void testConstructFlow() {

		final FlowConfiguration flowConfiguration = this
				.createMock(FlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final RawTaskMetaData<?, ?, ?, ?> flowRawTaskMetaData = this
				.createMock(RawTaskMetaData.class);
		final TaskMetaData<?, ?, ?, ?> flowTaskMetaData = this
				.createMock(TaskMetaData.class);

		// Record construct flow
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new FlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getRawTaskMetaData("TASK"), flowRawTaskMetaData);
		this.recordReturn(flowRawTaskMetaData, flowRawTaskMetaData
				.getTaskMetaData(), flowTaskMetaData);
		this.recordReturn(flowConfiguration, flowConfiguration
				.getInstigationStrategy(),
				FlowInstigationStrategyEnum.SEQUENTIAL);
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Verify flow
		FlowMetaData<?> flowMetaData = metaData.getTaskMetaData().getFlow(0);
		assertEquals("Incorrect initial task meta-data", flowTaskMetaData,
				flowMetaData.getInitialTaskMetaData());
		assertEquals("Incorrect instigation strategy",
				FlowInstigationStrategyEnum.SEQUENTIAL, flowMetaData
						.getInstigationStrategy());

		// TODO verify flow manager
		flowMetaData.getFlowManager();
	}

	/**
	 * Ensure issue if no next {@link Task} name.
	 */
	public void testNoNextTaskName() {

		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record no next task name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.record_NoFlows();
		this.recordReturn(this.configuration, this.configuration
				.getNextTaskInFlow(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null);
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				null);
		this.record_taskIssue("No task name provided for next task");
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct next {@link TaskMetaData}.
	 */
	public void testConstructNextTask() {

		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final RawTaskMetaData<?, ?, ?, ?> nextRawTaskMetaData = this
				.createMock(RawTaskMetaData.class);
		final TaskMetaData<?, ?, ?, ?> nextTaskMetaData = this
				.createMock(TaskMetaData.class);

		// Record construct next task
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.record_NoFlows();
		this.recordReturn(this.configuration, this.configuration
				.getNextTaskInFlow(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null);
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"NEXT_TASK");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getRawTaskMetaData("NEXT_TASK"), nextRawTaskMetaData);
		this.recordReturn(nextRawTaskMetaData, nextRawTaskMetaData
				.getTaskMetaData(), nextTaskMetaData);
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Verify constructed next task
		TaskMetaData<?, ?, ?, ?> nextTask = metaData.getTaskMetaData()
				.getNextTaskInFlow();
		assertEquals("Incorrect next task meta-data", nextTaskMetaData,
				nextTask);
	}

	/**
	 * Ensure issue if no {@link Escalation} type.
	 */
	public void testNoEscalationType() {

		final EscalationConfiguration escalationConfiguration = this
				.createMock(EscalationConfiguration.class);

		// Record no escalation type
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.record_OfficeFloorEscalationProcedure();
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(),
				new EscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTypeOfCause(), null);
		this.record_taskIssue("No escalation type for escalation index 0");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link TaskNodeReference} for {@link Escalation}.
	 */
	public void testNoEscalationTaskNodeReference() {

		final EscalationConfiguration escalationConfiguration = this
				.createMock(EscalationConfiguration.class);

		// Record no task referenced
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.record_OfficeFloorEscalationProcedure();
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(),
				new EscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTypeOfCause(), IOException.class);
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTaskNodeReference(), null);
		this.record_taskIssue("No task referenced for escalation index 0");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link Task} name for {@link Escalation}.
	 */
	public void testNoEscalationTaskName() {

		final EscalationConfiguration escalationConfiguration = this
				.createMock(EscalationConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record no escalation task name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.record_OfficeFloorEscalationProcedure();
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(),
				new EscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTypeOfCause(), IOException.class);
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTaskNodeReference(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				null);
		this.record_taskIssue("No task name provided for escalation index 0");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link Escalation}.
	 */
	public void testConstructEscalation() {

		final EscalationConfiguration escalationConfiguration = this
				.createMock(EscalationConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final RawTaskMetaData<?, ?, ?, ?> escalationRawTaskMetaData = this
				.createMock(RawTaskMetaData.class);
		final TaskMetaData<?, ?, ?, ?> escalationTaskMetaData = this
				.createMock(TaskMetaData.class);

		// Record construct escalation
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_WorkMetaData();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.record_OfficeFloorEscalationProcedure();
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(),
				new EscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTypeOfCause(), IOException.class);
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTaskNodeReference(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"ESCALATION_HANDLER");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getRawTaskMetaData("ESCALATION_HANDLER"),
				escalationRawTaskMetaData);
		this.recordReturn(escalationRawTaskMetaData, escalationRawTaskMetaData
				.getTaskMetaData(), escalationTaskMetaData);

		// Fully construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<P, W, M, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Verify constructed escalation
		EscalationProcedure escalationProcedure = metaData.getTaskMetaData()
				.getEscalationProcedure();
		Escalation escalation = escalationProcedure
				.getEscalation(new IOException("test"));
		assertEquals("Incorrect type of cause", IOException.class, escalation
				.getTypeOfCause());
		FlowMetaData<?> flowMetaData = escalation.getFlowMetaData();
		assertEquals("Incorrect escalation task meta-data",
				escalationTaskMetaData, flowMetaData.getInitialTaskMetaData());
		assertEquals("Incorrect instigation strategy",
				FlowInstigationStrategyEnum.SEQUENTIAL, flowMetaData
						.getInstigationStrategy());
	}

	/**
	 * Records obtaining {@link Task} name, {@link TaskFactory} and responsible
	 * {@link Team}.
	 */
	private void record_taskNameFactoryTeam() {
		this.recordReturn(this.configuration, this.configuration.getTaskName(),
				TASK_NAME);
		this.recordReturn(this.configuration, this.configuration
				.getTaskFactory(), this.taskFactory);
		this.recordReturn(this.configuration, this.configuration
				.getOfficeTeamName(), "TEAM");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getRawOfficeMetaData(), this.rawOfficeMetaData);
		Map<String, Team> teams = new HashMap<String, Team>();
		teams.put("TEAM", this.team);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData
				.getTeams(), teams);
	}

	/**
	 * Records no {@link ManagedObject} instances.
	 */
	private void record_NoManagedObjects() {
		this.recordReturn(this.configuration, this.configuration
				.getManagedObjectConfiguration(),
				new TaskManagedObjectConfiguration[0]);
	}

	/**
	 * Records no {@link Administrator} {@link Duty} instances for {@link Task}.
	 */
	private void record_NoAdministration() {
		this.recordReturn(this.configuration, this.configuration
				.getPreTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getPostTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);
	}

	/**
	 * Duty key for testing.
	 */
	private static enum DutyKey {
		KEY
	}

	/**
	 * Records obtaining the {@link WorkMetaData}.
	 */
	private void record_WorkMetaData() {
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getWorkMetaData(this.issues), this.workMetaData);
	}

	/**
	 * Records no {@link Flow}.
	 */
	private void record_NoFlows() {
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(), new FlowConfiguration[0]);
	}

	/**
	 * Records no next {@link Task}.
	 */
	private void record_NoNextTask() {
		this.recordReturn(this.configuration, this.configuration
				.getNextTaskInFlow(), null);
	}

	/**
	 * Records obtaining the {@link OfficeFloor} {@link EscalationProcedure}.
	 */
	private void record_OfficeFloorEscalationProcedure() {
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getRawOfficeMetaData(), this.rawOfficeMetaData);
		this.recordReturn(this.rawOfficeMetaData, this.rawOfficeMetaData
				.getRawOfficeFloorMetaData(), this.rawOfficeFloorMetaData);
		this.recordReturn(this.rawOfficeFloorMetaData,
				this.rawOfficeFloorMetaData.getEscalationProcedure(),
				this.officeFloorEscalationProcedure);
	}

	/**
	 * Records no {@link Escalation}.
	 */
	private void record_NoEscalations() {
		this.record_OfficeFloorEscalationProcedure();
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(), new EscalationConfiguration[0]);
	}

	/**
	 * Records an issue on the {@link OfficeFloorIssues} about the {@link Task}.
	 * 
	 * @param issueDescription
	 *            Issue description expected.
	 */
	private void record_taskIssue(String issueDescription) {
		this.issues.addIssue(AssetType.TASK, TASK_NAME, issueDescription);
	}

	/**
	 * Constructs the {@link RawTaskMetaData}.
	 * 
	 * @param isExpectConstruct
	 *            If expected to be constructed.
	 * @return {@link RawTaskMetaData}.
	 */
	private RawTaskMetaData<P, W, M, F> constructRawTaskMetaData(
			boolean isExpectConstruct) {

		// Construct the raw task meta-data
		RawTaskMetaData<P, W, M, F> metaData = RawTaskMetaDataImpl.getFactory()
				.constructRawTaskMetaData(this.configuration, this.issues,
						this.rawWorkMetaData);
		if (isExpectConstruct) {
			assertNotNull("Expected to construct meta-data", metaData);
		} else {
			assertNull("Not expected to construct meta-data", metaData);
		}

		// Return the meta-data
		return metaData;
	}

	/**
	 * Fully constructs the {@link RawTaskMetaData} by ensuring remaining state
	 * is loaded. Will always expect to construct the {@link RawTaskMetaData}.
	 * 
	 * @return {@link RawTaskMetaData}.
	 */
	private RawTaskMetaData<P, W, M, F> fullyConstructRawTaskMetaData() {

		// Construct the raw task meta-data
		RawTaskMetaData<P, W, M, F> metaData = this
				.constructRawTaskMetaData(true);

		// Other tasks and work expected to be constructed between these steps

		// Load the remaining state
		metaData.loadRemainingState(this.workRegistry, this.issues);

		// Return the fully constructed meta-data
		return metaData;
	}

}