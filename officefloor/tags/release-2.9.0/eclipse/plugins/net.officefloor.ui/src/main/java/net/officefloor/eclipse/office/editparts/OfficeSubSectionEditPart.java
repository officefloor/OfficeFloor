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
import net.officefloor.eclipse.skin.office.OfficeSubSectionFigure;
import net.officefloor.eclipse.skin.office.OfficeSubSectionFigureContext;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeSubSectionModel.OfficeSubSectionEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeSubSectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSubSectionEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeSubSectionModel, OfficeSubSectionEvent, OfficeSubSectionFigure>
		implements OfficeSubSectionFigureContext {

	/*
	 * ==================== AbstractOfficeFloorEditPart ===================
	 */

	@Override
	protected OfficeSubSectionFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeSubSectionFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getOfficeTasks());
		childModels.addAll(this.getCastedModel().getOfficeSubSections());
	}

	@Override
	protected Class<OfficeSubSectionEvent> getPropertyChangeEventType() {
		return OfficeSubSectionEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeSubSectionEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_SUB_SECTION_NAME:
			this.getOfficeFloorFigure().setOfficeSubSectionName(
					this.getOfficeSubSectionName());
			break;

		case ADD_OFFICE_TASK:
		case REMOVE_OFFICE_TASK:
		case ADD_OFFICE_SUB_SECTION:
		case REMOVE_OFFICE_SUB_SECTION:
			this.refreshChildren();
			break;

		case ADD_OFFICE_GOVERNANCE:
		case REMOVE_OFFICE_GOVERNANCE:
		case ADD_OFFICE_SECTION_MANAGED_OBJECT:
		case REMOVE_OFFICE_SECTION_MANAGED_OBJECT:
			// TODO add governance configuration
			break;
		}
	}

	/*
	 * ===================== OfficeSubSectionFigureContext =================
	 */

	@Override
	public String getOfficeSubSectionName() {
		return this.getCastedModel().getOfficeSubSectionName();
	}

}