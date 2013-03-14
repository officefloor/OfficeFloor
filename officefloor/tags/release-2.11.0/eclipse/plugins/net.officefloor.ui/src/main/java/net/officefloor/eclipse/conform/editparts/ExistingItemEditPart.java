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
import net.officefloor.eclipse.conform.figures.ExistingConformModelItemFigure;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.conform.ExistingItemModel;
import net.officefloor.model.conform.ExistingItemModel.ExistingItemEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for an {@link ExistingItemModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExistingItemEditPart
		extends
		AbstractOfficeFloorEditPart<ExistingItemModel, ExistingItemEvent, ExistingConformModelItemFigure> {

	/*
	 * ===================== AbstractOfficeFloorEditPart =======================
	 */

	@Override
	protected ExistingConformModelItemFigure createOfficeFloorFigure() {
		return new ExistingConformModelItemFigure(this.getCastedModel()
				.getExistingItemName());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getTargetItem());
	}

	@Override
	protected Class<ExistingItemEvent> getPropertyChangeEventType() {
		return ExistingItemEvent.class;
	}

	@Override
	protected void handlePropertyChange(ExistingItemEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_TARGET_ITEM:
			this.refreshSourceConnections();
			break;

		case CHANGE_EXISTING_ITEM_NAME:
			// Name should not change, but reflect if does
			this.getOfficeFloorFigure().setItemName(
					this.getCastedModel().getExistingItemName());
			break;
		}
	}

}