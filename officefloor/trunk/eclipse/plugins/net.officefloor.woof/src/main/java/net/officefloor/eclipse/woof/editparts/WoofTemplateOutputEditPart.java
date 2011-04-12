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
import net.officefloor.eclipse.skin.woof.TemplateOutputFigure;
import net.officefloor.eclipse.skin.woof.TemplateOutputFigureContext;
import net.officefloor.model.woof.WoofTemplateOutputModel;
import net.officefloor.model.woof.WoofTemplateOutputModel.WoofTemplateOutputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofTemplateOutputEditPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateOutputEditPart
		extends
		AbstractOfficeFloorEditPart<WoofTemplateOutputModel, WoofTemplateOutputEvent, TemplateOutputFigure>
		implements TemplateOutputFigureContext {

	@Override
	protected TemplateOutputFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createTemplateOutputFigure(this);
	}

	@Override
	protected Class<WoofTemplateOutputEvent> getPropertyChangeEventType() {
		return WoofTemplateOutputEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofTemplateOutputEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_WOOF_TEMPLATE:
		case CHANGE_WOOF_SECTION_INPUT:
		case CHANGE_WOOF_RESOURCE:
			// TODO refresh connections
			break;
		}
	}

	/*
	 * ==================== TemplateOutputFigureContext ==============
	 */

	@Override
	public String getTemplateOutputName() {
		return this.getCastedModel().getWoofTemplateOutputName();
	}

}