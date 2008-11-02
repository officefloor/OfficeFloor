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
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.FlowItemOutputFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel;
import net.officefloor.model.desk.FlowItemOutputToExternalFlowModel;
import net.officefloor.model.desk.FlowItemOutputToFlowItemModel;
import net.officefloor.model.desk.FlowItemOutputModel.FlowItemOutputEvent;

import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.desk.FlowItemOutputModel}.
 * 
 * @author Daniel
 */
public class FlowItemOutputEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<FlowItemOutputModel, OfficeFloorFigure>
		implements FlowItemOutputFigureContext {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createFlowItemOutputFigure(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.editparts.
	 * AbstractOfficeFloorSourceNodeEditPart#createConnectionModelFactory()
	 */
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {

				// TODO Handle always only connected to one type

				// Obtain the type of link
				String linkType = (String) request.getNewObject();

				if (target instanceof FlowItemModel) {

					// Create the flow connection
					FlowItemOutputToFlowItemModel conn = new FlowItemOutputToFlowItemModel();
					conn.setOutput((FlowItemOutputModel) source);
					conn.setFlowItem((FlowItemModel) target);
					conn.setLinkType(linkType);
					conn.connect();
					return conn;
				} else if (target instanceof ExternalFlowModel) {

					// Create the external flow connection
					FlowItemOutputToExternalFlowModel conn = new FlowItemOutputToExternalFlowModel();
					conn.setOutput((FlowItemOutputModel) source);
					conn.setExternalFlow((ExternalFlowModel) target);
					conn.setLinkType(linkType);
					conn.connect();
					return conn;
				} else {
					throw new OfficeFloorPluginFailure(
							"Unknown connection target type "
									+ target.getClass().getName()
									+ " for "
									+ FlowItemOutputEditPart.this.getClass()
											.getName());
				}
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.editparts.
	 * AbstractOfficeFloorSourceNodeEditPart
	 * #populateConnectionTargetTypes(java.util.List)
	 */
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(FlowItemModel.class);
		types.add(ExternalFlowModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionSourceModels(java.util.List)
	 */
	protected void populateConnectionSourceModels(List<Object> models) {
		// Flow
		FlowItemOutputToFlowItemModel flow = this.getCastedModel()
				.getFlowItem();
		if (flow != null) {
			models.add(flow);
		}

		// External flow
		FlowItemOutputToExternalFlowModel extFlow = this.getCastedModel()
				.getExternalFlow();
		if (extFlow != null) {
			models.add(extFlow);
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
		// Not a target
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
		handlers.add(new PropertyChangeHandler<FlowItemOutputEvent>(
				FlowItemOutputEvent.values()) {
			protected void handlePropertyChange(FlowItemOutputEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_FLOW_ITEM:
				case CHANGE_EXTERNAL_FLOW:
					FlowItemOutputEditPart.this.refreshSourceConnections();
					break;
				}
			}
		});
	}

	/*
	 * ======================= FlowItemOutputFigureContext ================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.desk.FlowItemOutputFigureContext#
	 * getFlowItemOutputName()
	 */
	@Override
	public String getFlowItemOutputName() {

		// Obtain the name
		String name = this.getCastedModel().getLabel();
		if ((name == null) || (name.trim().length() == 0)) {
			// Label not provided, so use the id
			name = this.getCastedModel().getId();
		}

		// Return the name
		return name;
	}

}
