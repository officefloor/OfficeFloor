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
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.connection.ConnectionModelFactory;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeObjectFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectToOfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectModel;
import net.officefloor.model.officefloor.DeployedOfficeObjectModel.DeployedOfficeObjectEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link DeployedOfficeObjectModel}.
 * 
 * @author Daniel
 */
// TODO rename to DeployedOfficeObjectModel
public class DeployedOfficeObjectEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<DeployedOfficeObjectModel, OfficeFloorFigure>
		implements DeployedOfficeObjectFigureContext {

	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {
				// Create the connection
				DeployedOfficeObjectToOfficeFloorManagedObjectModel conn = new DeployedOfficeObjectToOfficeFloorManagedObjectModel();
				conn
						.setDeployedOfficeObject((DeployedOfficeObjectModel) source);
				conn
						.setOfficeFloorManagedObject((OfficeFloorManagedObjectModel) target);
				conn.connect();

				// Return the connection
				return conn;
			}
		};
	}

	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(OfficeFloorManagedObjectModel.class);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		DeployedOfficeObjectToOfficeFloorManagedObjectModel conn = this
				.getCastedModel().getOfficeFloorManagedObject();
		if (conn != null) {
			models.add(conn);
		}
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Never a target
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<DeployedOfficeObjectEvent>(
				DeployedOfficeObjectEvent.values()) {
			@Override
			protected void handlePropertyChange(
					DeployedOfficeObjectEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_OFFICE_FLOOR_MANAGED_OBJECT:
					DeployedOfficeObjectEditPart.this.refreshSourceConnections();
					break;
				}
			}
		});
	}

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createDeployedOfficeObjectFigure(this);
	}

	/*
	 * ==================== OfficeManagedObjectFigureContext =================
	 */

	@Override
	public String getDeployedOfficeObjectName() {
		return this.getCastedModel().getDeployedOfficeObjectName();
	}

}