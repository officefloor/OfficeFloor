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
package net.officefloor.frame.impl.execute.flow;

import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.TaskMetaData;

/**
 * Implementation of the
 * {@link net.officefloor.frame.internal.structure.FlowMetaData}.
 * 
 * @author Daniel
 */
public class FlowMetaDataImpl<W extends Work> implements FlowMetaData<W> {

	/**
	 * {@link FlowInstigationStrategyEnum}.
	 */
	protected final FlowInstigationStrategyEnum strategy;

	/**
	 * {@link TaskMetaData} of the initial
	 * {@link net.officefloor.frame.api.execute.Task} of the
	 * {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	protected final TaskMetaData<?, W, ?, ?> initialTaskMetaData;

	/**
	 * {@link AssetManager} to managed this
	 * {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	protected final AssetManager flowManager;

	/**
	 * Initiate.
	 * 
	 * @param strategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @param initialTaskMetaData
	 *            {@link TaskMetaData} of the initial
	 *            {@link net.officefloor.frame.api.execute.Task} of the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 * @param flowManager
	 *            {@link AssetManager} to managed this
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	public FlowMetaDataImpl(FlowInstigationStrategyEnum strategy,
			TaskMetaData<?, W, ?, ?> initialTaskMetaData,
			AssetManager flowManager) {
		this.strategy = strategy;
		this.initialTaskMetaData = initialTaskMetaData;
		this.flowManager = flowManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.FlowMetaData#getInstigationStrategy()
	 */
	public FlowInstigationStrategyEnum getInstigationStrategy() {
		return this.strategy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.FlowMetaData#getInitialTaskMetaData()
	 */
	public TaskMetaData<?, W, ?, ?> getInitialTaskMetaData() {
		return this.initialTaskMetaData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.FlowMetaData#getFlowManager()
	 */
	public AssetManager getFlowManager() {
		return this.flowManager;
	}

}
