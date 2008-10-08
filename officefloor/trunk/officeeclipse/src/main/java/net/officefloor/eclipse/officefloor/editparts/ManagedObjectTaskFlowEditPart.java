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
import net.officefloor.eclipse.common.dialog.OfficeTaskDialog;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFlowFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.officefloor.FlowTaskToOfficeTaskModel;
import net.officefloor.model.officefloor.ManagedObjectTaskFlowModel;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;
import net.officefloor.model.officefloor.OfficeTaskModel;
import net.officefloor.model.officefloor.ManagedObjectTaskFlowModel.ManagedObjectTaskFlowEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link ManagedObjectTaskFlowModel}.
 * 
 * @author Daniel
 */
public class ManagedObjectTaskFlowEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<ManagedObjectTaskFlowModel>
		implements ManagedObjectTaskFlowFigureContext {

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
		handlers.add(new PropertyChangeHandler<ManagedObjectTaskFlowEvent>(
				ManagedObjectTaskFlowEvent.values()) {
			@Override
			protected void handlePropertyChange(
					ManagedObjectTaskFlowEvent property, PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_OFFICE_TASK:
					ManagedObjectTaskFlowEditPart.this
							.refreshSourceConnections();
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
				.createManagedObjectTaskFlowFigure(this);
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

				// Obtain the office task
				OfficeTaskModel task = OfficeTaskDialog.getOfficeTaskModel(
						target, ManagedObjectTaskFlowEditPart.this.getEditor());
				if (task == null) {
					// No task
					return null;
				}

				// Create the connection to the task
				FlowTaskToOfficeTaskModel conn = new FlowTaskToOfficeTaskModel();
				conn.setTaskFlow((ManagedObjectTaskFlowModel) source);
				conn.setOfficeTask(task);
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
		types.add(OfficeTaskModel.class);
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
		FlowTaskToOfficeTaskModel conn = this.getCastedModel().getOfficeTask();
		if (conn != null) {
			models.add(conn);
		}
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
		// Never a target
	}

	/*
	 * ================ ManagedObjectTaskFlowFigureContext ====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFlowFigureContext
	 * #getFlowName()
	 */
	@Override
	public String getFlowName() {
		return this.getCastedModel().getFlowId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFlowFigureContext
	 * #getInitialTaskName()
	 */
	@Override
	public String getInitialTaskName() {
		return this.getCastedModel().getInitialTaskName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.officefloor.ManagedObjectTaskFlowFigureContext
	 * #getInitialWorkName()
	 */
	@Override
	public String getInitialWorkName() {
		return this.getCastedModel().getInitialWorkName();
	}

}
