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

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.officefloor.figure.OfficeManagedObjectFigure;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel;
import net.officefloor.model.officefloor.OfficeManagedObjectToManagedObjectSourceModel;
import net.officefloor.model.officefloor.OfficeManagedObjectModel.OfficeManagedObjectEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.officefloor.OfficeManagedObjectModel}.
 * 
 * @author Daniel
 */
public class OfficeManagedObjectEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<OfficeManagedObjectModel> {

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
				// Create the connection
				OfficeManagedObjectToManagedObjectSourceModel conn = new OfficeManagedObjectToManagedObjectSourceModel();
				conn.setOfficeManagedObject((OfficeManagedObjectModel) source);
				conn.setManagedObjectSource((ManagedObjectSourceModel) target);
				conn.connect();

				// Return the connection
				return conn;
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
		types.add(ManagedObjectSourceModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		OfficeManagedObjectToManagedObjectSourceModel conn = this
				.getCastedModel().getManagedObjectSource();
		if (conn != null) {
			models.add(conn);
		}
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
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<OfficeManagedObjectEvent>(
				OfficeManagedObjectEvent.values()) {
			@Override
			protected void handlePropertyChange(
					OfficeManagedObjectEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_MANAGED_OBJECT_SOURCE:
					OfficeManagedObjectEditPart.this.refreshSourceConnections();
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
		return new OfficeManagedObjectFigure(this.getCastedModel()
				.getManagedObjectName());
	}

}
