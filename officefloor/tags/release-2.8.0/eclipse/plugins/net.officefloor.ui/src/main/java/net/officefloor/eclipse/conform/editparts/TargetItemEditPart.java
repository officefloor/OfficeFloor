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
import net.officefloor.eclipse.conform.figures.ConformModelItemFigure;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.conform.TargetItemModel;
import net.officefloor.model.conform.TargetItemModel.TargetItemEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link TargetItemModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class TargetItemEditPart
		extends
		AbstractOfficeFloorEditPart<TargetItemModel, TargetItemEvent, ConformModelItemFigure> {

	/*
	 * ================== AbstractOfficeFloorEditPart ======================
	 */

	@Override
	protected ConformModelItemFigure createOfficeFloorFigure() {
		return new ConformModelItemFigure(this.getCastedModel()
				.getTargetItemName(), false);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getExistingItem());
	}

	@Override
	protected Class<TargetItemEvent> getPropertyChangeEventType() {
		return TargetItemEvent.class;
	}

	@Override
	protected void handlePropertyChange(TargetItemEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_EXISTING_ITEM:
			this.refreshTargetConnections();
			break;
			
		case CHANGE_TARGET_ITEM_NAME:
			// Name should not change, but reflect if does
			this.getOfficeFloorFigure().setItemName(
					this.getCastedModel().getTargetItemName());
			break;
		}
	}

}