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
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.ClasspathFilter;
import net.officefloor.eclipse.common.dialog.input.ClasspathUtil;
import net.officefloor.eclipse.common.dialog.input.filter.FileExtensionInputFilter;
import net.officefloor.eclipse.common.dialog.input.impl.ClasspathSelectionInput;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.ButtonEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.common.wrap.OfficeFloorWrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingModel;
import net.officefloor.eclipse.desk.figure.SectionFigure;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.ExternalTeamModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeRoomModel;
import net.officefloor.model.office.OfficeModel.OfficeEvent;
import net.officefloor.model.room.RoomModel;
import net.officefloor.office.OfficeLoader;
import net.officefloor.office.RoomToOfficeRoomSynchroniser;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.room.RoomLoader;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Point;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.office.OfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeEditPart extends
		AbstractOfficeFloorDiagramEditPart<OfficeModel> {

	/**
	 * Adds a {@link AdministratorModel}.
	 */
	private WrappingModel<OfficeModel> addAdministrator;

	/**
	 * Listing of {@link net.officefloor.model.office.ExternalTeamModel}
	 * instances.
	 */
	private WrappingModel<OfficeModel> externalTeams;

	/**
	 * {@link net.officefloor.model.office.OfficeRoomModel}.
	 */
	private WrappingModel<OfficeModel> room;

	/**
	 * Listing of
	 * {@link net.officefloor.model.office.ExternalManagedObjectModel}
	 * instances.
	 */
	private WrappingModel<OfficeModel> externalManagedObjects;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#init()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void init() {

		// Button to add Administrator
		final ButtonEditPart addAdministratorButton = new ButtonEditPart(
				"Add administrator") {
			@Override
			protected void handleButtonClick() {
				// Add the Administrator
				AdministratorModel bean = new AdministratorModel();
				BeanDialog dialog = OfficeEditPart.this.createBeanDialog(bean,
						"getX", "getY");
				if (dialog.populate()) {
					try {

						// TODO Load the administrator

						// Add the administrator
						OfficeEditPart.this.getCastedModel().addAdministrator(
								bean);

					} catch (Exception ex) {
						OfficeEditPart.this.messageError(ex);
					}
				}
			}
		};

		// Add Administrator
		WrappingEditPart addAdministratorEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.add(addAdministratorButton);
			}
		};
		addAdministratorEditPart.setFigure(new FreeformWrapperFigure(
				new SectionFigure("Add administrator")));
		this.addAdministrator = new WrappingModel<OfficeModel>(this
				.getCastedModel(), addAdministratorEditPart, new Point(10, 10));

		// Button to add Team
		final ButtonEditPart teamButton = new ButtonEditPart("Add team") {
			@Override
			protected void handleButtonClick() {
				// Add the team
				ExternalTeamModel team = new ExternalTeamModel();
				BeanDialog dialog = OfficeEditPart.this.createBeanDialog(team,
						"X", "Y");
				if (dialog.populate()) {
					OfficeEditPart.this.getCastedModel().addExternalTeam(team);
				}
			}
		};

		// Add Teams
		WrappingEditPart teamEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.addAll(OfficeEditPart.this.getCastedModel()
						.getExternalTeams());
				children.add(teamButton);
			}
		};
		teamEditPart.setFigure(new FreeformWrapperFigure(new SectionFigure(
				"Add team")));
		this.externalTeams = new WrappingModel<OfficeModel>(this
				.getCastedModel(), teamEditPart, new Point(10, 100));

		// Button to add Room
		final ButtonEditPart roomButton = new ButtonEditPart("Add Room") {
			@Override
			protected void handleButtonClick() {
				// Add the room
				OfficeRoomModel room = new OfficeRoomModel();
				BeanDialog dialog = OfficeEditPart.this.createBeanDialog(room,
						"Name", "X", "Y");
				dialog.registerPropertyInput("Id", new ClasspathSelectionInput(
						OfficeEditPart.this.getEditor(), new ClasspathFilter(
								IFile.class, new FileExtensionInputFilter(
										"room"))));
				if (dialog.populate()) {
					try {
						// Obtain the class path location
						String classPathLocation = ClasspathUtil
								.getClassPathLocation(room.getId());

						// Obtain the room configuration
						ProjectClassLoader classLoader = ProjectClassLoader
								.create(OfficeEditPart.this.getEditor());
						ConfigurationItem roomConfigItem = classLoader
								.findConfigurationItem(room.getId());
						if (roomConfigItem == null) {
							OfficeEditPart.this
									.messageError("Could not find Office Room at '"
											+ classPathLocation + "'");
							return;
						}

						// Load the room
						RoomLoader roomLoader = new RoomLoader();
						RoomModel rawRoom = roomLoader.loadRoom(roomConfigItem);

						// Load the office room
						OfficeLoader officeLoader = new OfficeLoader();
						room = officeLoader.loadOfficeRoom(roomConfigItem
								.getId(), "OFFICE ROOM", rawRoom,
								roomConfigItem.getContext(), classLoader);

						// Synchronise the room onto the office
						RoomToOfficeRoomSynchroniser.synchroniseRoomOntoOffice(
								rawRoom, room, OfficeEditPart.this
										.getCastedModel());

					} catch (Exception ex) {
						OfficeEditPart.this.messageError(ex);
					}
				}
			}
		};

		// Add Room
		WrappingEditPart roomEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				OfficeRoomModel room = OfficeEditPart.this.getCastedModel()
						.getRoom();
				if (room == null) {
					// No room, provide button to add room
					children.add(roomButton);
				} else {
					// Have room therefore add
					children.add(room);
				}
			}
		};
		roomEditPart.setFigure(new FreeformWrapperFigure(new SectionFigure(
				"Room")));
		this.room = new WrappingModel<OfficeModel>(this.getCastedModel(),
				roomEditPart, new Point(120, 10));

		// Add Managed Objects
		WrappingEditPart moEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.addAll(OfficeEditPart.this.getCastedModel()
						.getExternalManagedObjects());
			}
		};
		moEditPart
				.setFigure(new FreeformWrapperFigure(new SectionFigure("MO")));
		this.externalManagedObjects = new WrappingModel<OfficeModel>(this
				.getCastedModel(), moEditPart, new Point(500, 10));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart#createLayoutEditPolicy()
	 */
	@Override
	protected OfficeFloorLayoutEditPolicy<?> createLayoutEditPolicy() {
		return new OfficeLayoutEditPolicy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart#populateChildren(java.util.List)
	 */
	@Override
	protected void populateChildren(List<Object> childModels) {
		// Add the static children
		childModels.add(this.addAdministrator);
		childModels.add(this.externalTeams);
		childModels.add(this.room);
		childModels.add(this.externalManagedObjects);

		// Add the dynamic children
		childModels.addAll(this.getCastedModel().getAdministrators());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeEvent>(OfficeEvent
				.values()) {
			@Override
			protected void handlePropertyChange(OfficeEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_ADMINISTRATOR:
				case REMOVE_ADMINISTRATOR:
					OfficeEditPart.this.refreshChildren();
					break;
				case ADD_EXTERNAL_TEAM:
				case REMOVE_EXTERNAL_TEAM:
					OfficeEditPart.this.externalTeams.getEditPart().refresh();
					break;
				case ADD_EXTERNAL_MANAGED_OBJECT:
				case REMOVE_EXTERNAL_MANAGED_OBJECT:
					OfficeEditPart.this.externalManagedObjects.getEditPart()
							.refresh();
					break;
				case CHANGE_ROOM:
					OfficeEditPart.this.room.getEditPart().refresh();
					break;
				}
			}
		});
	}
}

/**
 * {@link org.eclipse.gef.editpolicies.LayoutEditPolicy} for the
 * {@link net.officefloor.model.office.OfficeModel}.
 */
class OfficeLayoutEditPolicy extends OfficeFloorLayoutEditPolicy<OfficeModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy#createCreateComand(P,
	 *      java.lang.Object, org.eclipse.draw2d.geometry.Point)
	 */
	@Override
	protected CreateCommand<?, ?> createCreateComand(OfficeModel parentModel,
			Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}
