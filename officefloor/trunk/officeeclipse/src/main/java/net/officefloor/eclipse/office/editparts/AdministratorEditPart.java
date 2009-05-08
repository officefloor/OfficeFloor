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
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.AdministratorFigureContext;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.AdministratorModel.AdministratorEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link AdministratorModel}.
 * 
 * @author Daniel
 */
public class AdministratorEditPart
		extends
		AbstractOfficeFloorEditPart<AdministratorModel, AdministratorEvent, OfficeFloorFigure>
		implements AdministratorFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createAdministratorFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getDuties());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// models.addAll(this.getCastedModel().getManagedObjects());
		// AdministratorToTeamModel team = this.getCastedModel().getTeam();
		// if (team != null) {
		// models.add(team);
		// }
	}

	@Override
	protected Class<AdministratorEvent> getPropertyChangeEventType() {
		return AdministratorEvent.class;
	}

	@Override
	protected void handlePropertyChange(AdministratorEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_DUTY:
		case REMOVE_DUTY:
			AdministratorEditPart.this.refreshChildren();
			break;
		// case ADD_MANAGED_OBJECT:
		// case REMOVE_MANAGED_OBJECT:
		// AdministratorEditPart.this.refreshSourceConnections();
		// break;
		// case CHANGE_TEAM:
		// AdministratorEditPart.this.refreshSourceConnections();
		// break;
		}
	}

	/*
	 * ==================== AdministratorFigureContext ======================
	 */

	@Override
	public String getAdministratorName() {
		return this.getCastedModel().getAdministratorName();
	}

}