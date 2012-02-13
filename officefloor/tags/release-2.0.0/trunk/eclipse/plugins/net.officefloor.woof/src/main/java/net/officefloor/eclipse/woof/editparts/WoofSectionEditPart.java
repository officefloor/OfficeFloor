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
import net.officefloor.eclipse.skin.woof.SectionFigure;
import net.officefloor.eclipse.skin.woof.SectionFigureContext;
import net.officefloor.model.woof.WoofSectionModel;
import net.officefloor.model.woof.WoofSectionModel.WoofSectionEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofSectionEditPart
		extends
		AbstractOfficeFloorEditPart<WoofSectionModel, WoofSectionEvent, SectionFigure>
		implements SectionFigureContext {

	@Override
	protected SectionFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createSectionFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getInputs());
		childModels.addAll(this.getCastedModel().getOutputs());
	}

	@Override
	protected Class<WoofSectionEvent> getPropertyChangeEventType() {
		return WoofSectionEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofSectionEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_INPUT:
		case REMOVE_INPUT:
		case ADD_OUTPUT:
		case REMOVE_OUTPUT:
			this.refreshChildren();
			break;
		}
	}

	/*
	 * =========================== SectionFigureContext =======================
	 */

	@Override
	public String getSectionName() {
		return this.getCastedModel().getWoofSectionName();
	}

}