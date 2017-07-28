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

import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.office.models.PostFunctionAdministrationJointPointModel;
import net.officefloor.eclipse.office.models.PreFunctionAdministrationJointPointModel;
import net.officefloor.eclipse.skin.office.OfficeFunctionFigure;
import net.officefloor.eclipse.skin.office.OfficeFunctionFigureContext;
import net.officefloor.model.office.OfficeFunctionModel;
import net.officefloor.model.office.OfficeFunctionModel.OfficeFunctionEvent;

/**
 * {@link EditPart} for the {@link OfficeFunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFunctionEditPart
		extends AbstractOfficeFloorEditPart<OfficeFunctionModel, OfficeFunctionEvent, OfficeFunctionFigure>
		implements OfficeFunctionFigureContext {

	/**
	 * {@link PreFunctionAdministrationJointPointModel}.
	 */
	private PreFunctionAdministrationJointPointModel preFunctionAdministrationJoinPoint;

	/**
	 * {@link PostFunctionAdministrationJointPointModel}.
	 */
	private PostFunctionAdministrationJointPointModel postFunctionAdministrationJoinPoint;

	@Override
	protected void init() {
		// Create the function administration join points
		this.preFunctionAdministrationJoinPoint = new PreFunctionAdministrationJointPointModel(this.getCastedModel());
		this.postFunctionAdministrationJoinPoint = new PostFunctionAdministrationJointPointModel(this.getCastedModel());
	}

	@Override
	protected OfficeFunctionFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory().createOfficeFunctionFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		// Add task administration join points
		childModels.add(this.preFunctionAdministrationJoinPoint);
		childModels.add(this.postFunctionAdministrationJoinPoint);
	}

	@Override
	protected Class<OfficeFunctionEvent> getPropertyChangeEventType() {
		return OfficeFunctionEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeFunctionEvent property, PropertyChangeEvent evt) {
		switch (property) {

		case CHANGE_OFFICE_FUNCTION_NAME:
			this.getOfficeFloorFigure().setOfficeFunctionName(this.getOfficeFunctionName());
			break;

		case ADD_PRE_ADMINISTRATION:
		case REMOVE_PRE_ADMINISTRATION:
			this.preFunctionAdministrationJoinPoint.triggerDutyChangeEvent();
			break;

		case ADD_POST_ADMINISTRATION:
		case REMOVE_POST_ADMINISTRATION:
			this.postFunctionAdministrationJoinPoint.triggerDutyChangeEvent();
			break;
		}
	}

	/*
	 * =================== OfficeFunctionFigureContext =========================
	 */

	@Override
	public String getOfficeFunctionName() {
		return this.getCastedModel().getOfficeFunctionName();
	}

}