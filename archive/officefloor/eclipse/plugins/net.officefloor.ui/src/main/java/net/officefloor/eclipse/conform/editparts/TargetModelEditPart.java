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
package net.officefloor.eclipse.conform.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.conform.figures.ConformModelFigure;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.conform.TargetModel;
import net.officefloor.model.conform.TargetModel.TargetEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link TargetModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class TargetModelEditPart
		extends
		AbstractOfficeFloorEditPart<TargetModel, TargetEvent, OfficeFloorFigure> {

	/*
	 * ================= AbstractOfficeFloorEditPart =========================
	 */

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return new ConformModelFigure();
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getTargetItems());
	}

	@Override
	protected Class<TargetEvent> getPropertyChangeEventType() {
		return TargetEvent.class;
	}

	@Override
	protected void handlePropertyChange(TargetEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_TARGET_ITEM:
		case REMOVE_TARGET_ITEM:
			this.refreshChildren();
			break;
		}
	}

}