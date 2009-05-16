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
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.duty.DutyKeyImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectIndexImpl;
import net.officefloor.frame.impl.execute.task.TaskJob;
import net.officefloor.frame.internal.configuration.TaskEscalationConfiguration;
import net.officefloor.frame.internal.configuration.TaskFlowConfiguration;
import net.officefloor.frame.internal.configuration.TaskConfiguration;
import net.officefloor.frame.internal.configuration.TaskDutyConfiguration;
import net.officefloor.frame.internal.configuration.TaskNodeReference;
import net.officefloor.frame.internal.configuration.TaskObjectConfiguration;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.construct.RawBoundAdministratorMetaData;
import net.officefloor.frame.internal.construct.RawBoundManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawManagedObjectMetaData;
import net.officefloor.frame.internal.construct.RawOfficeMetaData;
import net.officefloor.frame.internal.construct.RawTaskMetaData;
import net.officefloor.frame.internal.construct.RawWorkMetaData;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
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
public class RawTaskMetaDataTest<W extends Work, D extends Enum<D>, F extends Enum<F>>
		extends OfficeFrameTestCase {

	/**
	 * Name of the {@link Task}.
	 */
	private static final String TASK_NAME = "TASK";

	/**
	 * Name of the {@link Work} containing the {@link Task}.
	 */
	private static final String DEFAULT_WORK_NAME = "DEFAULT_WORK_NAME";

	/**
	 * {@link TaskConfiguration}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskConfiguration<W, D, F> configuration = this
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
	private final TaskFactory<W, D, F> taskFactory = this
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
	 * {@link OfficeMetaDataLocator}.
	 */
	private final OfficeMetaDataLocator inputTaskMetaDataLocator = this
			.createMock(OfficeMetaDataLocator.class);

	/**
	 * {@link OfficeMetaDataLocator} to find the {@link TaskMetaData}.
	 */
	private final OfficeMetaDataLocator taskLocator = this
			.createMock(OfficeMetaDataLocator.class);

	/**
	 * {@link AssetManagerFactory}.
	 */
	private final AssetManagerFactory assetManagerFactory = this
			.createMock(AssetManagerFactory.class);

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
		RawTaskMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
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
	 * Ensure issue if no {@link Object} type.
	 */
	public void testNoObjectType() {

		final TaskObjectConfiguration<?> moConfiguration = this
				.createMock(TaskObjectConfiguration.class);

		// Record no managed object name
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getObjectConfiguration(),
				new TaskObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				null);
		this.record_taskIssue("No type for object at index 0");
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to link in a parameter.
	 */
	public void testParameter() {

		final Class<?> parameterType = Connection.class;
		final TaskObjectConfiguration<?> moConfiguration = this
				.createMock(TaskObjectConfiguration.class);

		// Record parameter
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getObjectConfiguration(),
				new TaskObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				parameterType);
		this.recordReturn(moConfiguration, moConfiguration.isParameter(), true);
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> rawMetaData = this
				.constructRawTaskMetaData(true);
		TaskMetaData<W, D, F> metaData = rawMetaData.getTaskMetaData();
		this.verifyMockObjects();

		// Ensure have parameter
		assertEquals("Should have no required objects", 0, metaData
				.getRequiredManagedObjects().length);
		assertEquals("Incorrect parameter type", parameterType, metaData
				.getParameterType());

		// Ensure can translate to parameter
		ManagedObjectIndex parameterIndex = metaData
				.translateManagedObjectIndexForWork(0);
		assertEquals("Incorrect translation to parameter",
				TaskJob.PARAMETER_INDEX, parameterIndex
						.getIndexOfManagedObjectWithinScope());
		assertNull("Should not have scope for parameter index", parameterIndex
				.getManagedObjectScope());
	}

	/**
	 * Ensure able to link in the parameter being used twice.
	 */
	public void testParameterUsedTwice() {

		final Class<?> parameterType = Connection.class;

		// Parameter will be Connection but can be passed as Object
		final Class<?> paramTypeOne = Connection.class;
		final TaskObjectConfiguration<?> paramConfigOne = this
				.createMock(TaskObjectConfiguration.class);
		final Class<?> paramTypeTwo = Object.class;
		final TaskObjectConfiguration<?> paramConfigTwo = this
				.createMock(TaskObjectConfiguration.class);

		// Record parameter used twice
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getObjectConfiguration(), new TaskObjectConfiguration[] {
				paramConfigOne, paramConfigTwo });
		this.recordReturn(paramConfigOne, paramConfigOne.getObjectType(),
				paramTypeOne);
		this.recordReturn(paramConfigOne, paramConfigOne.isParameter(), true);
		this.recordReturn(paramConfigTwo, paramConfigTwo.getObjectType(),
				paramTypeTwo);
		this.recordReturn(paramConfigTwo, paramConfigTwo.isParameter(), true);
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> rawMetaData = this
				.constructRawTaskMetaData(true);
		TaskMetaData<W, D, F> metaData = rawMetaData.getTaskMetaData();
		this.verifyMockObjects();

		// Ensure have parameter
		assertEquals("Should have no required objects", 0, metaData
				.getRequiredManagedObjects().length);
		assertEquals("Parameter type should be most specific", parameterType,
				metaData.getParameterType());

		// Ensure can translate to parameters
		assertEquals("Incorrect translation to parameter one",
				TaskJob.PARAMETER_INDEX, metaData
						.translateManagedObjectIndexForWork(0)
						.getIndexOfManagedObjectWithinScope());
		assertEquals("Incorrect translation to parameter two",
				TaskJob.PARAMETER_INDEX, metaData
						.translateManagedObjectIndexForWork(1)
						.getIndexOfManagedObjectWithinScope());
	}

	/**
	 * Ensure issue if parameter used twice with incompatible types.
	 */
	public void testIncompatibleParameters() {

		// Parameter types are incompatible
		final Class<?> paramTypeOne = Integer.class;
		final TaskObjectConfiguration<?> paramConfigOne = this
				.createMock(TaskObjectConfiguration.class);
		final Class<?> paramTypeTwo = String.class;
		final TaskObjectConfiguration<?> paramConfigTwo = this
				.createMock(TaskObjectConfiguration.class);

		// Record parameters incompatible
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getObjectConfiguration(), new TaskObjectConfiguration[] {
				paramConfigOne, paramConfigTwo });
		this.recordReturn(paramConfigOne, paramConfigOne.getObjectType(),
				paramTypeOne);
		this.recordReturn(paramConfigOne, paramConfigOne.isParameter(), true);
		this.recordReturn(paramConfigTwo, paramConfigTwo.getObjectType(),
				paramTypeTwo);
		this.recordReturn(paramConfigTwo, paramConfigTwo.isParameter(), true);
		this.record_taskIssue("Incompatible parameter types ("
				+ paramTypeOne.getName() + ", " + paramTypeTwo.getName() + ")");
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		this.constructRawTaskMetaData(true);
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link ManagedObject} name.
	 */
	public void testNoScopeManagedObjectName() {

		final TaskObjectConfiguration<?> moConfiguration = this
				.createMock(TaskObjectConfiguration.class);

		// Record no managed object name
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getObjectConfiguration(),
				new TaskObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				Object.class);
		this
				.recordReturn(moConfiguration, moConfiguration.isParameter(),
						false);
		this.recordReturn(moConfiguration, moConfiguration
				.getScopeManagedObjectName(), null);
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

		final TaskObjectConfiguration<?> moConfiguration = this
				.createMock(TaskObjectConfiguration.class);

		// Record unknown managed object
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getObjectConfiguration(),
				new TaskObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				Object.class);
		this
				.recordReturn(moConfiguration, moConfiguration.isParameter(),
						false);
		this.recordReturn(moConfiguration, moConfiguration
				.getScopeManagedObjectName(), "MO");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getScopeManagedObjectMetaData("MO"), null);
		this.record_taskIssue("Can not find scope managed object 'MO'");
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure no managed objects
		assertEquals("Should be no managed objects", 0, metaData
				.getTaskMetaData().getRequiredManagedObjects().length);
	}

	/**
	 * Ensure issue {@link Object} required is incompatible with {@link Object}
	 * from the {@link ManagedObject}.
	 */
	public void testIncompatibleManagedObject() {

		final TaskObjectConfiguration<?> moConfiguration = this
				.createMock(TaskObjectConfiguration.class);
		final RawBoundManagedObjectMetaData<?> rawWorkMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawManagedObjectMetaData<?, ?> rawMo = this
				.createMock(RawManagedObjectMetaData.class);

		// Record incompatible managed object type
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getObjectConfiguration(),
				new TaskObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				Connection.class); // require Connection but Integer
		this
				.recordReturn(moConfiguration, moConfiguration.isParameter(),
						false);
		this.recordReturn(moConfiguration, moConfiguration
				.getScopeManagedObjectName(), "MO");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getScopeManagedObjectMetaData("MO"), rawWorkMo);
		this.recordReturn(rawWorkMo, rawWorkMo.getRawManagedObjectMetaData(),
				rawMo);
		this.recordReturn(rawMo, rawMo.getObjectType(), Integer.class);
		this.record_taskIssue("Managed object MO is incompatible (require="
				+ Connection.class.getName()
				+ ", object of managed object type=" + Integer.class.getName()
				+ ")");
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure no managed objects
		assertEquals("Should be no managed objects", 0, metaData
				.getTaskMetaData().getRequiredManagedObjects().length);
	}

	/**
	 * Ensure able to link in {@link ManagedObject} dependency.
	 */
	public void testManagedObjectDependency() {

		final TaskObjectConfiguration<?> moConfiguration = this
				.createMock(TaskObjectConfiguration.class);
		final RawBoundManagedObjectMetaData<?> rawWorkMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final RawManagedObjectMetaData<?, ?> workMo = this
				.createMock(RawManagedObjectMetaData.class);
		final ManagedObjectIndex workMoIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 0);
		final RawBoundManagedObjectMetaData<?> dependencyWorkMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final ManagedObjectIndex dependencyMoIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 1);

		// Record unknown managed object
		this.record_taskNameFactoryTeam();
		this.recordReturn(this.configuration, this.configuration
				.getObjectConfiguration(),
				new TaskObjectConfiguration[] { moConfiguration });
		this.recordReturn(moConfiguration, moConfiguration.getObjectType(),
				Object.class);
		this
				.recordReturn(moConfiguration, moConfiguration.isParameter(),
						false);
		this.recordReturn(moConfiguration, moConfiguration
				.getScopeManagedObjectName(), "MO");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getScopeManagedObjectMetaData("MO"), rawWorkMo);
		this.recordReturn(rawWorkMo, rawWorkMo.getRawManagedObjectMetaData(),
				workMo);
		this.recordReturn(workMo, workMo.getObjectType(), Connection.class);
		this.recordReturn(rawWorkMo, rawWorkMo.getManagedObjectIndex(),
				workMoIndex);
		this.recordReturn(rawWorkMo, rawWorkMo.getDependencies(),
				new RawBoundManagedObjectMetaData[] { dependencyWorkMo });
		this.recordReturn(dependencyWorkMo, dependencyWorkMo
				.getManagedObjectIndex(), dependencyMoIndex);
		this.recordReturn(dependencyWorkMo, dependencyWorkMo.getDependencies(),
				null);
		this.record_NoAdministration();

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// Ensure have managed object
		ManagedObjectIndex[] requiredManagedObjects = metaData
				.getTaskMetaData().getRequiredManagedObjects();
		assertEquals("Should have managed objects", 2,
				requiredManagedObjects.length);
		assertEquals("Incorrect required managed object", workMoIndex,
				requiredManagedObjects[0]);
		assertEquals("Incorrect dependency managed object", dependencyMoIndex,
				requiredManagedObjects[1]);

		// Ensure can translate
		assertEquals("Incorrect task managed object", workMoIndex, metaData
				.getTaskMetaData().translateManagedObjectIndexForWork(0));
	}

	/**
	 * Ensure issue if no {@link Administrator} name.
	 */
	public void testNoAdministratorName() {

		final TaskDutyConfiguration<?> dutyConfiguration = this
				.createMock(TaskDutyConfiguration.class);

		// Record no administrator name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration, this.configuration
				.getPreTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration, dutyConfiguration
				.getScopeAdministratorName(), null);
		this.record_taskIssue("No administrator name for pre-task at index 0");
		this.recordReturn(this.configuration, this.configuration
				.getPostTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// No duties
		assertEquals("Should not have duties", 0, metaData.getTaskMetaData()
				.getPreAdministrationMetaData().length);
	}

	/**
	 * Ensure issue if unknown {@link Administrator}.
	 */
	public void testUnknownAdministrator() {

		final TaskDutyConfiguration<?> dutyConfiguration = this
				.createMock(TaskDutyConfiguration.class);

		// Record unknown administrator
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration, this.configuration
				.getPreTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration, dutyConfiguration
				.getScopeAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getScopeAdministratorMetaData("ADMIN"), null);
		this.record_taskIssue("Can not find scope administrator 'ADMIN'");
		this.recordReturn(this.configuration, this.configuration
				.getPostTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// No duties
		assertEquals("Should not have duties", 0, metaData.getTaskMetaData()
				.getPreAdministrationMetaData().length);
	}

	/**
	 * Ensure issue if no {@link Duty} name.
	 */
	public void testNoDutyName() {

		final TaskDutyConfiguration<?> dutyConfiguration = this
				.createMock(TaskDutyConfiguration.class);
		final RawBoundAdministratorMetaData<?, ?> rawAdmin = this
				.createMock(RawBoundAdministratorMetaData.class);
		final AdministratorIndex adminIndex = this
				.createMock(AdministratorIndex.class);

		// Record no duty key
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration, this.configuration
				.getPreTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration, dutyConfiguration
				.getScopeAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getScopeAdministratorMetaData("ADMIN"), rawAdmin);
		this.recordReturn(rawAdmin, rawAdmin.getAdministratorIndex(),
				adminIndex);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDutyKey(),
				null);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDutyName(),
				null);
		this.record_taskIssue("No duty name/key for pre-task at index 0");
		this.recordReturn(this.configuration, this.configuration
				.getPostTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);
		this.verifyMockObjects();

		// No duties
		assertEquals("Should not have duties", 0, metaData.getTaskMetaData()
				.getPreAdministrationMetaData().length);
	}

	/**
	 * Ensure able to construct pre {@link TaskDutyAssociation}.
	 */
	public void testConstructPreAdministratorDuty() {

		final TaskDutyConfiguration<?> dutyConfiguration = this
				.createMock(TaskDutyConfiguration.class);
		final RawBoundAdministratorMetaData<?, ?> rawAdmin = this
				.createMock(RawBoundAdministratorMetaData.class);
		final AdministratorIndex adminIndex = this
				.createMock(AdministratorIndex.class);
		final RawBoundManagedObjectMetaData<?> rawMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final ManagedObjectIndex moIndex = this
				.createMock(ManagedObjectIndex.class);
		final DutyKeyImpl<DutyKey> dutyKey = new DutyKeyImpl<DutyKey>(
				DutyKey.KEY);

		// Record construct task duty association
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration, this.configuration
				.getPreTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration, dutyConfiguration
				.getScopeAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getScopeAdministratorMetaData("ADMIN"), rawAdmin);
		this.recordReturn(rawAdmin, rawAdmin.getAdministratorIndex(),
				adminIndex);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDutyKey(),
				DutyKey.KEY);
		this.recordReturn(rawAdmin, rawAdmin.getDutyKey(DutyKey.KEY), dutyKey);
		this.recordReturn(rawAdmin, rawAdmin
				.getAdministeredRawBoundManagedObjects(),
				new RawBoundManagedObjectMetaData[] { rawMo });
		this.recordReturn(rawMo, rawMo.getManagedObjectIndex(), moIndex);
		this.recordReturn(rawMo, rawMo.getDependencies(), null);
		this.recordReturn(this.configuration, this.configuration
				.getPostTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> rawMetaData = this
				.constructRawTaskMetaData(true);
		TaskMetaData<W, D, F> taskMetaData = rawMetaData.getTaskMetaData();
		this.verifyMockObjects();

		// Ensure have duty
		assertEquals("Should have duty", 1, taskMetaData
				.getPreAdministrationMetaData().length);
		TaskDutyAssociation<?> taskDuty = taskMetaData
				.getPreAdministrationMetaData()[0];
		assertEquals("Incorrect administrator index", adminIndex, taskDuty
				.getAdministratorIndex());
		assertEquals("Incorrect duty key", DutyKey.KEY, taskDuty.getDutyKey()
				.getKey());

		// Ensure have administered managed object included in required
		assertEquals("Administered managed objects should be required", 1,
				taskMetaData.getRequiredManagedObjects().length);
		assertEquals("Incorrect administered managed object index", moIndex,
				taskMetaData.getRequiredManagedObjects()[0]);
	}

	/**
	 * Ensures that dependency of administered {@link ManagedObject} is also
	 * included in required {@link ManagedObjectIndex} instances.
	 */
	public void testPostTaskDutyWithAdministeredManagedObjectDependency() {

		final TaskDutyConfiguration<?> dutyConfiguration = this
				.createMock(TaskDutyConfiguration.class);
		final RawBoundAdministratorMetaData<?, ?> rawAdmin = this
				.createMock(RawBoundAdministratorMetaData.class);
		final AdministratorIndex adminIndex = this
				.createMock(AdministratorIndex.class);
		final RawBoundManagedObjectMetaData<?> rawMo = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final ManagedObjectIndex moIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.THREAD, 0);
		final RawBoundManagedObjectMetaData<?> rawDependency = this
				.createMock(RawBoundManagedObjectMetaData.class);
		final ManagedObjectIndex dependencyIndex = new ManagedObjectIndexImpl(
				ManagedObjectScope.PROCESS, 0);
		final DutyKeyImpl<DutyKey> dutyKey = new DutyKeyImpl<DutyKey>(
				DutyKey.KEY);

		// Record construct task duty association
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.recordReturn(this.configuration, this.configuration
				.getPreTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[0]);
		this.recordReturn(this.configuration, this.configuration
				.getPostTaskAdministratorDutyConfiguration(),
				new TaskDutyConfiguration[] { dutyConfiguration });
		this.recordReturn(dutyConfiguration, dutyConfiguration
				.getScopeAdministratorName(), "ADMIN");
		this.recordReturn(this.rawWorkMetaData, this.rawWorkMetaData
				.getScopeAdministratorMetaData("ADMIN"), rawAdmin);
		this.recordReturn(rawAdmin, rawAdmin.getAdministratorIndex(),
				adminIndex);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDutyKey(),
				null);
		this.recordReturn(dutyConfiguration, dutyConfiguration.getDutyName(),
				DutyKey.KEY.name());
		this.recordReturn(rawAdmin, rawAdmin.getDutyKey(DutyKey.KEY.name()),
				dutyKey);
		this.recordReturn(rawAdmin, rawAdmin
				.getAdministeredRawBoundManagedObjects(),
				new RawBoundManagedObjectMetaData[] { rawMo });
		this.recordReturn(rawMo, rawMo.getManagedObjectIndex(), moIndex);
		this.recordReturn(rawMo, rawMo.getDependencies(),
				new RawBoundManagedObjectMetaData[] { rawDependency });
		this.recordReturn(rawDependency, rawDependency.getManagedObjectIndex(),
				dependencyIndex);
		this.recordReturn(rawDependency, rawDependency.getDependencies(), null);

		// Attempt to construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> rawMetaData = this
				.constructRawTaskMetaData(true);
		TaskMetaData<W, D, F> taskMetaData = rawMetaData.getTaskMetaData();
		this.verifyMockObjects();

		// Ensure have duty
		assertEquals("Should have post task duty", 1, taskMetaData
				.getPostAdministrationMetaData().length);
		TaskDutyAssociation<?> taskDuty = taskMetaData
				.getPostAdministrationMetaData()[0];
		assertEquals("Incorrect administrator index", adminIndex, taskDuty
				.getAdministratorIndex());
		assertEquals("Incorrect duty key", DutyKey.KEY, taskDuty.getDutyKey()
				.getKey());

		// Ensure have administered managed object and dependency in required
		assertEquals("Administered managed objects should be required", 2,
				taskMetaData.getRequiredManagedObjects().length);
		assertEquals("Incorrect administered managed object", moIndex,
				taskMetaData.getRequiredManagedObjects()[0]);
		assertEquals("Incorrect administered dependency", dependencyIndex,
				taskMetaData.getRequiredManagedObjects()[1]);
	}

	/**
	 * Ensure issue if no {@link Flow} {@link TaskNodeReference}.
	 */
	public void testNoFlowTaskNodeReference() {

		final TaskFlowConfiguration<?> flowConfiguration = this
				.createMock(TaskFlowConfiguration.class);

		// Record no task node reference
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new TaskFlowConfiguration[] { flowConfiguration });
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

		final TaskFlowConfiguration<?> flowConfiguration = this
				.createMock(TaskFlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record no task name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new TaskFlowConfiguration[] { flowConfiguration });
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

		final TaskFlowConfiguration<?> flowConfiguration = this
				.createMock(TaskFlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record unknown work
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new TaskFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"UNKNOWN WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"IGNORED TASK NAME");
		this.recordReturn(this.taskLocator, this.taskLocator.getTaskMetaData(
				"UNKNOWN WORK", "IGNORED TASK NAME"), null);
		this
				.record_taskIssue("Can not find task meta-data (work=UNKNOWN WORK, task=IGNORED TASK NAME) for flow index 0");
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

		final TaskFlowConfiguration<?> flowConfiguration = this
				.createMock(TaskFlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record unknown task
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new TaskFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.taskLocator, this.taskLocator
				.getTaskMetaData("TASK"), null);
		this.recordReturn(this.taskLocator, this.taskLocator
				.getDefaultWorkMetaData(), this.workMetaData);
		this.recordReturn(this.workMetaData, this.workMetaData.getWorkName(),
				"WORK");
		this
				.record_taskIssue("Can not find task meta-data (work=WORK, task=TASK) for flow index 0");
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if argument to {@link Flow} is not compatible.
	 */
	public void testIncompatibleFlowArgument() {

		final TaskFlowConfiguration<?> flowConfiguration = this
				.createMock(TaskFlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?> flowTaskMetaData = this
				.createMock(TaskMetaData.class);

		// Record incompatible flow argument
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new TaskFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.taskLocator, this.taskLocator.getTaskMetaData(
				"WORK", "TASK"), flowTaskMetaData);
		this.recordReturn(taskNodeReference, taskNodeReference
				.getArgumentType(), Connection.class);
		this.recordReturn(flowTaskMetaData,
				flowTaskMetaData.getParameterType(), Integer.class);
		this
				.record_taskIssue("Argument is not compatible with task parameter (argument="
						+ Connection.class.getName()
						+ ", parameter="
						+ Integer.class.getName()
						+ ", work=WORK, task=TASK) for flow index 0");
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

		final TaskFlowConfiguration<?> flowConfiguration = this
				.createMock(TaskFlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?> flowTaskMetaData = this
				.createMock(TaskMetaData.class);

		// Record no instigation strategy
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new TaskFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.taskLocator, this.taskLocator
				.getTaskMetaData("TASK"), flowTaskMetaData);
		this.recordReturn(taskNodeReference, taskNodeReference
				.getArgumentType(), Connection.class);
		this.recordReturn(flowTaskMetaData,
				flowTaskMetaData.getParameterType(), null);
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
	 * Ensure able to construct an asynchronous {@link Flow}.
	 */
	public void testConstructAsynchronousFlow() {
		this.doConstructFlowTest(FlowInstigationStrategyEnum.ASYNCHRONOUS);
	}

	/**
	 * Ensure able to construct a parallel {@link Flow}.
	 */
	public void testConstructParallelFlow() {
		this.doConstructFlowTest(FlowInstigationStrategyEnum.PARALLEL);
	}

	/**
	 * Ensure able to construct a sequential {@link Flow}.
	 */
	public void testConstructSequentialFlow() {
		this.doConstructFlowTest(FlowInstigationStrategyEnum.SEQUENTIAL);
	}

	/**
	 * Ensure able to construct {@link Flow}.
	 */
	public void doConstructFlowTest(
			FlowInstigationStrategyEnum instigationStrategy) {

		final TaskFlowConfiguration<?> flowConfiguration = this
				.createMock(TaskFlowConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?> flowTaskMetaData = this
				.createMock(TaskMetaData.class);
		final AssetManager assetManager = this.createMock(AssetManager.class);

		// Record construct flow
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(),
				new TaskFlowConfiguration[] { flowConfiguration });
		this.recordReturn(flowConfiguration,
				flowConfiguration.getInitialTask(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"TASK");
		this.recordReturn(this.taskLocator, this.taskLocator
				.getTaskMetaData("TASK"), flowTaskMetaData);
		this.recordReturn(taskNodeReference, taskNodeReference
				.getArgumentType(), Connection.class);
		this.recordReturn(flowTaskMetaData,
				flowTaskMetaData.getParameterType(), Connection.class);
		this.recordReturn(flowConfiguration, flowConfiguration
				.getInstigationStrategy(), instigationStrategy);
		if (instigationStrategy == FlowInstigationStrategyEnum.ASYNCHRONOUS) {
			this.recordReturn(this.assetManagerFactory,
					this.assetManagerFactory.createAssetManager(AssetType.TASK,
							DEFAULT_WORK_NAME + "." + TASK_NAME, "Flow0",
							this.issues), assetManager);
		}
		this.record_NoNextTask();
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Verify flow
		FlowMetaData<?> flowMetaData = metaData.getTaskMetaData().getFlow(0);
		assertEquals("Incorrect initial task meta-data", flowTaskMetaData,
				flowMetaData.getInitialTaskMetaData());
		assertEquals("Incorrect instigation strategy", instigationStrategy,
				flowMetaData.getInstigationStrategy());

		// Ensure correct flow manager
		if (instigationStrategy == FlowInstigationStrategyEnum.ASYNCHRONOUS) {
			// Asynchronous so should have flow manager
			assertEquals("Incorrect flow manager", assetManager, flowMetaData
					.getFlowManager());
		} else {
			// Not asynchronous so not require flow manager
			assertNull("Should not require flow manager", flowMetaData
					.getFlowManager());
		}
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
		this.record_createWorkSpecificTaskMetaDataLocator();
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
	 * Ensure issue if argument to next {@link Task} is incompatible.
	 */
	public void testIncompatibleNextTaskArgument() {

		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?> nextTaskMetaData = this
				.createMock(TaskMetaData.class);

		// Record construct next task (which is on another work)
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.recordReturn(this.configuration, this.configuration
				.getNextTaskInFlow(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"ANOTHER_WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"NEXT_TASK");
		this.recordReturn(this.taskLocator, this.taskLocator.getTaskMetaData(
				"ANOTHER_WORK", "NEXT_TASK"), nextTaskMetaData);
		this.recordReturn(taskNodeReference, taskNodeReference
				.getArgumentType(), Integer.class);
		this.recordReturn(nextTaskMetaData,
				nextTaskMetaData.getParameterType(), Connection.class);
		this
				.record_taskIssue("Argument is not compatible with task parameter (argument="
						+ Integer.class.getName()
						+ ", parameter="
						+ Connection.class.getName()
						+ ", work=ANOTHER_WORK, task=NEXT_TASK) for next task");
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
		final TaskMetaData<?, ?, ?> nextTaskMetaData = this
				.createMock(TaskMetaData.class);

		// Record construct next task (which is on another work)
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.recordReturn(this.configuration, this.configuration
				.getNextTaskInFlow(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"ANOTHER_WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"NEXT_TASK");
		this.recordReturn(this.taskLocator, this.taskLocator.getTaskMetaData(
				"ANOTHER_WORK", "NEXT_TASK"), nextTaskMetaData);
		this.recordReturn(taskNodeReference, taskNodeReference
				.getArgumentType(), null);
		this.recordReturn(nextTaskMetaData,
				nextTaskMetaData.getParameterType(), null);
		this.record_NoEscalations();

		// Fully construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Verify constructed next task
		TaskMetaData<?, ?, ?> nextTask = metaData.getTaskMetaData()
				.getNextTaskInFlow();
		assertEquals("Incorrect next task meta-data", nextTaskMetaData,
				nextTask);
	}

	/**
	 * Ensure issue if no {@link EscalationFlow} type.
	 */
	public void testNoEscalationType() {

		final TaskEscalationConfiguration escalationConfiguration = this
				.createMock(TaskEscalationConfiguration.class);

		// Record no escalation type
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(),
				new TaskEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTypeOfCause(), null);
		this.record_taskIssue("No escalation type for escalation index 0");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure issue if no {@link TaskNodeReference} for {@link EscalationFlow}.
	 */
	public void testNoEscalationTaskNodeReference() {

		final TaskEscalationConfiguration escalationConfiguration = this
				.createMock(TaskEscalationConfiguration.class);

		// Record no task referenced
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(),
				new TaskEscalationConfiguration[] { escalationConfiguration });
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
	 * Ensure issue if no {@link Task} name for {@link EscalationFlow}.
	 */
	public void testNoEscalationTaskName() {

		final TaskEscalationConfiguration escalationConfiguration = this
				.createMock(TaskEscalationConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);

		// Record no escalation task name
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(),
				new TaskEscalationConfiguration[] { escalationConfiguration });
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
	 * Ensure issue if incompatible {@link EscalationFlow}.
	 */
	public void testIncompatibleEscalation() {

		final TaskEscalationConfiguration escalationConfiguration = this
				.createMock(TaskEscalationConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?> escalationTaskMetaData = this
				.createMock(TaskMetaData.class);

		// Record construct escalation
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(),
				new TaskEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTypeOfCause(), IOException.class);
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTaskNodeReference(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				"WORK");
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"ESCALATION_HANDLER");
		this.recordReturn(this.taskLocator, this.taskLocator.getTaskMetaData(
				"WORK", "ESCALATION_HANDLER"), escalationTaskMetaData);
		this.recordReturn(taskNodeReference, taskNodeReference
				.getArgumentType(), NullPointerException.class);
		this.recordReturn(escalationTaskMetaData, escalationTaskMetaData
				.getParameterType(), Integer.class);
		this
				.record_taskIssue("Argument is not compatible with task parameter (argument="
						+ NullPointerException.class.getName()
						+ ", parameter="
						+ Integer.class.getName()
						+ ", work=WORK, task=ESCALATION_HANDLER) for escalation index 0");

		// Fully construct task meta-data
		this.replayMockObjects();
		this.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to construct {@link EscalationFlow}.
	 */
	public void testConstructEscalation() {

		final TaskEscalationConfiguration escalationConfiguration = this
				.createMock(TaskEscalationConfiguration.class);
		final TaskNodeReference taskNodeReference = this
				.createMock(TaskNodeReference.class);
		final TaskMetaData<?, ?, ?> escalationTaskMetaData = this
				.createMock(TaskMetaData.class);

		// Record construct escalation
		this.record_taskNameFactoryTeam();
		this.record_NoManagedObjects();
		this.record_NoAdministration();
		this.record_createWorkSpecificTaskMetaDataLocator();
		this.record_NoFlows();
		this.record_NoNextTask();
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(),
				new TaskEscalationConfiguration[] { escalationConfiguration });
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTypeOfCause(), IOException.class);
		this.recordReturn(escalationConfiguration, escalationConfiguration
				.getTaskNodeReference(), taskNodeReference);
		this.recordReturn(taskNodeReference, taskNodeReference.getWorkName(),
				null); // same work
		this.recordReturn(taskNodeReference, taskNodeReference.getTaskName(),
				"ESCALATION_HANDLER");
		this.recordReturn(this.taskLocator, this.taskLocator
				.getTaskMetaData("ESCALATION_HANDLER"), escalationTaskMetaData);
		this.recordReturn(taskNodeReference, taskNodeReference
				.getArgumentType(), NullPointerException.class);
		this.recordReturn(escalationTaskMetaData, escalationTaskMetaData
				.getParameterType(), RuntimeException.class);

		// Fully construct task meta-data
		this.replayMockObjects();
		RawTaskMetaData<W, D, F> metaData = this
				.fullyConstructRawTaskMetaData();
		this.verifyMockObjects();

		// Verify constructed escalation
		EscalationProcedure escalationProcedure = metaData.getTaskMetaData()
				.getEscalationProcedure();
		EscalationFlow escalation = escalationProcedure
				.getEscalation(new IOException("test"));
		assertEquals("Incorrect type of cause", IOException.class, escalation
				.getTypeOfCause());
		FlowMetaData<?> flowMetaData = escalation.getFlowMetaData();
		assertEquals("Incorrect escalation task meta-data",
				escalationTaskMetaData, flowMetaData.getInitialTaskMetaData());
		assertEquals("Incorrect instigation strategy",
				FlowInstigationStrategyEnum.PARALLEL, flowMetaData
						.getInstigationStrategy());
		assertNull("Should not have flow manager as always parallel",
				flowMetaData.getFlowManager());
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
				.getObjectConfiguration(), new TaskObjectConfiguration[0]);
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
	 * Records obtaining the {@link OfficeMetaDataLocator}.
	 */
	private void record_createWorkSpecificTaskMetaDataLocator() {
		this
				.recordReturn(
						this.inputTaskMetaDataLocator,
						this.inputTaskMetaDataLocator
								.createWorkSpecificOfficeMetaDataLocator(this.workMetaData),
						this.taskLocator);
		this.recordReturn(this.workMetaData, this.workMetaData.getWorkName(),
				DEFAULT_WORK_NAME);
	}

	/**
	 * Records no {@link Flow}.
	 */
	private void record_NoFlows() {
		this.recordReturn(this.configuration, this.configuration
				.getFlowConfiguration(), new TaskFlowConfiguration[0]);
	}

	/**
	 * Records no next {@link Task}.
	 */
	private void record_NoNextTask() {
		this.recordReturn(this.configuration, this.configuration
				.getNextTaskInFlow(), null);
	}

	/**
	 * Records no {@link EscalationFlow}.
	 */
	private void record_NoEscalations() {
		this.recordReturn(this.configuration, this.configuration
				.getEscalations(), new TaskEscalationConfiguration[0]);
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
	private RawTaskMetaData<W, D, F> constructRawTaskMetaData(
			boolean isExpectConstruct) {

		// Construct the raw task meta-data
		RawTaskMetaData<W, D, F> metaData = RawTaskMetaDataImpl.getFactory()
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
	private RawTaskMetaData<W, D, F> fullyConstructRawTaskMetaData() {

		// Construct the raw task meta-data
		RawTaskMetaData<W, D, F> metaData = this.constructRawTaskMetaData(true);

		// Other tasks and work expected to be constructed between these steps

		// Link the tasks and load remaining state to task meta-data
		metaData.linkTasks(this.inputTaskMetaDataLocator, this.workMetaData,
				this.assetManagerFactory, this.issues);

		// Return the fully constructed meta-data
		return metaData;
	}

}