/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.woof.GovernanceFigure;
import net.officefloor.eclipse.skin.woof.GovernanceFigureContext;
import net.officefloor.model.woof.WoofGovernanceModel;
import net.officefloor.model.woof.WoofGovernanceModel.WoofGovernanceEvent;

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
			// TODO handle
			break;
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