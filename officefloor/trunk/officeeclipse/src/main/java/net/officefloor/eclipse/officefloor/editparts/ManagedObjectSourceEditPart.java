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
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectSourceFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.RemoveConnectionsAction;
import net.officefloor.model.officefloor.ManagedObjectSourceModel;
import net.officefloor.model.officefloor.ManagedObjectSourceToOfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.ManagedObjectSourceModel.ManagedObjectSourceEvent;

import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.officefloor.ManagedObjectSourceModel}.
 * 
 * @author Daniel
 */
public class ManagedObjectSourceEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<ManagedObjectSourceModel, OfficeFloorFigure>
		implements RemovableEditPart, ManagedObjectSourceFigureContext {

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
		handlers.add(new PropertyChangeHandler<ManagedObjectSourceEvent>(
				ManagedObjectSourceEvent.values()) {
			@Override
			protected void handlePropertyChange(
					ManagedObjectSourceEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case ADD_OFFICE_MANAGED_OBJECT:
				case REMOVE_OFFICE_MANAGED_OBJECT:
					ManagedObjectSourceEditPart.this.refreshTargetConnections();
					break;
				case CHANGE_MANAGING_OFFICE:
					ManagedObjectSourceEditPart.this.refreshSourceConnections();
					break;
				case ADD_DEPENDENCY:
				case REMOVE_DEPENDENCY:
				case ADD_HANDLER:
				case REMOVE_HANDLER:
				case ADD_TASK:
				case REMOVE_TASK:
				case ADD_TEAM:
				case REMOVE_TEAM:
					ManagedObjectSourceEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createManagedObjectSourceFigure(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populateModelChildren(java.util.List)
	 */
	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getDependencies());
		childModels.addAll(this.getCastedModel().getHandlers());
		childModels.addAll(this.getCastedModel().getTasks());
		childModels.addAll(this.getCastedModel().getTeams());
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
		models.addAll(this.getCastedModel().getOfficeManagedObjects());
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
				// Create the connection
				ManagedObjectSourceToOfficeFloorOfficeModel conn = new ManagedObjectSourceToOfficeFloorOfficeModel();
				conn.setManagedObjectSource((ManagedObjectSourceModel) source);
				conn.setManagingOffice((OfficeFloorOfficeModel) target);
				conn.connect();

				// Return the connection
				return conn;
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
	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(OfficeFloorOfficeModel.class);
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
		ManagedObjectSourceToOfficeFloorOfficeModel conn = this
				.getCastedModel().getManagingOffice();
		if (conn != null) {
			models.add(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.RemovableEditPart#delete()
	 */
	@Override
	public void delete() {
		// Disconnect and remove managed object source
		RemoveConnectionsAction<ManagedObjectSourceModel> mos = this
				.getCastedModel().removeConnections();
		OfficeFloorModel officeFloor = (OfficeFloorModel) this.getParent()
				.getParent().getModel();
		officeFloor.removeManagedObjectSource(mos.getModel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.RemovableEditPart#undelete()
	 */
	@Override
	public void undelete() {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectSourceEditPart.undelete");
	}

	/*
	 * ================== ManagedObjectSourceFigureContext =====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.officefloor.ManagedObjectSourceFigureContext
	 * #getManagedObjectSourceName()
	 */
	@Override
	public String getManagedObjectSourceName() {
		return this.getCastedModel().getId();
	}

}
