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

import net.officefloor.eclipse.common.commands.CreateCommand;
import net.officefloor.eclipse.common.dialog.BeanDialog;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart;
import net.officefloor.eclipse.common.editparts.ButtonEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy;
import net.officefloor.eclipse.common.figure.FreeformWrapperFigure;
import net.officefloor.eclipse.common.wrap.OfficeFloorWrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingEditPart;
import net.officefloor.eclipse.common.wrap.WrappingModel;
import net.officefloor.eclipse.desk.figure.SectionFigure;
import net.officefloor.model.room.ExternalEscalationModel;
import net.officefloor.model.room.ExternalFlowModel;
import net.officefloor.model.room.RoomModel;
import net.officefloor.model.room.RoomModel.RoomEvent;

import org.eclipse.draw2d.geometry.Point;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.room.RoomModel}.
 * 
 * @author Daniel
 */
public class RoomEditPart extends AbstractOfficeFloorDiagramEditPart<RoomModel> {

	/**
	 * Listing of {@link net.officefloor.model.room.ExternalFlowModel}
	 * instances.
	 */
	private WrappingModel<RoomModel> externalOuputFlows;

	/**
	 * Listing of {@link ExternalEscalationModel} instances.
	 */
	private WrappingModel<RoomModel> externalEscalations;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * init()
	 */
	@SuppressWarnings("unchecked")
	protected void init() {

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

		// Button to add external escalations
		final ButtonEditPart extEscalationButton = new ButtonEditPart(
				"Add Escalation") {
			@Override
			protected void handleButtonClick() {
				// Add the populated External Escalation
				ExternalEscalationModel escalation = new ExternalEscalationModel();
				BeanDialog dialog = RoomEditPart.this.createBeanDialog(
						escalation, "Escalation Type", "X", "Y");
				if (dialog.populate()) {
					RoomEditPart.this.getCastedModel().addExternalEscalation(
							escalation);
				}
			}
		};

		// External Escalation
		WrappingEditPart extEscalationEditPart = new OfficeFloorWrappingEditPart() {
			@Override
			protected void populateModelChildren(List<Object> children) {
				children.addAll(RoomEditPart.this.getCastedModel()
						.getExternalEscalations());
				children.add(extEscalationButton);
			}
		};
		extEscalationEditPart.setFigure(new FreeformWrapperFigure(
				new SectionFigure("Ext Escalations")));
		this.externalEscalations = new WrappingModel<RoomModel>(this
				.getCastedModel(), extEscalationEditPart, new Point(510, 300));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart
	 * #createLayoutEditPolicy()
	 */
	protected OfficeFloorLayoutEditPolicy<?> createLayoutEditPolicy() {
		return new RoomLayoutEditPolicy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorDiagramEditPart
	 * #populateChildren(java.util.List)
	 */
	protected void populateChildren(List<Object> childModels) {
		// Add the static children
		childModels.add(this.externalOuputFlows);
		childModels.add(this.externalEscalations);

		// Add the sub rooms
		childModels.addAll(this.getCastedModel().getSubRooms());
		childModels.addAll(this.getCastedModel().getExternalManagedObjects());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
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
				case ADD_EXTERNAL_ESCALATION:
				case REMOVE_EXTERNAL_ESCALATION:
					RoomEditPart.this.externalEscalations.getEditPart()
							.refresh();
					break;
				case ADD_EXTERNAL_MANAGED_OBJECT:
				case REMOVE_EXTERNAL_MANAGED_OBJECT:
				case ADD_SUB_ROOM:
				case REMOVE_SUB_ROOM:
					RoomEditPart.this.refreshChildren();
					break;
				}
			}
		});
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
	 * @see
	 * net.officefloor.eclipse.common.editpolicies.OfficeFloorLayoutEditPolicy
	 * #createCreateComand(P, java.lang.Object,
	 * org.eclipse.draw2d.geometry.Point)
	 */
	protected CreateCommand<?, ?> createCreateComand(RoomModel parentModel,
			Object newModel, Point location) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO implement");
	}

}