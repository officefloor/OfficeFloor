/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.eclipse.woof.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.woof.GovernanceFigure;
import net.officefloor.eclipse.skin.woof.GovernanceFigureContext;
import net.officefloor.model.woof.WoofGovernanceAreaModel;
import net.officefloor.model.woof.WoofGovernanceModel;
import net.officefloor.model.woof.WoofGovernanceModel.WoofGovernanceEvent;
import net.officefloor.model.woof.WoofGovernanceToWoofGovernanceAreaModel;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofGovernanceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofGovernanceEditPart
		extends
		AbstractOfficeFloorEditPart<WoofGovernanceModel, WoofGovernanceEvent, GovernanceFigure>
		implements GovernanceFigureContext {

	/**
	 * Obtains the {@link WoofGovernanceToWoofGovernanceAreaModel} from the
	 * {@link WoofGovernanceAreaModel}.
	 * 
	 * @param area
	 *            {@link WoofGovernanceAreaModel}.
	 * @return {@link WoofGovernanceToWoofGovernanceAreaModel}.
	 */
	public static WoofGovernanceToWoofGovernanceAreaModel getWoofGovernanceToWoofGovernanceArea(
			WoofGovernanceAreaModel area) {
		WoofGovernanceToWoofGovernanceAreaModel conn = area
				.getGovernanceConnection();
		if (conn == null) {
			conn = new WoofGovernanceToWoofGovernanceAreaModel();
			area.setGovernanceConnection(conn);
		}
		return conn;
	}

	/*
	 * ====================== AbstractOfficeFloorEditPart =================
	 */

	@Override
	protected GovernanceFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createGovernanceFigure(this);
	}

	@Override
	protected Class<WoofGovernanceEvent> getPropertyChangeEventType() {
		return WoofGovernanceEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofGovernanceEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_WOOF_GOVERNANCE_NAME:
			this.getOfficeFloorFigure().setGovernanceName(
					this.getGovernanceName());
			break;
		case ADD_GOVERNANCE_AREA:
		case REMOVE_GOVERNANCE_AREA:
			// Refresh woof as area added graphically to top level
			WoofEditPart woofEditPart = (WoofEditPart) this.getParent();
			woofEditPart.refresh();

			// Refresh connections to areas
			this.refreshSourceConnections();
			break;
		}
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Load the listing of governance to area connections
		for (WoofGovernanceAreaModel area : this.getCastedModel()
				.getGovernanceAreas()) {
			WoofGovernanceToWoofGovernanceAreaModel conn = getWoofGovernanceToWoofGovernanceArea(area);
			models.add(conn);
		}
	}

	/*
	 * ======================== GovernanceFigureContext =======================
	 */

	@Override
	public String getGovernanceName() {
		return this.getCastedModel().getWoofGovernanceName();
	}

}