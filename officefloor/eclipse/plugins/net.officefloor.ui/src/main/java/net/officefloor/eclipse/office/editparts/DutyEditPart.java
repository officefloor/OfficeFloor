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
import net.officefloor.eclipse.skin.office.DutyFigure;
import net.officefloor.eclipse.skin.office.DutyFigureContext;
import net.officefloor.model.office.DutyModel;
import net.officefloor.model.office.DutyModel.DutyEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link DutyModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DutyEditPart extends
		AbstractOfficeFloorEditPart<DutyModel, DutyEvent, DutyFigure>
		implements DutyFigureContext {

	@Override
	protected DutyFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createDutyFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getPreOfficeTasks());
		models.addAll(this.getCastedModel().getPostOfficeTasks());
	}

	@Override
	protected Class<DutyEvent> getPropertyChangeEventType() {
		return DutyEvent.class;
	}

	@Override
	protected void handlePropertyChange(DutyEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_DUTY_NAME:
			this.getOfficeFloorFigure().setDutyName(this.getDutyName());
			break;

		case ADD_PRE_OFFICE_TASK:
		case REMOVE_PRE_OFFICE_TASK:
		case ADD_POST_OFFICE_TASK:
		case REMOVE_POST_OFFICE_TASK:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * =================== DutyFigureContext ========================
	 */

	@Override
	public String getDutyName() {
		return this.getCastedModel().getDutyName();
	}

}