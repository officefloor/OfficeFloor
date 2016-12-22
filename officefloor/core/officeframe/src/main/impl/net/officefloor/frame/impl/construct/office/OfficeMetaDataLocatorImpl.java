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
package net.officefloor.frame.impl.construct.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.construct.OfficeMetaDataLocator;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;

/**
 * {@link OfficeMetaDataLocator} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeMetaDataLocatorImpl implements OfficeMetaDataLocator {

	/**
	 * Listing of all {@link WorkMetaData} within the {@link Office}.
	 */
	private final WorkMetaData<?>[] allWorkMetaData;

	/**
	 * {@link WorkMetaData} to default to on locating {@link ManagedFunctionMetaData}.
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
	 * @param allWorkMetaData
	 *            Listing of all {@link WorkMetaData} within the {@link Office}.
	 */
	public OfficeMetaDataLocatorImpl(WorkMetaData<?>[] allWorkMetaData) {
		this(allWorkMetaData, null);
	}

	/**
	 * Initiate.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData} to find the {@link ManagedFunctionMetaData}
	 *            within.
	 * @param workMetaData
	 *            Listing of all {@link WorkMetaData} within the {@link Office}.
	 */
	private OfficeMetaDataLocatorImpl(WorkMetaData<?>[] allWorkMetaData, WorkMetaData<?> workMetaData) {
		this.allWorkMetaData = allWorkMetaData;
		this.workMetaData = workMetaData;

		// Load the work entries and specify default work entry
		WorkEntry defaultWork = null;
		for (WorkMetaData<?> work : this.allWorkMetaData) {

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
	 * ================== OfficeMetaDataLocator ===============================
	 */

	@Override
	public WorkMetaData<?> getDefaultWorkMetaData() {
		return this.workMetaData;
	}

	@Override
	public OfficeMetaDataLocator createWorkSpecificOfficeMetaDataLocator(WorkMetaData<?> workMetaData) {
		return new OfficeMetaDataLocatorImpl(this.allWorkMetaData, workMetaData);
	}

	@Override
	public WorkMetaData<?> getWorkMetaData(String workName) {
		WorkEntry workEntry = this.officeWork.get(workName);
		return (workEntry != null ? workEntry.workMetaData : null);
	}

	@Override
	public ManagedFunctionMetaData<?, ?, ?> getTaskMetaData(String workName, String taskName) {
		WorkEntry workEntry = (workName != null ? this.officeWork.get(workName) : this.defaultWorkEntry);
		return (workEntry != null ? workEntry.tasks.get(taskName) : null);
	}

	@Override
	public ManagedFunctionMetaData<?, ?, ?> getTaskMetaData(String taskName) {
		return (this.defaultWorkEntry != null ? this.defaultWorkEntry.tasks.get(taskName) : null);
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
		 * {@link ManagedFunctionMetaData} entries by their {@link ManagedFunction} names.
		 */
		public final Map<String, ManagedFunctionMetaData<?, ?, ?>> tasks = new HashMap<String, ManagedFunctionMetaData<?, ?, ?>>();

		/**
		 * Initiate.
		 * 
		 * @param workMetaData
		 *            {@link WorkMetaData}.
		 */
		public WorkEntry(WorkMetaData<?> workMetaData) {
			this.workMetaData = workMetaData;

			// Load the tasks
			for (ManagedFunctionMetaData<?, ?, ?> taskMetaData : workMetaData.getTaskMetaData()) {
				String taskName = taskMetaData.getFunctionName();
				this.tasks.put(taskName, taskMetaData);
			}
		}
	}

}