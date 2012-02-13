/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;

/**
 * Meta-data of a {@link JobSequence}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FlowMetaData<W extends Work> {

	/**
	 * Obtains the {@link FlowInstigationStrategyEnum} for the {@link JobSequence}.
	 * 
	 * @return {@link FlowInstigationStrategyEnum} for the {@link JobSequence}.
	 */
	FlowInstigationStrategyEnum getInstigationStrategy();

	/**
	 * Obtains the {@link TaskMetaData} of the initial {@link Task} within the
	 * {@link JobSequence}.
	 * 
	 * @return {@link TaskMetaData} of the initial {@link Task} within the
	 *         {@link JobSequence}.
	 */
	TaskMetaData<W, ?, ?> getInitialTaskMetaData();

	/**
	 * Obtains the {@link AssetManager} to managed this {@link JobSequence}.
	 * 
	 * @return {@link AssetManager} to managed this {@link JobSequence}.
	 */
	AssetManager getFlowManager();

}