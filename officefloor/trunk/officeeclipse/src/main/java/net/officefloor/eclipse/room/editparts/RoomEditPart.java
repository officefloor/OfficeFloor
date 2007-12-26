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
package net.officefloor.eclipse.room.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.classpath.ProjectClassLoader;
import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.dialog.input.ClasspathResourceSelectionPropertyInput;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.ButtonEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.common.wrap.OfficeFloorWrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingModel;
import net.officefloor.eclipse.desk.figure.SectionFigure;
import net.officefloor.model.room.ExternalFlowModel;
import net.officefloor.model.room.ExternalManagedObjectModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.SubRoomModel;
import net.officefloor.model.room.RoomModel.RoomEvent;
import net.officefloor.repository.ConfigurationItem;
import net.officefloor.room.RoomLoader;

import org.eclipse.draw2d.geometry.Point;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.room.RoomModel}.
 * 
 * @author Daniel
 */
public class RoomEditPart extends AbstractOfficeFloorDiagramEditPart<RoomModel> {

	/**
	 * Listing of {@link net.officefloor.model.room.ExternalManagedObjectModel}
	 * instances.
	 */
	private WrappingModel<RoomModel> externalManagedObjects;

	/**
	 * Listing of {@link net.officefloor.model.room.ExternalFlowModel}
	 * instances.
	 */
	private WrappingModel<RoomModel> externalOuputFlows;

	/**
	 * Button to add a {@link RoomModel} or
	 * {@link net.officefloor.model.desk.DeskModel} as a
	 * {@link net.officefloor.model.room.SubRoomModel}.
	 */
	private WrappingModel<RoomModel> roomDesk;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#init()
	 */
	@SuppressWarnings("unchecked")
	protected void init() {

		// Button to add Room/Desk as Sub Room
		final ButtonEditPart roomDeskButton = new ButtonEditPart("Add sub room") {
			@Override
			protected void handleButtonClick() {
				// Add the Sub Room
				SubRoomAddBean bean = new SubRoomAddBean();
				BeanDialog dialog = RoomEditPart.this.createBeanDialog(bean);
				dialog.registerPropertyInputBuilder("File",
						new ClasspathResourceSelectionPropertyInput(
								RoomEditPart.this.getEditor(), "desk", "room"));
				if (dialog.populate()) {
					try {
						// Obtain the configuration item
						ConfigurationItem configItem = ProjectClassLoader
								.findConfigurationItem(RoomEditPart.this
										.getEditor(), bean.file);
						if (configItem == null) {
							RoomEditPart.this.messageError("Can not find '"
									+ bean.file + "' on class path");
							return;
						}

						// Create the sub room
						RoomLoader roomLoader = new RoomLoader();
						SubRoomModel subRoom = roomLoader
								.loadSubRoom(configItem);
						subRoom.setId(bean.name);
						subRoom.setX(200);
						subRoom.setY(200);

						// Add the sub room
						RoomEditPart.this.getCastedModel().addSubRoom(subRoom);

					} catch (Exception ex) {
						RoomEditPart.this.messageError(ex);
					}
				}
			}
		};

		// Add Sub Rooms
		WrappingEditPart roomDeskEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.add(roomDeskButton);
			}
		};
		roomDeskEditPart.setFigure(new FreeformWrapperFigure(new SectionFigure(
				"Add sub room")));
		this.roomDesk = new WrappingModel<RoomModel>(this.getCastedModel(),
				roomDeskEditPart, new Point(10, 10));

		// Button to add external Managed Objects
		final ButtonEditPart extMoButton = new ButtonEditPart("Add Ext MO") {
			@Override
			protected void handleButtonClick() {
				// Add the populated External Managed Object
				ExternalManagedObjectModel mo = new ExternalManagedObjectModel();
				BeanDialog dialog = RoomEditPart.this.createBeanDialog(mo,
						"Object Type", "X", "Y");
				if (dialog.populate()) {
					RoomEditPart.this.getCastedModel()
							.addExternalManagedObject(mo);
				}
			}
		};

		// External Managed Objects
		WrappingEditPart extMoEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.addAll(RoomEditPart.this.getCastedModel()
						.getExternalManagedObjects());
				children.add(extMoButton);
			}
		};
		extMoEditPart.setFigure(new FreeformWrapperFigure(new SectionFigure(
				"Managed Objects")));
		this.externalManagedObjects = new WrappingModel<RoomModel>(this
				.getCastedModel(), extMoEditPart, new Point(10, 70));

		// Button to add external output Flow
		final ButtonEditPart extFlowButton = new ButtonEditPart("Add Flow") {
			@Override
			protected void handleButtonClick() {
				// Add the populated External Flow
				ExternalFlowModel flow = new ExternalFlowModel();
				BeanDialog dialog = RoomEditPart.this.createBeanDialog(flow,
						"X", "Y");
				if (dialog.populate()) {
					RoomEditPart.this.getCastedModel().addExternalFlow(flow);
				}
			}
		};

		// External Output Flows
		WrappingEditPart extFlowEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List children) {
				children.addAll(RoomEditPart.this.getCastedModel()
						.getExternalFlows());
				children.add(extFlowButton);
			}
		};
		extFlowEditPart.setFigure(new FreeformWrapperFigure(new SectionFigure(
				"Ext Flows")));
		this.externalOuputFlows = new WrappingModel<RoomModel>(this
				.getCastedModel(), extFlowEditPart, new Point(510, 10));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart#createLayoutEditPolicy()
	 */
	protected OfficeFloorLayoutEditPolicy<?> createLayoutEditPolicy() {
		return new RoomLayoutEditPolicy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart#populateChildren(java.util.List)
	 */
	protected void populateChildren(List<Object> childModels) {
		// Add the static children
		childModels.add(this.externalManagedObjects);
		childModels.add(this.externalOuputFlows);
		childModels.add(this.roomDesk);

		// Add the sub rooms
		childModels.addAll(this.getCastedModel().getSubRooms());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<RoomEvent>(RoomEvent.values()) {
			protected void handlePropertyChange(RoomEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_EXTERNAL_FLOW:
				case REMOVE_EXTERNAL_FLOW:
					RoomEditPart.this.externalOuputFlows.getEditPart()
							.refresh();
					break;
				case ADD_EXTERNAL_MANAGED_OBJECT:
				case REMOVE_EXTERNAL_MANAGED_OBJECT:
					RoomEditPart.this.externalManagedObjects.getEditPart()
							.refresh();
					break;
				case ADD_SUB_ROOM:
				case REMOVE_SUB_ROOM:
					RoomEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}

	/**
	 * Object to add a {@link net.officefloor.model.room.SubRoomModel}.
	 */
	public static class SubRoomAddBean {

		/**
		 * Name of the {@link SubRoomModel}.
		 */
		public String name;

		/**
		 * Name of the file.
		 */
		public String file;

		/**
		 * Specify the name.
		 * 
		 * @param name
		 *            Name of the {@link SubRoomModel}.
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Specify the file name.
		 * 
		 * @param file
		 *            File name.
		 */
		public void setFile(String file) {
			this.file = file;
		}
	}
}

/**
 * {@link org.eclipse.gef.editpolicies.LayoutEditPolicy} for the
 * {@link net.officefloor.model.room.RoomModel}.
 */
class RoomLayoutEditPolicy extends OfficeFloorLayoutEditPolicy<RoomModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy#createCreateComand(P,
	 *      java.lang.Object, org.eclipse.draw2d.geometry.Point)
	 */
	protected CreateCommand<?, ?> createCreateComand(RoomModel parentModel,
			Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}