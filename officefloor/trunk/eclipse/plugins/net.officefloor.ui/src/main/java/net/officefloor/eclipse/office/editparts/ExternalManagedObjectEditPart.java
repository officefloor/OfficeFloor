/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.ExternalManagedObjectModel.ExternalManagedObjectEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExternalManagedObjectEditPart
		extends
		AbstractOfficeFloorEditPart<ExternalManagedObjectModel, ExternalManagedObjectEvent, ExternalManagedObjectFigure>
		implements ExternalManagedObjectFigureContext {

	@Override
	protected ExternalManagedObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createExternalManagedObjectFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		models.addAll(this.getCastedModel().getAdministrators());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOfficeSectionObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<ExternalManagedObjectModel> policy) {
		policy
				.allowDirectEdit(new DirectEditAdapter<OfficeChanges, ExternalManagedObjectModel>() {
					@Override
					public String getInitialValue() {
						return ExternalManagedObjectEditPart.this
								.getCastedModel()
								.getExternalManagedObjectName();
					}

					@Override
					public IFigure getLocationFigure() {
						return ExternalManagedObjectEditPart.this
								.getOfficeFloorFigure()
								.getExternalManagedObjectNameFigure();
					}

					@Override
					public Change<ExternalManagedObjectModel> createChange(
							OfficeChanges changes,
							ExternalManagedObjectModel target, String newValue) {
						return changes.renameExternalManagedObject(target,
								newValue);
					}
				});
	}

	@Override
	protected Class<ExternalManagedObjectEvent> getPropertyChangeEventType() {
		return ExternalManagedObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(ExternalManagedObjectEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_EXTERNAL_MANAGED_OBJECT_NAME:
			this.getOfficeFloorFigure().setExternalManagedObjectName(
					this.getCastedModel().getExternalManagedObjectName());
			break;
		case ADD_ADMINISTRATOR:
		case REMOVE_ADMINISTRATOR:
			this.refreshSourceConnections();
			break;
		case ADD_OFFICE_SECTION_OBJECT:
		case REMOVE_OFFICE_SECTION_OBJECT:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ==================== ExternalManagedObjectFigureContext ================
	 */

	@Override
	public String getExternalManagedObjectName() {
		return this.getCastedModel().getExternalManagedObjectName();
	}

	@Override
	public String getNextScope(String currentScope) {
		// // Find the index of the current scope
		// int currentScopeIndex = -1;
		// for (int i = 0; i < WorkEntry.MANAGED_OBJECT_SCOPES.length; i++) {
		// if (WorkEntry.MANAGED_OBJECT_SCOPES[i].equals(currentScope)) {
		// currentScopeIndex = i;
		// }
		// }
		//
		// // Move to the next scope (and cycle back to first if necessary)
		// int nextScopeIndex = (currentScopeIndex + 1)
		// % WorkEntry.MANAGED_OBJECT_SCOPES.length;
		//
		// // Return the next scope
		// return WorkEntry.MANAGED_OBJECT_SCOPES[nextScopeIndex];
		return null;
	}

	@Override
	public String getScope() {
		// return this.getCastedModel().getScope();
		return null;
	}

	@Override
	public void setScope(final String scope) {

		// // Maintain current scope
		// final String currentScope = this.getCastedModel().getScope();
		//
		// // Change the scope
		// this.executeCommand(new OfficeFloorCommand() {
		//
		// @Override
		// protected void doCommand() {
		// ExternalManagedObjectEditPart.this.getCastedModel().setScope(
		// scope);
		// }
		//
		// @Override
		// protected void undoCommand() {
		// ExternalManagedObjectEditPart.this.getCastedModel().setScope(
		// currentScope);
		// }
		// });
	}

}