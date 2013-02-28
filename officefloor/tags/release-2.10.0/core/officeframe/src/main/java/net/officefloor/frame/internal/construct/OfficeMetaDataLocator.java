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
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * Locates the meta-data within the {@link OfficeMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeMetaDataLocator {

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
	 * Creates a {@link OfficeMetaDataLocator} that defaults to the input
	 * {@link WorkMetaData} if no {@link Work} name is provided.
	 * 
	 * @param workMetaData
	 *            {@link WorkMetaData} of the {@link Work} to default searching
	 *            for {@link TaskMetaData}.
	 * @return {@link OfficeMetaDataLocator}.
	 */
	OfficeMetaDataLocator createWorkSpecificOfficeMetaDataLocator(
			WorkMetaData<?> workMetaData);

	/**
	 * Obtains the {@link WorkMetaData} by the {@link Work} name.
	 * 
	 * @param workName
	 *            Name of the {@link Work}.
	 * @return {@link WorkMetaData} or <code>null</code> if not found.
	 */
	WorkMetaData<?> getWorkMetaData(String workName);

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
	TaskMetaData<?, ?, ?> getTaskMetaData(String workName, String taskName);

	/**
	 * Obtains the {@link TaskMetaData} on the default {@link WorkMetaData}. If
	 * no default {@link WorkMetaData} is available, this will always return
	 * <code>null</code>.
	 * 
	 * @param taskName
	 *            Name of the {@link Task} on the default {@link Work}.
	 * @return {@link TaskMetaData} or <code>null</code> if not found.
	 */
	TaskMetaData<?, ?, ?> getTaskMetaData(String taskName);

}