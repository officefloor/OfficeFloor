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
package net.officefloor.frame.util;

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.task.TaskMetaDataImpl;
import net.officefloor.frame.impl.execute.team.TeamManagementImpl;
import net.officefloor.frame.impl.execute.work.WorkMetaDataImpl;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TaskDutyAssociation;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * Factory for the creation of test instances of various objects.
 * 
 * @author Daniel Sagenschneider
 */
public class MetaDataTestInstanceFactory {

	/**
	 * Creates the {@link WorkMetaData}.
	 * 
	 * @param work
	 *            {@link Work}.
	 * @return {@link WorkMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public static <W extends Work> WorkMetaData<W> createWorkMetaData(
			final W work) {

		// Work factory
		final WorkFactory<W> workFactory = new WorkFactory<W>() {
			@Override
			public W createWork() {
				return work;
			}
		};

		// Create the meta-data
		WorkMetaDataImpl<W> metaData = new WorkMetaDataImpl<W>("TEST_WORK",
				workFactory, new ManagedObjectMetaData<?>[0],
				new AdministratorMetaData<?, ?>[0], null, new TaskMetaData[0]);

		// Return the meta-data
		return metaData;
	}

	/**
	 * Creates the {@link TaskMetaData}.
	 * 
	 * @param task
	 *            {@link Task}.
	 * @param workMetaData
	 *            {@link WorkMetaData}.
	 * @return {@link TaskMetaData}.
	 */
	@SuppressWarnings("unchecked")
	public static <W extends Work, D extends Enum<D>, F extends Enum<F>> TaskMetaData<W, D, F> createTaskMetaData(
			final Task<W, D, F> task, WorkMetaData<?> workMetaData) {

		// Task Factory
		final TaskFactory<W, D, F> taskFactory = new TaskFactory<W, D, F>() {
			@Override
			public Task<W, D, F> createTask(W work) {
				return task;
			}
		};

		// Obtain the job name
		String taskName = "TEST_TASK";
		String jobName = workMetaData.getWorkName() + "." + taskName;

		// Create the team
		TeamManagement teamManagement = new TeamManagementImpl(
				new PassiveTeam());

		// Create and initialise the meta-data
		TaskMetaDataImpl<W, D, F> metaData = new TaskMetaDataImpl<W, D, F>(
				jobName, taskName, taskFactory, "TEST_DIFFERENTIATOR",
				Object.class, teamManagement, teamManagement.getTeam(),
				new ManagedObjectIndex[0], new ManagedObjectIndex[0],
				new boolean[0], new TaskDutyAssociation<?>[0],
				new TaskDutyAssociation<?>[0]);
		metaData.loadRemainingState((WorkMetaData<W>) workMetaData,
				new FlowMetaData<?>[0], null, new EscalationProcedureImpl());

		// Return the meta-data
		return metaData;
	}

	/**
	 * All access via static methods.
	 */
	private MetaDataTestInstanceFactory() {
	}
}