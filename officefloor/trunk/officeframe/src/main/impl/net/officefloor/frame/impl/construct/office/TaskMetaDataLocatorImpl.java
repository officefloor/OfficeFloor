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
package net.officefloor.frame.impl.construct.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.construct.TaskMetaDataLocator;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * {@link TaskMetaDataLocator} implementation.
 * 
 * @author Daniel
 */
public class TaskMetaDataLocatorImpl implements TaskMetaDataLocator {

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * {@link WorkMetaData} to default to on locating {@link TaskMetaData}.
	 */
	private final WorkMetaData<?> workMetaData;

	/**
	 * {@link WorkEntry} instances of the {@link Office}.
	 */
	private final Map<String, WorkEntry> officeWork = new HashMap<String, WorkEntry>();

	/**
	 * Default {@link WorkEntry}.
	 */
	private final WorkEntry defaultWorkEntry;

	/**
	 * Initiate.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData} to find the {@link TaskMetaData}
	 *            within.
	 */
	public TaskMetaDataLocatorImpl(OfficeMetaData officeMetaData) {
		this(officeMetaData, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData} to find the {@link TaskMetaData}
	 *            within.
	 * @param workMetaData
	 *            {@link WorkMetaData} to default to on locating
	 *            {@link TaskMetaData}.
	 */
	private TaskMetaDataLocatorImpl(OfficeMetaData officeMetaData,
			WorkMetaData<?> workMetaData) {
		this.officeMetaData = officeMetaData;
		this.workMetaData = workMetaData;

		// Load the work entries and specify default work entry
		WorkEntry defaultWork = null;
		for (WorkMetaData<?> work : officeMetaData.getWorkMetaData()) {

			// Create and register the work entry for the work
			String workName = work.getWorkName();
			WorkEntry workEntry = new WorkEntry(work);
			this.officeWork.put(workName, workEntry);

			// Determine if the default work entry
			if (work == this.workMetaData) {
				defaultWork = workEntry;
			}
		}
		this.defaultWorkEntry = defaultWork;
	}

	/*
	 * ================== TaskMetaDataLocator ===============================
	 */

	@Override
	public OfficeMetaData getOfficeMetaData() {
		return this.officeMetaData;
	}

	@Override
	public WorkMetaData<?> getDefaultWorkMetaData() {
		return this.workMetaData;
	}

	@Override
	public TaskMetaDataLocator createWorkSpecificTaskMetaDataLocator(
			WorkMetaData<?> workMetaData) {
		return new TaskMetaDataLocatorImpl(this.officeMetaData, workMetaData);
	}

	@Override
	public WorkMetaData<?> getWorkMetaData(String workName) {
		WorkEntry workEntry = this.officeWork.get(workName);
		return (workEntry != null ? workEntry.workMetaData : null);
	}

	@Override
	public TaskMetaData<?, ?, ?, ?> getTaskMetaData(String workName,
			String taskName) {
		WorkEntry workEntry = (workName != null ? this.officeWork.get(workName)
				: this.defaultWorkEntry);
		return (workEntry != null ? workEntry.tasks.get(taskName) : null);
	}

	@Override
	public TaskMetaData<?, ?, ?, ?> getTaskMetaData(String taskName) {
		return (this.defaultWorkEntry != null ? this.defaultWorkEntry.tasks
				.get(taskName) : null);
	}

	/**
	 * {@link WorkEntry}.
	 */
	private class WorkEntry {

		/**
		 * {@link WorkMetaData} for this {@link WorkEntry}.
		 */
		public final WorkMetaData<?> workMetaData;

		/**
		 * {@link TaskMetaData} entries by their {@link Task} names.
		 */
		public final Map<String, TaskMetaData<?, ?, ?, ?>> tasks = new HashMap<String, TaskMetaData<?, ?, ?, ?>>();

		/**
		 * Initiate.
		 * 
		 * @param workMetaData
		 *            {@link WorkMetaData}.
		 */
		public WorkEntry(WorkMetaData<?> workMetaData) {
			this.workMetaData = workMetaData;

			// Load the tasks
			for (TaskMetaData<?, ?, ?, ?> taskMetaData : workMetaData
					.getTaskMetaData()) {
				String taskName = taskMetaData.getTaskName();
				this.tasks.put(taskName, taskMetaData);
			}
		}
	}

}