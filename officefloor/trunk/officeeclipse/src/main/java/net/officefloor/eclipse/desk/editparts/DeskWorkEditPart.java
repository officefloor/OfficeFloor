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

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.desk.figure.DeskWorkFigure;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.DeskWorkToFlowItemModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.DeskWorkModel.DeskWorkEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.desk.DeskWorkModel}.
 * 
 * @author Daniel
 */
public class DeskWorkEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<DeskWorkModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		return new DeskWorkFigure(this.getCastedModel().getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populateModelChildren(java.util.List)
	 */
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getTasks());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#createConnectionModelFactory()
	 */
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {
				DeskWorkToFlowItemModel conn = new DeskWorkToFlowItemModel();
				conn.setDeskWork((DeskWorkModel) source);
				conn.setInitialFlowItem((FlowItemModel) target);
				conn.connect();
				return conn;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#populateConnectionTargetTypes(java.util.List)
	 */
	protected void populateConnectionTargetTypes(List<Class> types) {
		types.add(FlowItemModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	protected void populateConnectionSourceModels(List<Object> models) {
		DeskWorkToFlowItemModel initialFlow = this.getCastedModel()
				.getInitialFlowItem();
		if (initialFlow != null) {
			models.add(initialFlow);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	protected void populateConnectionTargetModels(List<Object> models) {
		// Not a target
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler> handlers) {
		handlers.add(new PropertyChangeHandler<DeskWorkEvent>(DeskWorkEvent
				.values()) {
			protected void handlePropertyChange(DeskWorkEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_INITIAL_FLOW_ITEM:
					DeskWorkEditPart.this.refreshSourceConnections();
					break;
				}
			}
		});
	}

}
