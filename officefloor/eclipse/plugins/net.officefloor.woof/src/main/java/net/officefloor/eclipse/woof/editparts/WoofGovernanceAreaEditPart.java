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
package net.officefloor.eclipse.woof.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.woof.GovernanceAreaFigure;
import net.officefloor.eclipse.skin.woof.GovernanceAreaFigureContext;
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel;
import net.officefloor.woof.model.woof.WoofGovernanceAreaModel.WoofGovernanceAreaEvent;

/**
 * {@link EditPart} for the {@link WoofGovernanceAreaModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofGovernanceAreaEditPart
		extends AbstractOfficeFloorEditPart<WoofGovernanceAreaModel, WoofGovernanceAreaEvent, GovernanceAreaFigure>
		implements GovernanceAreaFigureContext {

	/*
	 * ================== AbstractOfficeFloorEditPart ===================
	 */

	@Override
	protected GovernanceAreaFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory().createGovernanceAreaFigure(this);
	}

	@Override
	protected Class<WoofGovernanceAreaEvent> getPropertyChangeEventType() {
		return WoofGovernanceAreaEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofGovernanceAreaEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_HEIGHT:
		case CHANGE_WIDTH:
			WoofGovernanceAreaModel area = this.getCastedModel();
			this.getOfficeFloorFigure().resize(area.getWidth(), area.getHeight());
			this.refresh();
			break;

		case CHANGE_GOVERNANCE_CONNECTION:
			// Should not be able to change owning Governance
			break;
		}
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Add the governance to area connection
		models.add(WoofGovernanceEditPart.getWoofGovernanceToWoofGovernanceArea(this.getCastedModel()));
	}

	/**
	 * Refresh the visuals.
	 */
	@Override
	protected void refreshVisuals() {

		// Obtain model for bounds refresh (possibly after resize)
		WoofGovernanceAreaModel model = this.getCastedModel();

		// Refresh the view off bounds defined by the model
		this.getFigure().setBounds(new Rectangle(model.getX(), model.getY(), model.getWidth(), model.getHeight()));
	}

	/*
	 * ======================= GovernanceAreaFigureContext ================
	 */

	@Override
	public int getWidth() {
		return this.getCastedModel().getWidth();
	}

	@Override
	public int getHeight() {
		return this.getCastedModel().getHeight();
	}

}