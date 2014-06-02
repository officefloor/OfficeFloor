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
import net.officefloor.eclipse.conform.figures.TargetConformModelItemFigure;
import net.officefloor.eclipse.conform.figures.TargetConformModelItemFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.conform.ExistingItemToTargetItemModel;
import net.officefloor.model.conform.TargetItemModel;
import net.officefloor.model.conform.TargetItemModel.TargetItemEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link TargetItemModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class TargetItemEditPart
		extends
		AbstractOfficeFloorEditPart<TargetItemModel, TargetItemEvent, TargetConformModelItemFigure>
		implements TargetConformModelItemFigureContext {

	/*
	 * ================== AbstractOfficeFloorEditPart ======================
	 */

	@Override
	protected TargetConformModelItemFigure createOfficeFloorFigure() {
		return new TargetConformModelItemFigure(this);
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
			this.getOfficeFloorFigure().setItemName(this.getTargetItemName());
			break;

		case CHANGE_IS_INHERITABLE:
			// Flag whether may inherit
			this.getOfficeFloorFigure().setInheritable(this.isInheritable());
			break;

		case CHANGE_INHERIT:
			// Flag whether inheriting
			this.getOfficeFloorFigure().setInherit(this.isInherit());
			break;
		}
	}

	/*
	 * =============== TargetConformModelItemFigureContext ================
	 */

	@Override
	public String getTargetItemName() {
		return this.getCastedModel().getTargetItemName();
	}

	@Override
	public boolean isInheritable() {
		return this.getCastedModel().getIsInheritable();
	}

	@Override
	public boolean isInherit() {
		return this.getCastedModel().getInherit();
	}

	@Override
	public void setInherit(boolean isInherit) {

		// Obtain the item
		TargetItemModel item = this.getCastedModel();

		// Determine if action is required
		if (isInherit == item.getInherit()) {
			return; // No change required
		}

		// Change required
		item.setInherit(isInherit);
		if (isInherit) {
			// Inheriting configuration, so remove possible configuration
			ExistingItemToTargetItemModel connection = item.getExistingItem();
			if (connection != null) {
				connection.remove();
			}
		}
	}

	@Override
	public void setLayoutConstraint(IFigure figure, Object layoutConstraint) {

		// Obtain the parent edit part
		TargetModelEditPart parentEditPart = (TargetModelEditPart) this
				.getParent();

		// Provide constraint for the figure
		parentEditPart.getOfficeFloorFigure().getFigure().getLayoutManager()
				.setConstraint(figure, layoutConstraint);
	}

}