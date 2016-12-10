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
package net.officefloor.frame.impl.execute.flow;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.TaskMetaData;

/**
 * Implementation of the {@link FlowMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowMetaDataImpl<W extends Work> implements FlowMetaData<W> {

	/**
	 * {@link FlowInstigationStrategyEnum}.
	 */
	private final FlowInstigationStrategyEnum strategy;

	/**
	 * {@link TaskMetaData} of the initial {@link Task} of the {@link JobSequence}.
	 */
	private final TaskMetaData<W, ?, ?> initialTaskMetaData;

	/**
	 * {@link AssetManager} to managed this {@link JobSequence}.
	 */
	private final AssetManager flowManager;

	/**
	 * Initiate.
	 * 
	 * @param strategy
	 *            {@link FlowInstigationStrategyEnum}.
	 * @param initialTaskMetaData
	 *            {@link TaskMetaData} of the initial {@link Task} of the
	 *            {@link JobSequence}.
	 * @param flowManager
	 *            {@link AssetManager} to managed this {@link JobSequence}.
	 */
	public FlowMetaDataImpl(FlowInstigationStrategyEnum strategy,
			TaskMetaData<W, ?, ?> initialTaskMetaData, AssetManager flowManager) {
		this.strategy = strategy;
		this.initialTaskMetaData = initialTaskMetaData;
		this.flowManager = flowManager;
	}

	/*
	 * =================== FlowMetaData ======================================
	 */

	@Override
	public FlowInstigationStrategyEnum getInstigationStrategy() {
		return this.strategy;
	}

	@Override
	public TaskMetaData<W, ?, ?> getInitialTaskMetaData() {
		return this.initialTaskMetaData;
	}

	@Override
	public AssetManager getFlowManager() {
		return this.flowManager;
	}

}