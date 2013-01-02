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
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.skin.office.OfficeStartFigure;
import net.officefloor.eclipse.skin.office.OfficeStartFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartModel.OfficeStartEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeStartModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeStartEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeStartModel, OfficeStartEvent, OfficeStartFigure>
		implements OfficeStartFigureContext {

	@Override
	protected OfficeStartFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeStartFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeSectionInput());
	}

	@Override
	protected Class<OfficeStartEvent> getPropertyChangeEventType() {
		return OfficeStartEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeStartEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_SECTION_INPUT:
			this.refreshSourceConnections();
			break;

		case CHANGE_START_NAME:
			// Non visual change
			break;
		}
	}

}