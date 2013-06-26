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

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.woof.AccessInputFigure;
import net.officefloor.eclipse.skin.woof.AccessInputFigureContext;
import net.officefloor.model.woof.WoofAccessInputModel;
import net.officefloor.model.woof.WoofAccessInputModel.WoofAccessInputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofAccessInputEditPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofAccessInputEditPart
		extends
		AbstractOfficeFloorEditPart<WoofAccessInputModel, WoofAccessInputEvent, AccessInputFigure>
		implements AccessInputFigureContext {

	@Override
	protected AccessInputFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createAccessInputFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getWoofTemplateOutputs());
		models.addAll(this.getCastedModel().getWoofSectionOutputs());
	}

	@Override
	protected Class<WoofAccessInputEvent> getPropertyChangeEventType() {
		return WoofAccessInputEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofAccessInputEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_WOOF_ACCESS_INPUT_NAME:
			this.getOfficeFloorFigure().setAccessInputName(
					this.getCastedModel().getWoofAccessInputName());
			break;

		case ADD_WOOF_TEMPLATE_OUTPUT:
		case REMOVE_WOOF_TEMPLATE_OUTPUT:
		case ADD_WOOF_SECTION_OUTPUT:
		case REMOVE_WOOF_SECTION_OUTPUT:
			this.refreshTargetConnections();
			break;

		case CHANGE_PARAMETER_TYPE:
			// No visual change
			break;
		}
	}

	/*
	 * ==================== AccessInputFigureContext ==============
	 */

	@Override
	public String getAccessInputName() {
		return this.getCastedModel().getWoofAccessInputName();
	}

}