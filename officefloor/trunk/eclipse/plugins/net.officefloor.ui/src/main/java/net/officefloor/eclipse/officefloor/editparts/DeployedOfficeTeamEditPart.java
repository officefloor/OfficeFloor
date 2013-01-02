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
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeTeamFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeTeamFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel;
import net.officefloor.model.officefloor.DeployedOfficeTeamModel.DeployedOfficeTeamEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link DeployedOfficeTeamModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeployedOfficeTeamEditPart
		extends
		AbstractOfficeFloorEditPart<DeployedOfficeTeamModel, DeployedOfficeTeamEvent, DeployedOfficeTeamFigure>
		implements DeployedOfficeTeamFigureContext {

	@Override
	protected DeployedOfficeTeamFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createDeployedOfficeTeamFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeFloorTeam());
	}

	@Override
	protected Class<DeployedOfficeTeamEvent> getPropertyChangeEventType() {
		return DeployedOfficeTeamEvent.class;
	}

	@Override
	protected void handlePropertyChange(DeployedOfficeTeamEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_DEPLOYED_OFFICE_TEAM_NAME:
			this.getOfficeFloorFigure().setDeployedOfficeTeamName(
					this.getDeployedOfficeTeamName());
			break;

		case CHANGE_OFFICE_FLOOR_TEAM:
			DeployedOfficeTeamEditPart.this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ================ OfficeTeamFigureContext =======================
	 */

	@Override
	public String getDeployedOfficeTeamName() {
		return this.getCastedModel().getDeployedOfficeTeamName();
	}

}