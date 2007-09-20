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

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.BuildException;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.execute.WorkContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;

/**
 * Provides a abstract {@link net.officefloor.frame.api.execute.Task}.
 * 
 * @author Daniel
 */
public abstract class AbstractSingleTask<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>>
		implements WorkFactory<W>, Work, TaskFactory<P, W, M, F>,
		Task<P, W, M, F> {

	/**
	 * Registers this {@link Work} with the input {@link OfficeBuilder}.
	 * 
	 * @param workName
	 *            Name for {@link Work}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @return {@link WorkBuilder} to configure the {@link Work}.
	 * @throws BuildException
	 *             If fails to register.
	 */
	@SuppressWarnings("unchecked")
	public WorkBuilder registerWork(String workName, OfficeBuilder officeBuilder)
			throws BuildException {
		// Create and register the work builder
		WorkBuilder work = OfficeFrame.getInstance().getMetaDataFactory()
				.createWorkBuilder(this.getClass());
		officeBuilder.addWork(workName, work);

		// Configure the work builder
		work.setWorkFactory(this);

		// Return the work builder
		return work;
	}

	/**
	 * Registers the {@link Task} with the input {@link WorkBuilder}.
	 * 
	 * @param taskName
	 *            Name for {@link Task}.
	 * @param teamName
	 *            Name for {@link net.officefloor.frame.spi.team.Team}.
	 * @param workBuilder
	 *            {@link WorkBuilder}.
	 * @return {@link TaskBuilder} to configure the {@link Task}.
	 * @throws BuildException
	 *             If fails to register.
	 */
	@SuppressWarnings("unchecked")
	public TaskBuilder registerTask(String taskName, String teamName,
			WorkBuilder workBuilder) throws BuildException {

		// Configure the work builder
		workBuilder.setInitialTask(taskName);

		// Configure this as a task
		TaskBuilder task = workBuilder.addTask(taskName, Object.class);
		task.setTaskFactory(this);
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
	 *            Name for {@link net.officefloor.frame.spi.team.Team}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @return {@link TaskBuilder} to configure the {@link Task}.
	 * @throws BuildException
	 *             If fails to register.
	 */
	@SuppressWarnings("unchecked")
	public TaskBuilder registerTask(String workName, String taskName,
			String teamName, OfficeBuilder officeBuilder) throws BuildException {
		// Create and register the work builder
		WorkBuilder work = this.registerWork(workName, officeBuilder);

		// Create and register the task builder
		TaskBuilder task = this.registerTask(taskName, teamName, work);

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
	 *            Name of the {@link net.officefloor.frame.spi.team.Team} to
	 *            recycle the {@link ManagedObject}.
	 * @throws BuildException
	 *             If fails to configure.
	 */
	@SuppressWarnings("unchecked")
	public void registerAsRecycleTask(ManagedObjectSourceContext context,
			String teamName) throws BuildException {
		// Get the recycle work builder
		WorkBuilder recycleWork = context
				.getRecycleWorkBuilder(this.getClass());

		// Configure the work builder
		recycleWork.setWorkFactory(this);
		recycleWork.setInitialTask("recycle");

		// Configure this task to recycle the managed object
		TaskBuilder recycleTask = recycleWork.addTask("recycle",
				ManagedObject.class);
		recycleTask.setTaskFactory(this);
		recycleTask.setTeam(teamName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.WorkFactory#createWork()
	 */
	@SuppressWarnings("unchecked")
	public W createWork() {
		return (W) this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.execute.Work#setWorkContext(net.officefloor.frame.api.execute.WorkContext)
	 */
	public void setWorkContext(WorkContext context) throws Exception {
		// Not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.api.build.TaskFactory#createTask(W)
	 */
	@SuppressWarnings("unchecked")
	public Task<P, W, M, F> createTask(W work) {
		return (Task<P, W, M, F>) work;
	}

}
