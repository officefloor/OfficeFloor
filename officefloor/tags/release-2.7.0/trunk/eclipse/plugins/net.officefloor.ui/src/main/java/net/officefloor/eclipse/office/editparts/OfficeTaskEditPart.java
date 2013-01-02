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

package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.office.models.PostTaskAdministrationJointPointModel;
import net.officefloor.eclipse.office.models.PreTaskAdministrationJointPointModel;
import net.officefloor.eclipse.skin.office.OfficeTaskFigure;
import net.officefloor.eclipse.skin.office.OfficeTaskFigureContext;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.office.OfficeTaskModel.OfficeTaskEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeTaskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeTaskEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeTaskModel, OfficeTaskEvent, OfficeTaskFigure>
		implements OfficeTaskFigureContext {

	/**
	 * {@link PreTaskAdministrationJointPointModel}.
	 */
	private PreTaskAdministrationJointPointModel preTaskAdministrationJoinPoint;

	/**
	 * {@link PostTaskAdministrationJointPointModel}.
	 */
	private PostTaskAdministrationJointPointModel postTaskAdministrationJoinPoint;

	@Override
	protected void init() {
		// Create the task administration join points
		this.preTaskAdministrationJoinPoint = new PreTaskAdministrationJointPointModel(
				this.getCastedModel());
		this.postTaskAdministrationJoinPoint = new PostTaskAdministrationJointPointModel(
				this.getCastedModel());
	}

	@Override
	protected OfficeTaskFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeTaskFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		// Add task administration join points
		childModels.add(this.preTaskAdministrationJoinPoint);
		childModels.add(this.postTaskAdministrationJoinPoint);
	}

	@Override
	protected Class<OfficeTaskEvent> getPropertyChangeEventType() {
		return OfficeTaskEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeTaskEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_OFFICE_GOVERNANCE:
		case REMOVE_OFFICE_GOVERNANCE:
			// TODO add governance configuration
			break;

		case CHANGE_OFFICE_TASK_NAME:
			this.getOfficeFloorFigure().setOfficeTaskName(
					this.getOfficeTaskName());
			break;

		case ADD_PRE_DUTY:
		case REMOVE_PRE_DUTY:
			this.preTaskAdministrationJoinPoint.triggerDutyChangeEvent();
			break;

		case ADD_POST_DUTY:
		case REMOVE_POST_DUTY:
			this.postTaskAdministrationJoinPoint.triggerDutyChangeEvent();
			break;
		}
	}

	/*
	 * =================== OfficeTaskFigureContext =========================
	 */

	@Override
	public String getOfficeTaskName() {
		return this.getCastedModel().getOfficeTaskName();
	}

}