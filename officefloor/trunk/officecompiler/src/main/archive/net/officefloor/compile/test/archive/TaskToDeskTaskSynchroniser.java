/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.test.archive;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskObjectModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;

/**
 * Synchronises {@link net.officefloor.model.work.TaskModel} to the
 * {@link net.officefloor.model.desk.DeskTaskModel}.
 * 
 * @author Daniel Sagenschneider
 */
//Provide do/undo synchronise
@Deprecated
public class TaskToDeskTaskSynchroniser {

	/**
	 * Synchronise {@link TaskModel} onto the {@link DeskTaskModel}.
	 * 
	 * @param task
	 *            {@link TaskModel}.
	 * @param deskTask
	 *            {@link DeskTaskModel}.
	 */
	public static void synchroniseTaskOntoDeskTask(TaskModel<?, ?> task,
			DeskTaskModel deskTask) {
		// Link Task to Desk Task
		deskTask.setTask(task);

		// Clear object links
		for (DeskTaskObjectModel model : deskTask.getObjects()) {
			model.setTaskObject(null);
		}

		// Load objects onto target
		for (TaskObjectModel<?> object : task.getObjects()) {
			
			// Find first matching desk object
			DeskTaskObjectModel deskObject = null;
			for (DeskTaskObjectModel model : deskTask.getObjects()) {
				
				// Ensure desk task object not already linked to object
				if (model.getTaskObject() != null) {
					continue;
				}
				
				// Match to object type (or first null)
				String taskObjectType = object.getObjectType();
				if ((taskObjectType == null)
						|| (taskObjectType.equals(model.getObjectType()))) {
					deskObject = model;
					break;
				}
			}

			// Load the object
			if (deskObject == null) {
				// No matching therefore add
				deskTask.addObject(new DeskTaskObjectModel(object
						.getObjectType(), false, object, null));
			} else {
				// Load onto matching
				deskObject.setTaskObject(object);
			}
		}

		// Remove unused
		List<DeskTaskObjectModel> unused = new LinkedList<DeskTaskObjectModel>();
		for (DeskTaskObjectModel model : deskTask.getObjects()) {
			if (model.getTaskObject() == null) {
				unused.add(model);
			}
		}
		for (DeskTaskObjectModel model : unused) {
			// Remove connections as task
			model.removeConnections();
			deskTask.removeObject(model);
		}
	}

	/**
	 * All access via static methods.
	 */
	private TaskToDeskTaskSynchroniser() {
	}
}
