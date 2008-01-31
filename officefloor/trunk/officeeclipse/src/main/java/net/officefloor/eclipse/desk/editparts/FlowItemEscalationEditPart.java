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

import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.desk.figure.FlowItemOutputFigure;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.desk.ExternalEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToExternalEscalationModel;
import net.officefloor.model.desk.FlowItemEscalationToFlowItemModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemEscalationModel.FlowItemEscalationEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link FlowItemEscalationModel}.
 * 
 * @author Daniel
 */
public class FlowItemEscalationEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<FlowItemEscalationModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<FlowItemEscalationEvent>(
				FlowItemEscalationEvent.values()) {
			@Override
			protected void handlePropertyChange(
					FlowItemEscalationEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_ESCALATION_HANDLER:
				case CHANGE_EXTERNAL_ESCALATION:
					FlowItemEscalationEditPart.this.refreshSourceConnections();
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

		// Obtain simple name of escalation
		String escalationType = this.getCastedModel().getEscalationType();
		String simpleType = escalationType;
		if (simpleType.indexOf('.') > 0) {
			simpleType = simpleType.substring(simpleType.lastIndexOf('.') + 1);
		}

		// Create the figure
		FlowItemOutputFigure figure = new FlowItemOutputFigure(simpleType);
		figure.setToolTip(new Label(escalationType));

		// Return the figure
		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#createConnectionModelFactory()
	 */
	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			@Override
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {

				if (target instanceof FlowItemModel) {
					// Create the flow connection
					FlowItemEscalationToFlowItemModel conn = new FlowItemEscalationToFlowItemModel();
					conn.setEscalation((FlowItemEscalationModel) source);
					conn.setHandler((FlowItemModel) target);
					conn.connect();
					return conn;

				} else if (target instanceof ExternalEscalationModel) {
					// Create the external escalation connection
					FlowItemEscalationToExternalEscalationModel conn = new FlowItemEscalationToExternalEscalationModel();
					conn.setEscalation((FlowItemEscalationModel) source);
					conn
							.setExternalEscalation((ExternalEscalationModel) target);
					conn.connect();
					return conn;

				} else {
					// Unknown type
					throw new OfficeFloorPluginFailure("Unknown target model "
							+ target.getClass().getName());
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
		types.add(FlowItemModel.class);
		types.add(ExternalEscalationModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Flow
		FlowItemEscalationToFlowItemModel flow = this.getCastedModel()
				.getEscalationHandler();
		if (flow != null) {
			models.add(flow);
		}

		// External escalation
		FlowItemEscalationToExternalEscalationModel escalation = this
				.getCastedModel().getExternalEscalation();
		if (escalation != null) {
			models.add(escalation);
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

}
