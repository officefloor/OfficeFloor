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
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceTeamFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceTeamFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceTeamModel.OfficeFloorManagedObjectSourceTeamEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorManagedObjectSourceTeamEditPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagedObjectSourceTeamEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeFloorManagedObjectSourceTeamModel, OfficeFloorManagedObjectSourceTeamEvent, OfficeFloorManagedObjectSourceTeamFigure>
		implements OfficeFloorManagedObjectSourceTeamFigureContext {

	@Override
	protected OfficeFloorManagedObjectSourceTeamFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFloorManagedObjectSourceTeamFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeFloorTeam());
	}

	@Override
	protected Class<OfficeFloorManagedObjectSourceTeamEvent> getPropertyChangeEventType() {
		return OfficeFloorManagedObjectSourceTeamEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeFloorManagedObjectSourceTeamEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM_NAME:
			this.getOfficeFloorFigure()
					.setOfficeFloorManagedObjectSourceTeamName(
							this.getOfficeFloorManagedObjectSourceTeamName());
			break;

		case CHANGE_OFFICE_FLOOR_TEAM:
			OfficeFloorManagedObjectSourceTeamEditPart.this
					.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ================== ManagedObjectTeamFigureContext ======================
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceTeamName() {
		return this.getCastedModel()
				.getOfficeFloorManagedObjectSourceTeamName();
	}

}