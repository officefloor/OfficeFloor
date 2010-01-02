/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * Raw meta-data for a {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawTaskMetaData<W extends Work, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Task}.
	 * 
	 * @return Name of the {@link Task}.
	 */
	String getTaskName();

	/**
	 * Obtains the {@link RawWorkMetaData} of the {@link Work} containing this
	 * {@link Task}.
	 * 
	 * @return {@link RawWorkMetaData}.
	 */
	RawWorkMetaData<W> getRawWorkMetaData();

	/**
	 * Obtains the {@link TaskMetaData}.
	 * 
	 * @return {@link TaskMetaData}.
	 */
	TaskMetaData<W, D, F> getTaskMetaData();

	/**
	 * Links the {@link TaskMetaData} instances to create {@link Flow} of
	 * execution.
	 * 
	 * @param taskMetaDataLocator
	 *            {@link OfficeMetaDataLocator}.
	 * @param workMetaData
	 *            {@link WorkMetaData} containing this {@link TaskMetaData}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory} to create the {@link AssetManager}
	 *            instances that manage {@link Flow} instances.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void linkTasks(OfficeMetaDataLocator taskMetaDataLocator,
			WorkMetaData<W> workMetaData,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues);

}