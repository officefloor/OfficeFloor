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
package net.officefloor.compile.impl.work;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.work.TaskType;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.TaskFactoryManufacturer;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;

/**
 * {@link WorkType} implementation.
 * 
 * @author Daniel
 */
public class WorkTypeImpl<W extends Work> implements WorkType<W>,
		WorkTypeBuilder<W> {

	/**
	 * {@link WorkFactory}.
	 */
	private WorkFactory<W> workFactory;

	/**
	 * Listing of the {@link TaskType} definitions.
	 */
	private final List<TaskType<W, ?, ?>> tasks = new LinkedList<TaskType<W, ?, ?>>();

	/*
	 * =================== WorkTypeBuilder ====================================
	 */

	@Override
	public void setWorkFactory(WorkFactory<W> workFactory) {
		this.workFactory = workFactory;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <M extends Enum<M>, F extends Enum<F>> TaskTypeBuilder<M, F> addTaskType(
			String taskName,
			TaskFactoryManufacturer<? super W, M, F> taskFactoryManufacturer,
			Class<M> objectKeysClass, Class<F> flowKeysClass) {
		TaskTypeImpl taskType = new TaskTypeImpl(taskName,
				taskFactoryManufacturer, objectKeysClass, flowKeysClass);
		this.tasks.add(taskType);
		return taskType;
	}

	/*
	 * =================== WorkType ===========================================
	 */

	@Override
	public WorkFactory<W> getWorkFactory() {
		return this.workFactory;
	}

	@Override
	@SuppressWarnings("unchecked")
	public TaskType<W, ?, ?>[] getTaskTypes() {
		return this.tasks.toArray(new TaskType[0]);
	}

}