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
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorTeamFigureContext;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel.OfficeFloorTeamEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorTeamModel}.
 * 
 * @author Daniel
 */
public class OfficeFloorTeamEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeFloorTeamModel, OfficeFloorTeamEvent, OfficeFloorFigure>
		implements OfficeFloorTeamFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFloorTeamFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getDeployedOfficeTeams());
		models.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceTeams());
	}

	@Override
	protected Class<OfficeFloorTeamEvent> getPropertyChangeEventType() {
		return OfficeFloorTeamEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeFloorTeamEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
		case ADD_DEPLOYED_OFFICE_TEAM:
		case REMOVE_DEPLOYED_OFFICE_TEAM:
			OfficeFloorTeamEditPart.this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ======================== TeamFigureContext ============================
	 */

	@Override
	public String getOfficeFloorTeamName() {
		return this.getCastedModel().getOfficeFloorTeamName();
	}

}