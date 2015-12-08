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
package net.officefloor.model.impl.desk;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;

/**
 * Tests setting the {@link Object} as parameter.
 * 
 * @author Daniel Sagenschneider
 */
public class SetObjectAsParameterTest extends AbstractDeskChangesTestCase {

	/**
	 * {@link Object} {@link WorkTaskObjectModel}.
	 */
	private WorkTaskObjectModel object;

	/**
	 * Parameter {@link WorkTaskObjectModel}.
	 */
	private WorkTaskObjectModel parameter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.compile.impl.desk.AbstractDeskOperationsTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the object parameter task object
		WorkTaskModel workTask = this.model.getWorks().get(0).getWorkTasks()
				.get(0);
		this.object = workTask.getTaskObjects().get(0);
		this.parameter = workTask.getTaskObjects().get(1);
	}

	/**
	 * Ensures no change if {@link WorkTaskModel} is not on the
	 * {@link DeskModel}.
	 */
	public void testWorkTaskNotOnDesk() {
		WorkTaskObjectModel taskObject = new WorkTaskObjectModel("NOT_ON_DESK",
				null, String.class.getName(), false);
		Change<WorkTaskObjectModel> change = this.operations
				.setObjectAsParameter(true, taskObject);
		this.assertChange(change, taskObject,
				"Set task object NOT_ON_DESK as a parameter", false,
				"Task object NOT_ON_DESK not on desk");
	}

	/**
	 * Ensures can set a {@link WorkTaskObjectModel} as a parameter.
	 */
	public void testSetToParameter() {
		Change<WorkTaskObjectModel> change = this.operations
				.setObjectAsParameter(true, this.object);
		this.assertChange(change, this.object,
				"Set task object OBJECT as a parameter", true);
	}

	/**
	 * Ensures can set a {@link WorkTaskObjectModel} as an object.
	 */
	public void testSetToObject() {
		Change<WorkTaskObjectModel> change = this.operations
				.setObjectAsParameter(false, this.parameter);
		this.assertChange(change, this.parameter,
				"Set task object PARAMETER as an object", true);
	}

	/**
	 * Ensures can set a {@link WorkTaskObjectModel} as a parameter with
	 * connected {@link ConnectionModel} instances.
	 */
	public void testSetToParameterWithConnections() {
		this.useTestSetupModel();
		Change<WorkTaskObjectModel> change = this.operations
				.setObjectAsParameter(true, this.object);
		this.assertChange(change, this.object,
				"Set task object OBJECT as a parameter", true);
	}

}