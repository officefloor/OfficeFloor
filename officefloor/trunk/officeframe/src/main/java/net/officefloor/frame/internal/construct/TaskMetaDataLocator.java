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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * Locates the {@link TaskMetaData}.
 * 
 * @author Daniel
 */
public interface TaskMetaDataLocator {

	/**
	 * Obtains the {@link OfficeMetaData} of the {@link Office} that
	 * {@link TaskMetaData} is being located within.
	 * 
	 * @return {@link OfficeMetaData} of the {@link Office} that
	 *         {@link TaskMetaData} is being located within.
	 */
	OfficeMetaData getOfficeMetaData();

	/**
	 * Obtains the default {@link WorkMetaData}.
	 * 
	 * @return Default {@link WorkMetaData}.
	 */
	WorkMetaData<?> getDefaultWorkMetaData();

	/**
	 * Creates a {@link TaskMetaDataLocator} that defaults to the input
	 * {@link WorkMetaData} if no {@link Work} name is provided.
	 * 
	 * @param workMetaData
	 *            {@link WorkMetaData} of the {@link Work} to default searching
	 *            for {@link TaskMetaData}.
	 * @return {@link TaskMetaDataLocator}.
	 */
	TaskMetaDataLocator createWorkSpecificTaskMetaDataLocator(
			WorkMetaData<?> workMetaData);

	/**
	 * Obtains the {@link TaskMetaData} by the {@link Work} and {@link Task}
	 * name.
	 * 
	 * @param workName
	 *            Name of the {@link Work} that the {@link Task} is on. If
	 *            <code>null</code> attempts to find {@link TaskMetaData} on the
	 *            default {@link WorkMetaData}.
	 * @param taskName
	 *            Name of the {@link Task}.
	 * @return {@link TaskMetaData} or <code>null</code> if not found.
	 */
	TaskMetaData<?, ?, ?, ?> getTaskMetaData(String workName, String taskName);

	/**
	 * Obtains the {@link TaskMetaData} on the default {@link WorkMetaData}. If
	 * no default {@link WorkMetaData} is available, this will always return
	 * <code>null</code>.
	 * 
	 * @param taskName
	 *            Name of the {@link Task} on the default {@link Work}.
	 * @return {@link TaskMetaData} or <code>null</code> if not found.
	 */
	TaskMetaData<?, ?, ?, ?> getTaskMetaData(String taskName);

}