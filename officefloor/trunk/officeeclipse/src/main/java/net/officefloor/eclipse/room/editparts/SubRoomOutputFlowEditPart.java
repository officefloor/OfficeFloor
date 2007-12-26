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

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.requests.CreateConnectionRequest;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.room.figure.SubRoomOutputFlowFigure;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.room.ExternalFlowModel;
import net.officefloor.model.room.OutputFlowToExternalFlowModel;
import net.officefloor.model.room.OutputFlowToInputFlowModel;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomOutputFlowModel;
import net.officefloor.model.room.SubRoomOutputFlowModel.SubRoomOutputFlowEvent;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.room.SubRoomOutputFlowModel}.
 * 
 * @author Daniel
 */
public class SubRoomOutputFlowEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<SubRoomOutputFlowModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#createConnectionModelFactory()
	 */
	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {

				// TODO Handle always only connected to one type

				// Obtain the type of link
				String linkType = (String) request.getNewObject();

				if (target instanceof SubRoomInputFlowModel) {
					// Create the flow connection
					OutputFlowToInputFlowModel conn = new OutputFlowToInputFlowModel();
					conn.setOutput((SubRoomOutputFlowModel) source);
					conn.setInput((SubRoomInputFlowModel) target);
					conn.setLinkType(linkType);
					conn.connect();
					return conn;

				} else if (target instanceof ExternalFlowModel) {
					// Create the external flow connection
					OutputFlowToExternalFlowModel conn = new OutputFlowToExternalFlowModel();
					conn.setOutput((SubRoomOutputFlowModel) source);
					conn.setExternalFlow((ExternalFlowModel) target);
					conn.setLinkType(linkType);
					conn.connect();
					return conn;

				} else {
					throw new IllegalArgumentException("Unknown target '"
							+ target.getClass().getName()
							+ "' for "
							+ SubRoomOutputFlowEditPart.this.getClass()
									.getName());
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#populateConnectionTargetTypes(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(SubRoomInputFlowModel.class);
		types.add(ExternalFlowModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		OutputFlowToInputFlowModel sourceInput = this.getCastedModel()
				.getInput();
		if (sourceInput != null) {
			models.add(sourceInput);
		}
		OutputFlowToExternalFlowModel sourceExternal = this.getCastedModel()
				.getExternalFlow();
		if (sourceExternal != null) {
			models.add(sourceExternal);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Not a target
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<SubRoomOutputFlowEvent>(
				SubRoomOutputFlowEvent.values()) {
			@Override
			protected void handlePropertyChange(
					SubRoomOutputFlowEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_EXTERNAL_FLOW:
				case CHANGE_INPUT:
					SubRoomOutputFlowEditPart.this.refreshSourceConnections();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		return new SubRoomOutputFlowFigure(this.getCastedModel().getName());
	}

}
