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
import net.officefloor.eclipse.skin.woof.SectionInputFigure;
import net.officefloor.eclipse.skin.woof.SectionInputFigureContext;
import net.officefloor.model.woof.WoofSectionInputModel;
import net.officefloor.model.woof.WoofSectionInputModel.WoofSectionInputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofSectionInputEditPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSectionInputEditPart
		extends
		AbstractOfficeFloorEditPart<WoofSectionInputModel, WoofSectionInputEvent, SectionInputFigure>
		implements SectionInputFigureContext {

	@Override
	protected SectionInputFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createSectionInputFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getWoofTemplateOutputs());
		models.addAll(this.getCastedModel().getWoofSectionOutputs());
		models.addAll(this.getCastedModel().getWoofExceptions());
	}

	@Override
	protected Class<WoofSectionInputEvent> getPropertyChangeEventType() {
		return WoofSectionInputEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofSectionInputEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_WOOF_TEMPLATE_OUTPUT:
		case REMOVE_WOOF_TEMPLATE_OUTPUT:
		case ADD_WOOF_SECTION_OUTPUT:
		case REMOVE_WOOF_SECTION_OUTPUT:
		case ADD_WOOF_EXCEPTION:
		case REMOVE_WOOF_EXCEPTION:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * ==================== SectionInputFigureContext ==============
	 */

	@Override
	public String getSectionInputName() {
		return this.getCastedModel().getWoofSectionInputName();
	}

}