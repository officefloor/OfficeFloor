/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceTeamFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceTeamFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel.OfficeManagedObjectSourceTeamEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeManagedObjectSourceTeamEditPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagedObjectSourceTeamEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeManagedObjectSourceTeamModel, OfficeManagedObjectSourceTeamEvent, OfficeManagedObjectSourceTeamFigure>
		implements OfficeManagedObjectSourceTeamFigureContext {

	@Override
	protected OfficeManagedObjectSourceTeamFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeManagedObjectSourceTeamFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getOfficeTeam());
	}

	@Override
	protected Class<OfficeManagedObjectSourceTeamEvent> getPropertyChangeEventType() {
		return OfficeManagedObjectSourceTeamEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeManagedObjectSourceTeamEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_MANAGED_OBJECT_SOURCE_TEAM_NAME:
			this.getOfficeFloorFigure().setOfficeManagedObjectSourceTeamName(
					this.getOfficeManagedObjectSourceTeamName());
			break;

		case CHANGE_OFFICE_TEAM:
			this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ============== OfficeManagedObjectSourceTeamFigureContext =============
	 */

	@Override
	public String getOfficeManagedObjectSourceTeamName() {
		return this.getCastedModel().getOfficeManagedObjectSourceTeamName();
	}

}