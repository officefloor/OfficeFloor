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
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.skin.desk.DeskTaskObjectFigure;
import net.officefloor.eclipse.skin.desk.DeskTaskObjectFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.WorkTaskObjectModel;
import net.officefloor.model.desk.WorkTaskObjectToExternalManagedObjectModel;
import net.officefloor.model.desk.WorkTaskObjectModel.WorkTaskObjectEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link DeskTaskObjectModel}.
 * 
 * @author Daniel
 */
// TODO rename to WorkTaskObjectEditPart
public class DeskTaskObjectEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<WorkTaskObjectModel, DeskTaskObjectFigure>
		implements DeskTaskObjectFigureContext {

	@Override
	protected DeskTaskObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createDeskTaskObjectFigure(this);
	}

	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {
				WorkTaskObjectToExternalManagedObjectModel conn = new WorkTaskObjectToExternalManagedObjectModel();
				conn.setTaskObject((WorkTaskObjectModel) source);
				conn
						.setExternalManagedObject((ExternalManagedObjectModel) target);
				conn.connect();
				return conn;
			}
		};
	}

	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(ExternalManagedObjectModel.class);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		WorkTaskObjectToExternalManagedObjectModel source = this
				.getCastedModel().getExternalManagedObject();
		if (source != null) {
			models.add(source);
		}
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Not a target
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<WorkTaskObjectEvent>(
				WorkTaskObjectEvent.values()) {
			protected void handlePropertyChange(WorkTaskObjectEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_IS_PARAMETER:
					// Indicate is parameter changed
					DeskTaskObjectEditPart.this.getOfficeFloorFigure()
							.setIsParameter(
									DeskTaskObjectEditPart.this
											.getCastedModel().getIsParameter());
					break;
				case CHANGE_EXTERNAL_MANAGED_OBJECT:
					DeskTaskObjectEditPart.this.refreshSourceConnections();
					break;
				}
			}
		});
	}

	/*
	 * =================== DeskTaskObjectFigureContext ========================
	 */

	@Override
	public String getObjectType() {
		return this.getCastedModel().getObjectType();
	}

	@Override
	public boolean isParameter() {
		return this.getCastedModel().getIsParameter();
	}

	@Override
	public void setIsParameter(final boolean isParameter) {

		// Obtain the current state
		final boolean currentIsParameter = this.getCastedModel()
				.getIsParameter();

		// Specify the change
		this.executeCommand(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				// Set new value
				DeskTaskObjectEditPart.this.getCastedModel().setIsParameter(
						isParameter);
			}

			@Override
			protected void undoCommand() {
				// Reset to old value
				DeskTaskObjectEditPart.this.getCastedModel().setIsParameter(
						currentIsParameter);
			}
		});
	}

}