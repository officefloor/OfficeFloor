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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.skin.desk.FlowItemFigure;
import net.officefloor.eclipse.skin.desk.FlowItemFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;
import net.officefloor.model.desk.DeskWorkToFlowItemModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemToNextExternalFlowModel;
import net.officefloor.model.desk.FlowItemToNextFlowItemModel;
import net.officefloor.model.desk.FlowItemModel.FlowItemEvent;

import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.desk.FlowItemModel}.
 * 
 * @author Daniel
 */
public class FlowItemEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<FlowItemModel, FlowItemFigure>
		implements RemovableEditPart, FlowItemFigureContext {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected FlowItemFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createFlowItemFigure(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populateModelChildren(java.util.List)
	 */
	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getOutputs());
		childModels.addAll(this.getCastedModel().getEscalations());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.editparts.
	 * AbstractOfficeFloorSourceNodeEditPart
	 * #populateConnectionTargetTypes(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(FlowItemModel.class);
		types.add(ExternalFlowModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.editparts.
	 * AbstractOfficeFloorSourceNodeEditPart#createConnectionModelFactory()
	 */
	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			@Override
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {
				ConnectionModel returnConn;
				if (target instanceof FlowItemModel) {
					// Create the connection to flow item
					FlowItemToNextFlowItemModel conn = new FlowItemToNextFlowItemModel();
					conn.setPreviousFlowItem((FlowItemModel) source);
					conn.setNextFlowItem((FlowItemModel) target);
					conn.connect();
					returnConn = conn;

				} else if (target instanceof ExternalFlowModel) {
					// Create the connection to external flow
					FlowItemToNextExternalFlowModel conn = new FlowItemToNextExternalFlowModel();
					conn.setPreviousFlowItem((FlowItemModel) source);
					conn.setNextExternalFlow((ExternalFlowModel) target);
					conn.connect();
					returnConn = conn;

				} else {
					// Unknown target
					throw new OfficeFloorPluginFailure("Unknown target type "
							+ target.getClass().getName());
				}

				// Return the connection
				return returnConn;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionSourceModels(java.util.List)
	 */
	protected void populateConnectionSourceModels(List<Object> models) {
		// Add flow to next flow
		FlowItemToNextFlowItemModel nextFlowItem = this.getCastedModel()
				.getNextFlowItem();
		if (nextFlowItem != null) {
			models.add(nextFlowItem);
		}

		// Add flow to next external flow
		FlowItemToNextExternalFlowModel nextExternalFlow = this
				.getCastedModel().getNextExternalFlow();
		if (nextExternalFlow != null) {
			models.add(nextExternalFlow);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionTargetModels(java.util.List)
	 */
	protected void populateConnectionTargetModels(List<Object> models) {
		// Add work that this is initial flow
		DeskWorkToFlowItemModel work = this.getCastedModel().getDeskWork();
		if (work != null) {
			models.add(work);
		}

		// Add desk task
		DeskTaskToFlowItemModel task = this.getCastedModel().getDeskTask();
		if (task != null) {
			models.add(task);
		}

		// Add flow inputs
		models.addAll(this.getCastedModel().getInputs());

		// Add handled escalations
		models.addAll(this.getCastedModel().getHandledEscalations());

		// Add flow previous
		models.addAll(this.getCastedModel().getPreviousFlowItems());
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
		handlers.add(new PropertyChangeHandler<FlowItemEvent>(FlowItemEvent
				.values()) {
			protected void handlePropertyChange(FlowItemEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_IS_PUBLIC:
					// Ensure display is public
					FlowItemEditPart.this.getOfficeFloorFigure().setIsPublic(
							FlowItemEditPart.this.getCastedModel()
									.getIsPublic());
					break;
				case CHANGE_NEXT_FLOW_ITEM:
				case CHANGE_NEXT_EXTERNAL_FLOW:
					FlowItemEditPart.this.refreshSourceConnections();
					break;
				case CHANGE_DESK_WORK:
				case CHANGE_DESK_TASK:
				case ADD_INPUT:
				case REMOVE_INPUT:
				case ADD_HANDLED_ESCALATION:
				case REMOVE_HANDLED_ESCALATION:
				case ADD_PREVIOUS_FLOW_ITEM:
				case REMOVE_PREVIOUS_FLOW_ITEM:
					FlowItemEditPart.this.refreshTargetConnections();
					break;
				case ADD_OUTPUT:
				case REMOVE_OUTPUT:
				case ADD_ESCALATION:
				case REMOVE_ESCALATION:
					FlowItemEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.RemovableEditPart#delete()
	 */
	@Override
	public void delete() {
		// Disconnect and remove the flow item
		RemoveConnectionsAction<FlowItemModel> flowItem = this.getCastedModel()
				.removeConnections();
		DeskModel desk = (DeskModel) this.getParent().getModel();
		desk.removeFlowItem(flowItem.getModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.RemovableEditPart#undelete()
	 */
	@Override
	public void undelete() {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement FlowItemEditPart.undelete");
	}

	/*
	 * ======================= FlowItemFigureContext ========================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.FlowItemFigureContext#getFlowItemName()
	 */
	@Override
	public String getFlowItemName() {
		return this.getCastedModel().getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.skin.desk.FlowItemFigureContext#isPublic()
	 */
	@Override
	public boolean isPublic() {
		return this.getCastedModel().getIsPublic();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.FlowItemFigureContext#setIsPublic(boolean
	 * )
	 */
	@Override
	public void setIsPublic(final boolean isPublic) {

		// Store current state
		final boolean currentIsPublic = this.getCastedModel().getIsPublic();

		// Make change
		this.executeCommand(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				FlowItemEditPart.this.getCastedModel().setIsPublic(isPublic);
			}

			@Override
			protected void undoCommand() {
				FlowItemEditPart.this.getCastedModel().setIsPublic(
						currentIsPublic);
			}
		});
	}

}
