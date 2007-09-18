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
package net.officefloor.frame.impl.execute;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.structure.WorkMetaData}.
 * 
 * @author Daniel
 */
public class WorkMetaDataImpl<W extends Work> implements WorkMetaData<W> {

	/**
	 * Unique ID to identify this {@link Work} type.
	 */
	protected final int workId;

	/**
	 * {@link WorkFactory}.
	 */
	protected final WorkFactory<W> workFactory;

	/**
	 * Meta-data of the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances.
	 */
	protected final ManagedObjectMetaData[] managedObjectMetaData;

	/**
	 * Meta-data of the
	 * {@link net.officefloor.frame.spi.administration.Administrator} instances.
	 */
	protected final AdministratorMetaData[] administratorMetaData;

	/**
	 * {@link FlowMetaData} for the initial
	 * {@link net.officefloor.frame.internal.structure.Flow} of the {@link Work}.
	 */
	protected final FlowMetaData<W> initialFlowMetaData;

	/**
	 * Initiate.
	 * 
	 * @param workId
	 *            Unique ID to identify this {@link Work} type.
	 * @param workFactory
	 *            {@link WorkFactory}.
	 * @param moMetaData
	 *            Listing of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            instances.
	 * @param adminMetaData
	 *            Listing of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}
	 *            instances.
	 * @param initialFlowMetaData
	 *            {@link FlowMetaData} for the initial
	 *            {@link net.officefloor.frame.internal.structure.Flow} of the
	 *            {@link Work}.
	 */
	public WorkMetaDataImpl(int workId, WorkFactory<W> workFactory,
			ManagedObjectMetaData[] moMetaData,
			AdministratorMetaData[] adminMetaData,
			FlowMetaData<W> initialFlowMetaData) {
		this.workId = workId;
		this.workFactory = workFactory;
		this.managedObjectMetaData = moMetaData;
		this.administratorMetaData = adminMetaData;
		this.initialFlowMetaData = initialFlowMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.WorkMetaData#getWorkId()
	 */
	public int getWorkId() {
		return this.workId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.WorkMetaData#getWorkFactory()
	 */
	public WorkFactory<W> getWorkFactory() {
		return this.workFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.WorkMetaData#getInitialTaskMetaData()
	 */
	public FlowMetaData<W> getInitialFlowMetaData() {
		return this.initialFlowMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.WorkMetaData#getManagedObjectMetaData()
	 */
	public ManagedObjectMetaData[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.WorkMetaData#getAdministratorMetaData()
	 */
	public AdministratorMetaData[] getAdministratorMetaData() {
		return this.administratorMetaData;
	}

}
