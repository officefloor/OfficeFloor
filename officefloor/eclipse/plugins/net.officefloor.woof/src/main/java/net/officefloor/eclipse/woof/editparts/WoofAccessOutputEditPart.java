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
import net.officefloor.eclipse.skin.woof.AccessOutputFigure;
import net.officefloor.eclipse.skin.woof.AccessOutputFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.woof.WoofAccessOutputModel;
import net.officefloor.model.woof.WoofAccessOutputModel.WoofAccessOutputEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofAccessOutputEditPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofAccessOutputEditPart
		extends
		AbstractOfficeFloorEditPart<WoofAccessOutputModel, WoofAccessOutputEvent, AccessOutputFigure>
		implements AccessOutputFigureContext {

	@Override
	protected AccessOutputFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createAccessOutputFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getWoofTemplate());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getWoofSectionInput());
		EclipseUtil.addToList(models, this.getCastedModel().getWoofResource());
	}

	@Override
	protected Class<WoofAccessOutputEvent> getPropertyChangeEventType() {
		return WoofAccessOutputEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofAccessOutputEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_WOOF_ACCESS_OUTPUT_NAME:
			this.getOfficeFloorFigure().setAccessOutputName(
					this.getCastedModel().getWoofAccessOutputName());
			break;

		case CHANGE_WOOF_TEMPLATE:
		case CHANGE_WOOF_SECTION_INPUT:
		case CHANGE_WOOF_RESOURCE:
			this.refreshSourceConnections();
			break;

		case CHANGE_ARGUMENT_TYPE:
			// No visual change
			break;
		}
	}

	/*
	 * ==================== AccessOutputFigureContext ==============
	 */

	@Override
	public String getAccessOutputName() {
		return this.getCastedModel().getWoofAccessOutputName();
	}

}