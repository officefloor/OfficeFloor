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

import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.section.SubSectionObjectModel;
import net.officefloor.model.section.SubSectionModel;
import net.officefloor.util.OFCU;

/**
 * Contains the various items on the line from a {@link WorkTaskObjectModel}
 * through to its {@link ManagedObjectSourceModel}.
 * 
 * @author Daniel
 */
public class ManagedObjectLine<W extends Work> {

	/**
	 * {@link WorkTaskObjectModel}.
	 */
	public final WorkTaskObjectModel deskTaskObject;

	/**
	 * {@link WorkTaskModel}.
	 */
	public final WorkTaskModel deskTask;

	/**
	 * {@link DeskModel} {@link ExternalManagedObjectModel}.
	 */
	public final ExternalManagedObjectModel deskExternalManagedObject;

	/**
	 * {@link WorkEntry}.
	 */
	public final WorkEntry<W> workEntry;

	/**
	 * {@link DeskEntry}.
	 */
	public final DeskEntry deskEntry;

	/**
	 * {@link OfficeEntry}.
	 */
	public final OfficeEntry officeEntry;

	/**
	 * {@link net.officefloor.model.office.ExternalManagedObjectModel}.
	 */
	public final net.officefloor.model.office.ExternalManagedObjectModel officeExternalManagedObject;

	/**
	 * {@link OfficeFloorOfficeModel}.
	 */
	public final OfficeFloorOfficeModel officeFloorOffice;

	/**
	 * {@link OfficeFloorEntry}.
	 */
	public final OfficeFloorEntry officeFloorEntry;

	/**
	 * {@link OfficeManagedObjectModel}.
	 */
	public final OfficeManagedObjectModel officeFloorOfficeManagedObject;

	/**
	 * {@link ManagedObjectSourceModel}.
	 */
	public final ManagedObjectSourceModel managedObjectSource;

	/**
	 * Initiate the line from {@link WorkTaskObjectModel}.
	 * 
	 * @param deskTaskObject
	 *            {@link WorkTaskObjectModel}.
	 * @param taskEntry
	 *            {@link TaskEntry} for the {@link WorkTaskObjectModel}.
	 * @throws Exception
	 *             If fails to create the line.
	 */
	public ManagedObjectLine(WorkTaskObjectModel deskTaskObject,
			WorkEntry<W> workEntry) throws Exception {

		// Store starting point
		this.deskTaskObject = deskTaskObject;
		this.workEntry = workEntry;

		// Do generate line for parameters
		if (this.deskTaskObject.getIsParameter()) {
			throw new TODOException("Can not create "
					+ this.getClass().getSimpleName() + " for a parameter");
		}

		// Obtain the desk entry
		this.deskEntry = this.workEntry.getDeskEntry();

		// Obtain the desk task for task object
		WorkTaskModel deskTask = null;
		for (WorkTaskModel task : this.workEntry.getModel().getWorkTasks()) {
			for (WorkTaskObjectModel object : task.getTaskObjects()) {
				if (object == this.deskTaskObject) {
					deskTask = task;
				}
			}
		}
		if (deskTask == null) {
			throw new Exception("Desk task object "
					+ this.deskTaskObject.getObjectType() + " not on work "
					+ this.workEntry.getId() + " of desk "
					+ this.deskEntry.getId());
		}
		this.deskTask = deskTask;

		// Obtain the corresponding desk external managed object
		this.deskExternalManagedObject = OFCU.get(
				this.deskTaskObject.getExternalManagedObject(),
				"No managed object for work ${0} task ${1} object ${2}",
				this.workEntry.getId(), this.deskTask.getWorkTaskName(),
				this.deskTaskObject.getObjectType()).getExternalManagedObject();

		// Obtain the external managed object name
		String externalMoName = this.deskExternalManagedObject.getExternalManagedObjectName();

		// Obtain the room containing the desk
		RoomEntry roomEntry = this.deskEntry.getParentRoom();

		// Obtain the desk sub room
		SubSectionModel subRoom = roomEntry.getSubRoom(this.deskEntry);

		// Obtain the office external managed object
		OfficeEntry officeEntry = null;
		while (roomEntry != null) {

			// Obtain the external managed object name on the desk sub room
			SubSectionObjectModel subRoomMo = roomEntry
					.getSubRoomManagedObject(subRoom, externalMoName);

			// Obtain the external managed object name for the room
			externalMoName = subRoomMo.getExternalManagedObject()
					.getExternalManagedObject().getExternalManagedObjectName();

			// Obtain parent room of room or office
			officeEntry = roomEntry.getOffice(); // before parent
			roomEntry = roomEntry.getParentRoom();
		}

		// Should now have the office entry
		this.officeEntry = officeEntry;

		// Obtain the office external managed object
		net.officefloor.model.office.ExternalManagedObjectModel officeExtMo = null;
		for (net.officefloor.model.office.ExternalManagedObjectModel mo : this.officeEntry
				.getModel().getExternalManagedObjects()) {
			if (externalMoName.equals(mo.getName())) {
				officeExtMo = mo;
			}
		}
		if (officeExtMo == null) {
			throw new Exception("Can not find external managed object '"
					+ externalMoName + "' of office " + officeEntry.getId());
		}
		this.officeExternalManagedObject = officeExtMo;

		// Obtain the office within the office floor
		this.officeFloorEntry = this.officeEntry.getOfficeFloorEntry();
		this.officeFloorOffice = officeFloorEntry
				.getOfficeFloorOfficeModel(officeEntry);

		// Obtain office floor managed object
		OfficeManagedObjectModel officeFloorOfficeMo = null;
		for (OfficeManagedObjectModel mo : this.officeFloorOffice
				.getManagedObjects()) {
			if (this.officeExternalManagedObject.getName().equals(
					mo.getManagedObjectName())) {
				officeFloorOfficeMo = mo;
			}
		}
		if (officeFloorOfficeMo == null) {
			throw new Exception("Can not find managed object '"
					+ externalMoName + "' for office "
					+ this.officeFloorOffice.getId());
		}
		this.officeFloorOfficeManagedObject = officeFloorOfficeMo;

		// Obtain the managed object source model
		this.managedObjectSource = this.officeFloorOfficeManagedObject
				.getManagedObjectSource().getManagedObjectSource();
	}

}
