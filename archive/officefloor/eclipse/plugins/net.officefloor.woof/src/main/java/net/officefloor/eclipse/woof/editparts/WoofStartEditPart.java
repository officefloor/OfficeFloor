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

import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.woof.StartFigure;
import net.officefloor.eclipse.skin.woof.StartFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.woof.model.woof.WoofStartModel;
import net.officefloor.woof.model.woof.WoofStartModel.WoofStartEvent;

/**
 * {@link EditPart} for the {@link WoofStartModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofStartEditPart extends AbstractOfficeFloorEditPart<WoofStartModel, WoofStartEvent, StartFigure>
		implements StartFigureContext {

	@Override
	protected StartFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory().createStartFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getWoofSectionInput());
	}

	@Override
	protected Class<WoofStartEvent> getPropertyChangeEventType() {
		return WoofStartEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofStartEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_WOOF_SECTION_INPUT:
			this.refreshSourceConnections();
			break;
		}
	}

}