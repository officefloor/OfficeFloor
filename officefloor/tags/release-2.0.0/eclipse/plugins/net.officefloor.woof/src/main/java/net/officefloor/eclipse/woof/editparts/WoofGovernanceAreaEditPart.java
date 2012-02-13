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
import java.util.List;

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.woof.GovernanceAreaFigure;
import net.officefloor.eclipse.skin.woof.GovernanceAreaFigureContext;
import net.officefloor.model.woof.WoofGovernanceAreaModel;
import net.officefloor.model.woof.WoofGovernanceAreaModel.WoofGovernanceAreaEvent;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;

/**
 * {@link EditPart} for the {@link WoofGovernanceAreaModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofGovernanceAreaEditPart
		extends
		AbstractOfficeFloorEditPart<WoofGovernanceAreaModel, WoofGovernanceAreaEvent, GovernanceAreaFigure>
		implements GovernanceAreaFigureContext {

	/**
	 * Resizes the {@link WoofGovernanceAreaModel}.
	 * 
	 * @param bounds
	 *            Bounds for resizing.
	 */
	public void resize(Rectangle bounds) {

		// Resize the governance area
		WoofGovernanceAreaModel area = this.getCastedModel();
		area.setX(bounds.x);
		area.setY(bounds.y);
		area.setWidth(bounds.width);
		area.setHeight(bounds.height);

		// Resize the figure
		this.getOfficeFloorFigure().resize(bounds.width, bounds.height);
	}

	/*
	 * ================== AbstractOfficeFloorEditPart ===================
	 */

	@Override
	protected GovernanceAreaFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createGovernanceAreaFigure(this);
	}

	@Override
	protected Class<WoofGovernanceAreaEvent> getPropertyChangeEventType() {
		return WoofGovernanceAreaEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofGovernanceAreaEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_HEIGHT:
		case CHANGE_WIDTH:
			this.refresh();
			break;
		}
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Add the governance to area connection
		models.add(WoofGovernanceEditPart
				.getWoofGovernanceToWoofGovernanceArea(this.getCastedModel()));
	}

	@Override
	protected void createEditPolicies() {

		// Allow resizing
		this.createEditPolicies(false);

		// Enable resizing (handled by parent)
		this.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
				new ResizableEditPolicy());
	}

	/**
	 * Refresh the visuals.
	 */
	@Override
	protected void refreshVisuals() {

		// Obtain model for bounds refresh (possibly after resize)
		WoofGovernanceAreaModel model = this.getCastedModel();

		// Refresh the view off the model
		this.getFigure().setBounds(
				new Rectangle(model.getX(), model.getY(), model.getWidth(),
						model.getHeight()));
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