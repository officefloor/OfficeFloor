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
import net.officefloor.eclipse.room.figure.SubRoomManagedObjectFigure;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.room.ExternalManagedObjectModel;
import net.officefloor.model.room.ManagedObjectToExternalManagedObjectModel;
import net.officefloor.model.room.SubRoomManagedObjectModel;
import net.officefloor.model.room.SubRoomManagedObjectModel.SubRoomManagedObjectEvent;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.room.SubRoomManagedObjectModel}.
 * 
 * @author Daniel
 */
public class SubRoomManagedObjectEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<SubRoomManagedObjectModel> {

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
				ManagedObjectToExternalManagedObjectModel conn = new ManagedObjectToExternalManagedObjectModel();
				conn.setManagedObject((SubRoomManagedObjectModel) source);
				conn
						.setExternalManagedObject((ExternalManagedObjectModel) target);
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
	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(ExternalManagedObjectModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		ManagedObjectToExternalManagedObjectModel source = this
				.getCastedModel().getExternalManagedObject();
		if (source != null) {
			models.add(source);
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
		handlers.add(new PropertyChangeHandler<SubRoomManagedObjectEvent>(
				SubRoomManagedObjectEvent.values()) {
			@Override
			protected void handlePropertyChange(
					SubRoomManagedObjectEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_EXTERNAL_MANAGED_OBJECT:
					SubRoomManagedObjectEditPart.this
							.refreshSourceConnections();
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
		return new SubRoomManagedObjectFigure(this.getCastedModel().getName());
	}

}
