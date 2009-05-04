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

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.OfficeFloorPluginFailure;
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.common.editpolicies.connection.ConnectionModelFactory;
import net.officefloor.eclipse.office.operations.RemoveAdministratorOperation;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.AdministratorFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.office.AdministratorModel.AdministratorEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link AdministratorModel}.
 * 
 * @author Daniel
 */
public class AdministratorEditPart
		extends
		AbstractOfficeFloorSourceNodeEditPart<AdministratorModel, OfficeFloorFigure>
		implements RemovableEditPart, AdministratorFigureContext {

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<AdministratorEvent>(
				AdministratorEvent.values()) {
			@Override
			protected void handlePropertyChange(AdministratorEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_DUTY:
				case REMOVE_DUTY:
					AdministratorEditPart.this.refreshChildren();
					break;
//				case ADD_MANAGED_OBJECT:
//				case REMOVE_MANAGED_OBJECT:
//					AdministratorEditPart.this.refreshSourceConnections();
//					break;
//				case CHANGE_TEAM:
//					AdministratorEditPart.this.refreshSourceConnections();
//					break;
				}
			}
		});
	}

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createAdministratorFigure(this);
	}

	@Override
	protected boolean isFreeformFigure() {
		return true;
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getDuties());
	}

	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {

				if (target instanceof ExternalManagedObjectModel) {
					// Create the connection
//					AdministratorToManagedObjectModel conn = new AdministratorToManagedObjectModel();
//					conn.setAdministrator((AdministratorModel) source);
//					conn.setManagedObject((ExternalManagedObjectModel) target);
//					conn.connect();
//					return conn;
					return null;

				} else if (target instanceof OfficeTeamModel) {
					// Create the connection
//					AdministratorToTeamModel conn = new AdministratorToTeamModel();
//					conn.setAdministrator((AdministratorModel) source);
//					conn.setTeam((ExternalTeamModel) target);
//					conn.connect();
//					return conn;
					return null;

				} else {
					throw new OfficeFloorPluginFailure("Unknown target "
							+ target
							+ " [type="
							+ (target == null ? null : target.getClass()
									.getName()) + "]");
				}
			}
		};
	}

	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(ExternalManagedObjectModel.class);
		types.add(OfficeTeamModel.class);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
//		models.addAll(this.getCastedModel().getManagedObjects());
//		AdministratorToTeamModel team = this.getCastedModel().getTeam();
//		if (team != null) {
//			models.add(team);
//		}
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Never a target
	}

	@Override
	public Operation getRemoveOperation() {
		return new RemoveAdministratorOperation();
	}

	/*
	 * ==================== AdministratorFigureContext ======================
	 */

	@Override
	public String getAdministratorName() {
		return this.getCastedModel().getAdministratorName();
	}

}