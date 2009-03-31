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
package net.officefloor.compile.test.archive;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.WorkModel;

/**
 * Synchronises the {@link net.officefloor.model.WorkType.WorkModel} to a
 * {@link net.officefloor.model.desk.DeskWorkModel}.
 * 
 * @author Daniel
 */
//Provide do/undo synchronise
@Deprecated
public class WorkToDeskWorkSynchroniser {

	/**
	 * Synchronise {@link WorkType} onto the {@link DeskWorkModel}.
	 * 
	 * @param work
	 *            {@link WorkType}.
	 * @param deskWork
	 *            {@link DeskWorkModel}.
	 */
	public static void synchroniseWorkOntoDeskWork(WorkType<?> work,
			DeskWorkModel deskWork) {

		// Load work onto the desk work
		deskWork.setWork(work);

		// Create the set task names
		Set<String> taskNames = new HashSet<String>();
		for (TaskModel<?, ?> task : work.getTasks()) {
			taskNames.add(task.getTaskName());
		}

		// Remove any tasks no longer existing
		List<DeskTaskModel> tasksToRemove = new LinkedList<DeskTaskModel>();
		for (DeskTaskModel deskTask : deskWork.getTasks()) {
			if (!taskNames.contains(deskTask.getName())) {
				tasksToRemove.add(deskTask);
			}
		}
		for (DeskTaskModel deskTask : tasksToRemove) {
			// Remove connections and task
			deskTask.removeConnections();
			deskWork.removeTask(deskTask);
		}

		// Synchronise the tasks
		for (TaskModel<?, ?> task : work.getTasks()) {

			// Obtain the desk task
			DeskTaskModel deskTask = null;
			for (DeskTaskModel model : deskWork.getTasks()) {
				if (task.getTaskName().equals(model.getName())) {
					deskTask = model;
				}
			}

			// Load the task
			if (deskTask == null) {
				// Missing therefore add
				deskTask = new DeskTaskModel(task.getTaskName(), task, null,
						null);
				deskWork.addTask(deskTask);
			} else {
				deskTask.setTask(task);
			}

			// Synchronise task onto desk task
			TaskToDeskTaskSynchroniser.synchroniseTaskOntoDeskTask(task,
					deskTask);
		}
	}

	/**
	 * All access via static methods.
	 */
	private WorkToDeskWorkSynchroniser() {
	}
}
