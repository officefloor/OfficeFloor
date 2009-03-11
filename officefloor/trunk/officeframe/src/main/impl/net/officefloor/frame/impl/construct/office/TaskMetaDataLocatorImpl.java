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
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement TaskMetaDataLocator.getWorkMetaData");
	}

	@Override
	public TaskMetaData<?, ?, ?, ?> getTaskMetaData(String workName,
			String taskName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement TaskMetaDataLocator.getTaskMetaData");
	}

	@Override
	public TaskMetaData<?, ?, ?, ?> getTaskMetaData(String taskName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement TaskMetaDataLocator.getTaskMetaData");
	}

}