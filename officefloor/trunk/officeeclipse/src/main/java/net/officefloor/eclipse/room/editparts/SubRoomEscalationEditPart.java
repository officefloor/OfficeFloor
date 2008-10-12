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

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.room.SubRoomEscalationFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.room.EscalationToExternalEscalationModel;
import net.officefloor.model.room.EscalationToInputFlowModel;
import net.officefloor.model.room.ExternalEscalationModel;
import net.officefloor.model.room.SubRoomEscalationModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomEscalationModel.SubRoomEscalationEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link SubRoomEscalationModel}.
 * 
 * @author Daniel
 */
public class SubRoomEscalationEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<SubRoomEscalationModel, OfficeFloorFigure>
		implements SubRoomEscalationFigureContext {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getRoomFigureFactory()
				.createSubRoomEscalationFigure(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<SubRoomEscalationEvent>(
				SubRoomEscalationEvent.values()) {
			@Override
			protected void handlePropertyChange(
					SubRoomEscalationEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_EXTERNAL_ESCALATION:
				case CHANGE_INPUT_FLOW:
					SubRoomEscalationEditPart.this.refreshSourceConnections();
					break;
				}
			}
		});
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
		types.add(SubRoomInputFlowModel.class);
		types.add(ExternalEscalationModel.class);
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

				// Determine connection
				if (target instanceof SubRoomInputFlowModel) {

					// Create and return the connection
					EscalationToInputFlowModel conn = new EscalationToInputFlowModel();
					conn.setEscalation((SubRoomEscalationModel) source);
					conn.setInputFlow((SubRoomInputFlowModel) target);
					conn.connect();
					return conn;

				} else if (target instanceof ExternalEscalationModel) {

					// Create and return the connection
					EscalationToExternalEscalationModel conn = new EscalationToExternalEscalationModel();
					conn.setEscalation((SubRoomEscalationModel) source);
					conn
							.setExternalEscalation((ExternalEscalationModel) target);
					conn.connect();
					return conn;

				} else {

					// Unknown target
					throw new OfficeFloorPluginFailure("Can not connect "
							+ this.getClass().getSimpleName() + " to a "
							+ target.getClass().getSimpleName());
				}
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
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Input Flow connection
		EscalationToInputFlowModel inputFlowConn = this.getCastedModel()
				.getInputFlow();
		if (inputFlowConn != null) {
			models.add(inputFlowConn);
		}

		// External Escalation connection
		EscalationToExternalEscalationModel extConn = this.getCastedModel()
				.getExternalEscalation();
		if (extConn != null) {
			models.add(extConn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Never a target
	}

	/*
	 * ==================== SubRoomEscalationFigureContext =================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.SubRoomEscalationFigureContext#
	 * getSubRoomEscalationName()
	 */
	@Override
	public String getSubRoomEscalationName() {
		return this.getCastedModel().getName();
	}

}
