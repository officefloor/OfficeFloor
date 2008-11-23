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
package net.officefloor.eclipse.office.operations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.ClasspathFilter;
import net.officefloor.eclipse.common.dialog.input.filter.FileExtensionInputFilter;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.common.dialog.input.translator.ResourceFullPathValueTranslator;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.office.editparts.OfficeEditPart;
import net.officefloor.eclipse.office.editparts.RoomEditPart;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.office.OfficeLoader;
import net.officefloor.office.RoomToOfficeRoomSynchroniser;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.room.RoomLoader;

import org.eclipse.core.resources.IFile;

/**
 * Manages the {@link OfficeRoomModel} on the {@link OfficeModel}.
 * 
 * @author Daniel
 */
public class ManageOfficeRoomOperation extends
		AbstractOperation<AbstractOfficeFloorEditPart<?, ?>> {

	/**
	 * {@link OfficeEditPart}.
	 */
	private final OfficeEditPart officeEditPart;

	/**
	 * Initiate.
	 * 
	 * @param officeEditPart
	 *            {@link OfficeEditPart}.
	 */
	public ManageOfficeRoomOperation(OfficeEditPart officeEditPart) {
		super(null, null);
		this.officeEditPart = officeEditPart;
	}

	/**
	 * Indicates if {@link OfficeRoomModel} is added to the {@link OfficeModel}.
	 * 
	 * @return <code>true</code> if {@link OfficeRoomModel} is added to the
	 *         {@link OfficeModel}.
	 */
	protected boolean isRoomAdded() {

		// Obtain the office
		OfficeModel office = this.officeEditPart.getCastedModel();

		// Return whether room was added
		return (office.getRoom() != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.action.Operation#getActionText()
	 */
	@Override
	public String getActionText() {
		return (this.isRoomAdded() ? "Remove room" : "Add room");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.action.Operation#getEditPartTypes()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class[] getEditPartTypes() {
		// Focus operation based on whether room added
		return new Class[] { (this.isRoomAdded() ? RoomEditPart.class
				: OfficeEditPart.class) };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {
		if (this.isRoomAdded()) {
			this.removeOfficeRoom(context);
		} else {
			this.addOfficeRoom(context);
		}
	}

	/**
	 * Adds the {@link OfficeRoomModel}.
	 * 
	 * @param context
	 *            {@link Context}.
	 */
	protected void addOfficeRoom(Context context) {

		// Obtain the office edit part
		final OfficeEditPart editPart = (OfficeEditPart) context.getEditPart();

		// Add the room
		RoomModel rawRoom = null;
		OfficeRoomModel room = new OfficeRoomModel();
		BeanDialog dialog = editPart.createBeanDialog(room, "Name", "X", "Y");
		dialog.registerPropertyInput("Id", new ClasspathSelectionInput(editPart
				.getEditor(), new ClasspathFilter(IFile.class,
				new FileExtensionInputFilter("room"))));
		dialog.registerPropertyValueTranslator("Id",
				new ResourceFullPathValueTranslator());
		if (dialog.populate()) {
			try {
				// Obtain the class path location
				String classPathLocation = ClasspathUtil
						.getClassPathLocation(room.getId());

				// Obtain the room configuration
				ProjectClassLoader classLoader = ProjectClassLoader
						.create(editPart.getEditor());
				ConfigurationItem roomConfigItem = classLoader
						.findConfigurationItem(classPathLocation);
				if (roomConfigItem == null) {
					editPart.messageError("Could not find Office Room at '"
							+ classPathLocation + "'");
					return;
				}

				// Load the room
				RoomLoader roomLoader = new RoomLoader();
				rawRoom = roomLoader.loadRoom(roomConfigItem);

				// Load the office room
				OfficeLoader officeLoader = new OfficeLoader();
				room = officeLoader.loadOfficeRoom(roomConfigItem.getId(),
						"OFFICE ROOM", rawRoom, roomConfigItem.getContext(),
						classLoader);

			} catch (Exception ex) {
				editPart.messageError(ex);
			}
		}

		// Ensure created
		if (room == null) {
			return;
		}

		// Set location
		context.positionModel(room);

		// Make changes
		final OfficeRoomModel newRoom = room;
		final RoomModel selectedRoom = rawRoom;
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {

				// Obtain the office model
				OfficeModel office = editPart.getCastedModel();

				// Synchronise the room onto the office
				RoomToOfficeRoomSynchroniser.synchroniseRoomOntoOffice(
						selectedRoom, newRoom, office);
			}

			@Override
			protected void undoCommand() {
				OfficeModel office = editPart.getCastedModel();

				// Remove the office room
				office.setRoom(null);

				// Remove the managed objects
				List<ExternalManagedObjectModel> managedObjects = new ArrayList<ExternalManagedObjectModel>(
						office.getExternalManagedObjects());
				for (ExternalManagedObjectModel managedObject : managedObjects) {
					office.removeExternalManagedObject(managedObject);
				}
			}
		});
	}

	/**
	 * Removes the {@link OfficeRoomModel}.
	 * 
	 * @param context
	 *            {@link Context}.
	 */
	protected void removeOfficeRoom(Context context) {

		// Obtain the office
		final OfficeModel office = this.officeEditPart.getCastedModel();

		// Obtain the office room
		final OfficeRoomModel room = office.getRoom();

		// Obtain the managed objects
		final List<ExternalManagedObjectModel> managedObjects = new ArrayList<ExternalManagedObjectModel>(
				office.getExternalManagedObjects());

		// Connections
		final List<ConnectionModel> connections = new LinkedList<ConnectionModel>();

		// Team connections
		for (ExternalTeamModel team : office.getExternalTeams()) {
			connections.addAll(team.getFlowItems());
		}

		// Administrator connections
		for (AdministratorModel admin : office.getAdministrators()) {
			connections.addAll(admin.getManagedObjects());
			ConnectionModel teamConn = admin.getTeam();
			if (teamConn != null) {
				connections.add(teamConn);
			}
		}

		// Make change
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				// Remove connections
				for (ConnectionModel connection : connections) {
					connection.remove();
				}

				// Remove room and managed objects
				for (ExternalManagedObjectModel managedObject : managedObjects) {
					office.removeExternalManagedObject(managedObject);
				}
				office.setRoom(null);
			}

			@Override
			protected void undoCommand() {
				// Add room and managed objects
				office.setRoom(room);
				for (ExternalManagedObjectModel managedObject : managedObjects) {
					office.addExternalManagedObject(managedObject);
				}

				// Add connections
				for (ConnectionModel connection : connections) {
					connection.connect();
				}
			}
		});
	}
}
