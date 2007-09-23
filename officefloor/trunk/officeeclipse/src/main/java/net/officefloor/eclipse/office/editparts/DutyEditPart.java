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

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.requests.CreateConnectionRequest;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.common.figure.IndentFigure;
import net.officefloor.eclipse.common.figure.ListFigure;
import net.officefloor.eclipse.common.figure.ListItemFigure;
import net.officefloor.eclipse.common.figure.WrappingFigure;
import net.officefloor.eclipse.common.wrap.WrappingModel;
import net.officefloor.eclipse.office.models.FlowToAdminDutyWrappingModel;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.FlowItemToPostAdministratorDutyModel;
import net.officefloor.model.office.FlowItemToPreAdministratorDutyModel;
import net.officefloor.model.office.DutyModel.DutyEvent;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.office.DutyModel}.
 * 
 * @author Daniel
 */
public class DutyEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<DutyModel> {

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

				// Create the appropriate connection
				FlowToAdminDutyWrappingModel model = (FlowToAdminDutyWrappingModel) target;
				if (model.isPreAdmin()) {
					// Pre-admin
					FlowItemToPreAdministratorDutyModel conn = new FlowItemToPreAdministratorDutyModel();
					conn.setDuty((DutyModel) source);
					conn.setFlowItem(model.getFlowItem());
					conn.connect();
					return conn;

				} else {
					// Post-admin
					FlowItemToPostAdministratorDutyModel conn = new FlowItemToPostAdministratorDutyModel();
					conn.setDuty((DutyModel) source);
					conn.setFlowItem(model.getFlowItem());
					conn.connect();
					return conn;
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
	protected void populateConnectionTargetTypes(List<Class> types) {
		types.add(WrappingModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		models.addAll(this.getCastedModel().getPreAdminFlowItems());
		models.addAll(this.getCastedModel().getPostAdminFlowItems());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Never a target
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler> handlers) {
		handlers.add(new PropertyChangeHandler<DutyEvent>(DutyEvent.values()) {
			@Override
			protected void handlePropertyChange(DutyEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_FLOW:
				case REMOVE_FLOW:
					DutyEditPart.this.refreshChildren();
					break;
				case ADD_PRE_ADMIN_FLOW_ITEM:
				case REMOVE_PRE_ADMIN_FLOW_ITEM:
				case ADD_POST_ADMIN_FLOW_ITEM:
				case REMOVE_POST_ADMIN_FLOW_ITEM:
					DutyEditPart.this.refreshSourceConnections();
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
		// Create the figure
		WrappingFigure figure = new WrappingFigure(new IndentFigure(10,
				new ListFigure()));
		figure.addDecorate(new ListItemFigure(this.getCastedModel().getKey()));
		figure.addChildContainerFigure();

		// Return the figure
		return figure;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populateModelChildren(java.util.List)
	 */
	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getFlows());
	}

}
