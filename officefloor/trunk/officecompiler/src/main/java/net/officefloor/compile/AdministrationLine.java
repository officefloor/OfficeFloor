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
package net.officefloor.compile;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorToManagedObjectModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.FlowItemToPostAdministratorDutyModel;
import net.officefloor.model.office.FlowItemToPreAdministratorDutyModel;
import net.officefloor.util.OFCU;

/**
 * Creates the line from the {@link TaskModel} through to its
 * {@link AdministratorSource}.
 * 
 * @author Daniel
 */
public class AdministrationLine<W extends Work> {

	/**
	 * Creates the {@link AdministrationLine} instances for the
	 * {@link TaskModel} of the input {@link TaskLine}.
	 * 
	 * @param taskLine
	 *            {@link TaskLine} of the {@link TaskModel}.
	 * @return Listing of {@link AdministrationLine} instances.
	 * @throws Exception
	 *             If fails to create the lines.
	 */
	@SuppressWarnings("unchecked")
	public static <W extends Work> AdministrationLine<W>[] createAdministrationLines(
			TaskLine<W> taskLine) throws Exception {

		// Create listing to populate
		List<AdministrationLine<W>> adminLines = new LinkedList<AdministrationLine<W>>();

		// Add the pre duties
		for (FlowItemToPreAdministratorDutyModel preTask : taskLine.officeFlowItem
				.getPreAdminDutys()) {
			DutyModel duty = preTask.getDuty();
			AdministrationLine<W> adminLine = createAdministrationLine(
					taskLine, duty, true);
			adminLines.add(adminLine);
		}

		// Add the post duties
		for (FlowItemToPostAdministratorDutyModel postTask : taskLine.officeFlowItem
				.getPostAdminDutys()) {
			DutyModel duty = postTask.getDuty();
			AdministrationLine<W> adminLine = createAdministrationLine(
					taskLine, duty, false);
			adminLines.add(adminLine);
		}

		// Return the listing of admin lines
		return adminLines.toArray(new AdministrationLine[0]);
	}

	/**
	 * Creates the {@link AdministrationLine} for the {@link DutyModel} on the
	 * {@link TaskLine}.
	 * 
	 * @param taskLine
	 *            {@link TaskLine}.
	 * @param duty
	 *            {@link DutyModel}.
	 * @param isPreNotPost
	 *            Flag whether is pre/post administration.
	 * @return {@link AdministrationLine}.
	 * @throws Exception
	 *             If fails to create {@link AdministrationLine}.
	 */
	@SuppressWarnings("unchecked")
	private static <W extends Work> AdministrationLine<W> createAdministrationLine(
			TaskLine<W> taskLine, DutyModel duty, boolean isPreNotPost)
			throws Exception {

		// Obtain the administrator for the duty
		AdministratorEntry<?> adminEntry = taskLine.officeEntry
				.getAdministrator(duty);

		// Obtain the corresponding duty key
		Enum<?> dutyKey = OFCU.getEnum(adminEntry.getDutyKeys(), duty.getKey());

		// Create the mapping of office managed object to task managed objects
		Map<ExternalManagedObjectModel, ManagedObjectLine<W>> officeMoToMoLine = new HashMap<ExternalManagedObjectModel, ManagedObjectLine<W>>();
		for (WorkTaskObjectModel taskObject : taskLine.deskTask.getTaskObjects()) {
			// Ignore parameters
			if (taskObject.getIsParameter()) {
				continue;
			}

			// Create and add the managed object line for the task object
			ManagedObjectLine<W> moLine = new ManagedObjectLine<W>(taskObject,
					taskLine.workEntry);
			officeMoToMoLine.put(moLine.officeExternalManagedObject, moLine);
		}

		// Create the mapping of office managed object to work managed objects.
		// Note duplicates are ignored to obtain the work bound managed objects
		// as interest only to determine if bound to work.
		Map<ExternalManagedObjectModel, ManagedObjectLine<W>> officeMoToWorkMo = new HashMap<ExternalManagedObjectModel, ManagedObjectLine<W>>();
		for (WorkTaskModel deskTask : taskLine.workEntry.getModel().getWorkTasks()) {
			for (WorkTaskObjectModel taskObject : deskTask.getTaskObjects()) {
				// Ignore parameters
				if (taskObject.getIsParameter()) {
					continue;
				}

				// Create and add the managed object line for the work
				ManagedObjectLine<W> moLine = new ManagedObjectLine<W>(
						taskObject, taskLine.workEntry);
				officeMoToWorkMo
						.put(moLine.officeExternalManagedObject, moLine);
			}
		}

		// Create the listing of managed objects under administration
		List<ManagedObjectUnderAdministration<W>> managedObjects = new LinkedList<ManagedObjectUnderAdministration<W>>();
		for (AdministratorToManagedObjectModel adminToMo : adminEntry
				.getModel().getManagedObjects()) {
			ExternalManagedObjectModel officeMo = adminToMo.getManagedObject();

			// Attempt to obtain the managed object lines
			ManagedObjectLine<W> taskMoLine = officeMoToMoLine.get(officeMo);
			ManagedObjectLine<W> workMoLine = officeMoToWorkMo.get(officeMo);

			// Create the managed object under administration
			ManagedObjectUnderAdministration<W> managedObject;
			if (taskMoLine != null) {
				// Managed object being used by the task
				managedObject = new ManagedObjectUnderAdministration<W>(
						officeMo, taskMoLine.deskExternalManagedObject,
						taskMoLine.deskTaskObject);

			} else if (workMoLine != null) {
				// Managed object not used by task but used by work
				managedObject = new ManagedObjectUnderAdministration<W>(
						officeMo, workMoLine.deskExternalManagedObject, null);

			} else {
				// Managed object not used by work
				managedObject = new ManagedObjectUnderAdministration<W>(
						officeMo, null, null);
			}

			// Add to listing of managed objects
			managedObjects.add(managedObject);
		}

		// Return the administration line
		return new AdministrationLine<W>(
				taskLine,
				duty,
				dutyKey,
				isPreNotPost,
				managedObjects.toArray(new ManagedObjectUnderAdministration[0]),
				adminEntry, adminEntry.getModel());
	}

	/**
	 * {@link TaskLine} containing the details of the {@link TaskModel}.
	 */
	public final TaskLine<W> taskLine;

	/**
	 * {@link DutyModel} of this administration.
	 */
	public final DutyModel duty;

	/**
	 * Key for the {@link Duty}.
	 */
	public final Enum<?> dutyKey;

	/**
	 * Flags whether is a pre/post administration. <code>true</code> indicates
	 * pre.
	 */
	public final boolean isPreNotPost;

	/**
	 * Obtains the listing {@link ManagedObjectUnderAdministration} instances.
	 */
	public final ManagedObjectUnderAdministration<W>[] managedObjects;

	/**
	 * {@link AdministratorEntry}.
	 */
	public final AdministratorEntry<?> administratorEntry;

	/**
	 * {@link AdministratorModel}.
	 */
	public final AdministratorModel administrator;

	/**
	 * Initiate.
	 * 
	 * @param taskLine
	 *            {@link TaskLine} of the {@link TaskModel}.
	 * @param duty
	 *            {@link DutyModel}.
	 * @param dutyKey
	 *            {@link Duty} key.
	 * @param isPreNotPost
	 *            Flag whether pre/post administration.
	 * @param managedObjects
	 *            Listing of {@link ManagedObjectUnderAdministration} instances.
	 * @param administratorEntry
	 *            {@link AdministratorEntry}.
	 * @param administrator
	 *            {@link AdministratorModel}.
	 */
	private AdministrationLine(TaskLine<W> taskLine, DutyModel duty,
			Enum<?> dutyKey, boolean isPreNotPost,
			ManagedObjectUnderAdministration<W>[] managedObjects,
			AdministratorEntry<?> administratorEntry,
			AdministratorModel administrator) {
		this.taskLine = taskLine;
		this.duty = duty;
		this.dutyKey = dutyKey;
		this.isPreNotPost = isPreNotPost;
		this.managedObjects = managedObjects;
		this.administratorEntry = administratorEntry;
		this.administrator = administrator;
	}

	/**
	 * {@link ManagedObject} under administration.
	 */
	public static class ManagedObjectUnderAdministration<W extends Work> {

		/**
		 * {@link ExternalManagedObjectModel} under administration.
		 */
		public final ExternalManagedObjectModel officeManagedObject;

		/**
		 * {@link net.officefloor.model.desk.ExternalManagedObjectModel} if
		 * {@link ManagedObject} being used by the {@link Work}. May be
		 * <code>null</code>.
		 */
		public final net.officefloor.model.desk.ExternalManagedObjectModel deskManagedObject;

		/**
		 * {@link WorkTaskObjectModel} if {@link ManagedObject} being used by
		 * the {@link Task}. May be <code>null</code>.
		 */
		public final WorkTaskObjectModel deskTaskObject;

		/**
		 * Initiate.
		 * 
		 * @param officeManagedObject
		 *            {@link ExternalManagedObjectModel}.
		 * @param deskManagedObject
		 *            {@link net.officefloor.model.desk.ExternalManagedObjectModel}.
		 * @param deskTaskObject
		 *            {@link WorkTaskObjectModel}.
		 */
		private ManagedObjectUnderAdministration(
				ExternalManagedObjectModel officeManagedObject,
				net.officefloor.model.desk.ExternalManagedObjectModel deskManagedObject,
				WorkTaskObjectModel deskTaskObject) {
			this.officeManagedObject = officeManagedObject;
			this.deskManagedObject = deskManagedObject;
			this.deskTaskObject = deskTaskObject;
		}
	}

}
