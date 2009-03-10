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
 * @author Daniel
 */
public interface RawTaskMetaData<P, W extends Work, M extends Enum<M>, F extends Enum<F>> {

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
	TaskMetaData<P, W, M, F> getTaskMetaData();

	/**
	 * Links the {@link TaskMetaData} instances to create {@link Flow} of
	 * execution.
	 * 
	 * @param taskMetaDataLocator
	 *            {@link TaskMetaDataLocator}.
	 * @param workMetaData
	 *            {@link WorkMetaData} containing this {@link TaskMetaData}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory} to create the {@link AssetManager}
	 *            instances that manage {@link Flow} instances.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void linkTasks(TaskMetaDataLocator taskMetaDataLocator,
			WorkMetaData<W> workMetaData,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues);

}