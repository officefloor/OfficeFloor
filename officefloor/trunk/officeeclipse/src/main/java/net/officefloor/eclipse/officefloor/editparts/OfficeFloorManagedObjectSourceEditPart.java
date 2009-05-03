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
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.officefloor.operations.RemoveOfficeFloorManagedObjectSourceOperation;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel.OfficeFloorManagedObjectSourceEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link OfficeFloorManagedObjectSourceModel}.
 * 
 * @author Daniel
 */
// TODO rename to OfficeFloorManagedObjectSourceEditPart
public class OfficeFloorManagedObjectSourceEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<OfficeFloorManagedObjectSourceModel, OfficeFloorFigure>
		implements RemovableEditPart, OfficeFloorManagedObjectSourceFigureContext {

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers
				.add(new PropertyChangeHandler<OfficeFloorManagedObjectSourceEvent>(
						OfficeFloorManagedObjectSourceEvent.values()) {
					@Override
					protected void handlePropertyChange(
							OfficeFloorManagedObjectSourceEvent property,
							PropertyChangeEvent evt) {
						switch (property) {
						case CHANGE_MANAGING_OFFICE:
							OfficeFloorManagedObjectSourceEditPart.this
									.refreshSourceConnections();
							break;
						case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
						case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
						case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
						case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
							OfficeFloorManagedObjectSourceEditPart.this.refreshChildren();
							break;
						}
					}
				});
	}

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFloorManagedObjectSourceFigure(this);
	}

	@Override
	protected boolean isFreeformFigure() {
		return true;
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceFlows());
		childModels.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceTeams());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// No target connections
	}

	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			@Override
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {
				// Create the connection
				OfficeFloorManagedObjectSourceToDeployedOfficeModel conn = new OfficeFloorManagedObjectSourceToDeployedOfficeModel();
				conn
						.setOfficeFloorManagedObjectSource((OfficeFloorManagedObjectSourceModel) source);
				conn.setManagingOffice((DeployedOfficeModel) target);
				conn.connect();

				// Return the connection
				return conn;
			}
		};
	}

	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(DeployedOfficeModel.class);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		OfficeFloorManagedObjectSourceToDeployedOfficeModel conn = this
				.getCastedModel().getManagingOffice();
		if (conn != null) {
			models.add(conn);
		}
	}

	@Override
	public Operation getRemoveOperation() {
		return new RemoveOfficeFloorManagedObjectSourceOperation();
	}

	/*
	 * ================== ManagedObjectSourceFigureContext =====================
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.getCastedModel().getOfficeFloorManagedObjectSourceName();
	}

}