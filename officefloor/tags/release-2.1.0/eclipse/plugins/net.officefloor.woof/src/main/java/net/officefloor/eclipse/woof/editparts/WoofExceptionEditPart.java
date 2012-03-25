/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import net.officefloor.eclipse.skin.woof.ExceptionFigure;
import net.officefloor.eclipse.skin.woof.ExceptionFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.woof.WoofExceptionModel;
import net.officefloor.model.woof.WoofExceptionModel.WoofExceptionEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofExceptionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofExceptionEditPart
		extends
		AbstractOfficeFloorEditPart<WoofExceptionModel, WoofExceptionEvent, ExceptionFigure>
		implements ExceptionFigureContext {

	@Override
	protected ExceptionFigure createOfficeFloorFigure() {
		return WoofPlugin.getSkin().getWoofFigureFactory()
				.createExceptionFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getWoofTemplate());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getWoofSectionInput());
		EclipseUtil.addToList(models, this.getCastedModel().getWoofResource());
	}

	@Override
	protected Class<WoofExceptionEvent> getPropertyChangeEventType() {
		return WoofExceptionEvent.class;
	}

	@Override
	protected void handlePropertyChange(WoofExceptionEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_WOOF_TEMPLATE:
		case CHANGE_WOOF_SECTION_INPUT:
		case CHANGE_WOOF_RESOURCE:
			this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ======================== ExceptionFigureContext =======================
	 */

	@Override
	public String getExceptionName() {

		// Obtain the simple class name
		String className = this.getCastedModel().getClassName();
		String[] segments = className.split("\\.");
		String simpleClassName = segments[segments.length - 1];

		// Return the simple class name
		return simpleClassName;
	}

}