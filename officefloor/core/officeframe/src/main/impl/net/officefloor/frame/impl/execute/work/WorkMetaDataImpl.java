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
package net.officefloor.frame.impl.execute.work;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Implementation of the {@link WorkMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkMetaDataImpl<W extends Work> implements WorkMetaData<W> {

	/**
	 * Name of this {@link Work}.
	 */
	private final String workName;

	/**
	 * {@link WorkFactory}.
	 */
	private final WorkFactory<W> workFactory;

	/**
	 * {@link ManagedObjectMetaData} of the {@link ManagedObject} instances
	 * bound to this {@link Work}.
	 */
	private final ManagedObjectMetaData<?>[] managedObjectMetaData;

	/**
	 * {@link AdministratorMetaData} of the {@link Administrator} instances
	 * bound to this {@link Work}.
	 */
	private final AdministratorMetaData<?, ?>[] administratorMetaData;

	/**
	 * {@link FlowMetaData} for the initial {@link Flow} of the {@link Work} .
	 */
	private final FlowMetaData<W> initialFlowMetaData;

	/**
	 * {@link ManagedFunctionMetaData} of the {@link ManagedFunction} instances of this {@link Work}.
	 */
	private final ManagedFunctionMetaData<W, ?, ?>[] taskMetaData;

	/**
	 * Initiate.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @param workFactory
	 *            {@link WorkFactory}.
	 * @param moMetaData
	 *            {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 *            instances bound to this {@link Work}.
	 * @param adminMetaData
	 *            {@link AdministratorMetaData} of the {@link Administrator}
	 *            instances bound to this {@link Work}.
	 * @param initialFlowMetaData
	 *            {@link FlowMetaData} for the initial {@link Flow} of the
	 *            {@link Work}.
	 * @param taskMetaData
	 *            {@link ManagedFunctionMetaData} of the {@link ManagedFunction} instances of this
	 *            {@link Work}.
	 */
	public WorkMetaDataImpl(String workName, WorkFactory<W> workFactory, ManagedObjectMetaData<?>[] moMetaData,
			AdministratorMetaData<?, ?>[] adminMetaData, FlowMetaData<W> initialFlowMetaData,
			ManagedFunctionMetaData<W, ?, ?>[] taskMetaData) {
		this.workName = workName;
		this.workFactory = workFactory;
		this.managedObjectMetaData = moMetaData;
		this.administratorMetaData = adminMetaData;
		this.initialFlowMetaData = initialFlowMetaData;
		this.taskMetaData = taskMetaData;
	}

	/*
	 * ===================== WorkMetaData =================================
	 */

	@Override
	public String getWorkName() {
		return this.workName;
	}

	@Override
	public WorkContainer<W> createWorkContainer(ThreadState threadState) {
		W work = this.workFactory.createWork();
		return new WorkContainerImpl<W>(work, this, threadState);
	}

	@Override
	public WorkFactory<W> getWorkFactory() {
		return this.workFactory;
	}

	@Override
	public FlowMetaData<W> getInitialFlowMetaData() {
		return this.initialFlowMetaData;
	}

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public AdministratorMetaData<?, ?>[] getAdministratorMetaData() {
		return this.administratorMetaData;
	}

	@Override
	public ManagedFunctionMetaData<W, ?, ?>[] getTaskMetaData() {
		return this.taskMetaData;
	}

}