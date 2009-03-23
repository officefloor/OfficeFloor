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
package net.officefloor.frame.util;

import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.team.Team;

/**
 * Provides an abstract {@link Task}.
 * 
 * @author Daniel
 */
public abstract class AbstractSingleTask<W extends Work, D extends Enum<D>, F extends Enum<F>>
		implements WorkFactory<W>, Work, TaskFactory<W, D, F>, Task<W, D, F> {

	/**
	 * Registers this {@link Work} with the input {@link OfficeBuilder}.
	 * 
	 * @param workName
	 *            Name for {@link Work}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @return {@link WorkBuilder} to configure the {@link Work}.
	 */
	public WorkBuilder<W> registerWork(String workName,
			OfficeBuilder officeBuilder) {
		WorkBuilder<W> work = officeBuilder.addWork(workName, this);
		return work;
	}

	/**
	 * Registers the {@link Task} with the input {@link WorkBuilder}.
	 * 
	 * @param taskName
	 *            Name for {@link Task}.
	 * @param teamName
	 *            Name for {@link Team}.
	 * @param workBuilder
	 *            {@link WorkBuilder}.
	 * @return {@link TaskBuilder} to configure the {@link Task}.
	 */
	public TaskBuilder<W, D, F> registerTask(String taskName, String teamName,
			WorkBuilder<W> workBuilder) {

		// Configure the work builder
		workBuilder.setInitialTask(taskName);

		// Configure this as a task
		TaskBuilder<W, D, F> task = workBuilder.addTask(taskName, this);
		task.setTeam(teamName);

		// Return the task builder
		return task;
	}

	/**
	 * Registers this {@link Task} with the input {@link OfficeBuilder}.
	 * 
	 * @param workName
	 *            Name for {@link Work}.
	 * @param taskName
	 *            Name for {@link Task}.
	 * @param teamName
	 *            Name for {@link Team}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @return {@link TaskBuilder} to configure the {@link Task}.
	 */
	public TaskBuilder<W, D, F> registerTask(String workName, String taskName,
			String teamName, OfficeBuilder officeBuilder) {

		// Create and register the work builder
		WorkBuilder<W> work = this.registerWork(workName, officeBuilder);

		// Create and register the task builder
		TaskBuilder<W, D, F> task = this.registerTask(taskName, teamName, work);

		// Return the task builder
		return task;
	}

	/**
	 * Registers this {@link Task} with the input
	 * {@link ManagedObjectSourceContext}.
	 * 
	 * @param workName
	 *            Name for {@link Work}.
	 * @param taskName
	 *            Name for {@link Task}.
	 * @param teamName
	 *            Name for {@link Team}.
	 * @param context
	 *            {@link ManagedObjectSourceContext}.
	 * @return {@link ManagedObjectTaskBuilder} to configure the {@link Task}.
	 */
	@SuppressWarnings("unchecked")
	public ManagedObjectTaskBuilder<F> registerTask(String workName,
			String taskName, String teamName,
			ManagedObjectSourceContext<F> context) {

		// Create and initialise the work builder
		ManagedObjectWorkBuilder work = context.addWork(workName, this);

		// Create the the task builder
		ManagedObjectTaskBuilder<F> task = work.addTask(taskName, this);
		task.setTeam(teamName);

		// Return the task builder
		return task;
	}

	/**
	 * Registers this {@link Task} as the recycler for the
	 * {@link ManagedObjectSourceContext}.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceContext}.
	 * @param teamName
	 *            Name of the {@link Team} to recycle the {@link ManagedObject}.
	 */
	@SuppressWarnings("unchecked")
	public void registerAsRecycleTask(ManagedObjectSourceContext context,
			String teamName) {

		// Get the recycle work builder
		ManagedObjectWorkBuilder recycleWork = context.getRecycleWork(this);

		// Configure this task to recycle the managed object
		ManagedObjectTaskBuilder recycleTask = recycleWork.addTask("recycle",
				this);
		recycleTask.setTeam(teamName);
	}

	/*
	 * =================== WorkFactory ========================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public W createWork() {
		return (W) this;
	}

	/*
	 * =================== TaskFactory ========================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Task<W, D, F> createTask(W work) {
		return (Task<W, D, F>) work;
	}

}