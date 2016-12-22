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

import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectTaskBuilder;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectWorkBuilder;
import net.officefloor.frame.spi.team.Team;

/**
 * Provides an abstract {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSingleTask<W extends Work, D extends Enum<D>, F extends Enum<F>>
		implements WorkFactory<W>, Work, ManagedFunctionFactory<W, D, F>, ManagedFunction<W, D, F> {

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
	 * Registers the {@link ManagedFunction} with the input {@link WorkBuilder}.
	 * 
	 * @param taskName
	 *            Name for {@link ManagedFunction}.
	 * @param teamName
	 *            Name for {@link Team}.
	 * @param workBuilder
	 *            {@link WorkBuilder}.
	 * @return {@link ManagedFunctionBuilder} to configure the {@link ManagedFunction}.
	 */
	public ManagedFunctionBuilder<W, D, F> registerTask(String taskName, String teamName,
			WorkBuilder<W> workBuilder) {

		// Configure the work builder
		workBuilder.setInitialTask(taskName);

		// Configure this as a task
		ManagedFunctionBuilder<W, D, F> task = workBuilder.addManagedFunction(taskName, this);
		task.setTeam(teamName);

		// Return the task builder
		return task;
	}

	/**
	 * Registers this {@link ManagedFunction} with the input {@link OfficeBuilder}.
	 * 
	 * @param workName
	 *            Name for {@link Work}.
	 * @param taskName
	 *            Name for {@link ManagedFunction}.
	 * @param teamName
	 *            Name for {@link Team}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @return {@link ManagedFunctionBuilder} to configure the {@link ManagedFunction}.
	 */
	public ManagedFunctionBuilder<W, D, F> registerTask(String workName, String taskName,
			String teamName, OfficeBuilder officeBuilder) {

		// Create and register the work builder
		WorkBuilder<W> work = this.registerWork(workName, officeBuilder);

		// Create and register the task builder
		ManagedFunctionBuilder<W, D, F> task = this.registerTask(taskName, teamName, work);

		// Return the task builder
		return task;
	}

	/**
	 * Registers this {@link ManagedFunction} with the input
	 * {@link ManagedObjectSourceContext}.
	 * 
	 * @param workName
	 *            Name for {@link Work}.
	 * @param taskName
	 *            Name for {@link ManagedFunction}.
	 * @param teamName
	 *            Name for {@link Team}.
	 * @param context
	 *            {@link ManagedObjectSourceContext}.
	 * @return {@link ManagedObjectTaskBuilder} to configure the {@link ManagedFunction}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectTaskBuilder<D, F> registerTask(String workName,
			String taskName, String teamName,
			ManagedObjectSourceContext<F> context) {

		// Create and initialise the work builder
		ManagedObjectWorkBuilder work = context.addWork(workName, this);

		// Create the the task builder
		ManagedObjectTaskBuilder<D, F> task = work.addTask(taskName, this);
		task.setTeam(teamName);

		// Return the task builder
		return task;
	}

	/**
	 * Registers this {@link ManagedFunction} to recycle the {@link ManagedObject}.
	 *
	 * @param context
	 *            {@link ManagedObjectSourceContext}.
	 * @param teamName
	 *            Name of the {@link Team} to recycle the {@link ManagedObject}.
	 * @see #getRecycleManagedObjectParameter(ManagedFunctionContext, Class)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerAsRecycleTask(ManagedObjectSourceContext context,
			String teamName) {

		// Get the recycle work builder
		ManagedObjectWorkBuilder recycleWork = context.getRecycleWork(this);

		// Configure this task to recycle the managed object
		ManagedObjectTaskBuilder recycleTask = recycleWork.addTask("recycle",
				this);
		recycleTask.setTeam(teamName);
		recycleTask.linkParameter(0, RecycleManagedObjectParameter.class);
	}

	/**
	 * Obtains the {@link RecycleManagedObjectParameter}.
	 * 
	 * @param <MO>
	 *            {@link ManagedObject} type.
	 * @param context
	 *            {@link ManagedFunctionContext}.
	 * @param managedObjectClass
	 *            {@link Class} of the {@link ManagedObject}.
	 * @return {@link RecycleManagedObjectParameter}.
	 */
	@SuppressWarnings("unchecked")
	protected <MO extends ManagedObject> RecycleManagedObjectParameter<MO> getRecycleManagedObjectParameter(
			ManagedFunctionContext<W, D, F> context, Class<MO> managedObjectClass) {
		return (RecycleManagedObjectParameter<MO>) context.getObject(0);
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
	public ManagedFunction<W, D, F> createManagedFunction(W work) {
		return this;
	}

}