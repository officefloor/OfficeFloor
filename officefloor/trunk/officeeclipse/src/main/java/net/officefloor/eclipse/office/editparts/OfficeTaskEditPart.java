/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.office.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.office.models.PostTaskAdministrationJointPointModel;
import net.officefloor.eclipse.office.models.PreTaskAdministrationJointPointModel;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.office.OfficeTaskFigureContext;
import net.officefloor.model.office.OfficeTaskModel;
import net.officefloor.model.office.OfficeTaskModel.OfficeTaskEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeTaskModel}.
 * 
 * @author Daniel
 */
public class OfficeTaskEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeTaskModel, OfficeTaskEvent, OfficeFloorFigure>
		implements OfficeTaskFigureContext {

	/**
	 * {@link PreTaskAdministrationJointPointModel}.
	 */
	private PreTaskAdministrationJointPointModel preFlowItemAdministrationJoinPoint;

	/**
	 * {@link PostTaskAdministrationJointPointModel}.
	 */
	private PostTaskAdministrationJointPointModel postFlowItemAdministrationJoinPoint;

	@Override
	protected void init() {
		// Create the flow item administration join points
		this.preFlowItemAdministrationJoinPoint = new PreTaskAdministrationJointPointModel(
				this.getCastedModel());
		this.postFlowItemAdministrationJoinPoint = new PostTaskAdministrationJointPointModel(
				this.getCastedModel());
	}

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeTaskFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		// Add flow item administration join points
		childModels.add(this.preFlowItemAdministrationJoinPoint);
		childModels.add(this.postFlowItemAdministrationJoinPoint);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// FlowItemToTeamModel team = this.getCastedModel().getTeam();
		// if (team != null) {
		// models.add(team);
		// }
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// models.addAll(this.getCastedModel().getDutyFlows());
	}

	@Override
	protected Class<OfficeTaskEvent> getPropertyChangeEventType() {
		return OfficeTaskEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeTaskEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		// case ADD_PRE_ADMIN_DUTY:
		// case REMOVE_PRE_ADMIN_DUTY:
		// FlowItemEditPart.this.preFlowItemAdministrationJoinPoint
		// .triggerRefreshConnections();
		// break;
		// case ADD_POST_ADMIN_DUTY:
		// case REMOVE_POST_ADMIN_DUTY:
		// FlowItemEditPart.this.postFlowItemAdministrationJoinPoint
		// .triggerRefreshConnections();
		// break;
		// case CHANGE_TEAM:
		// FlowItemEditPart.this.refreshSourceConnections();
		// break;
		// case ADD_DUTY_FLOW:
		// case REMOVE_DUTY_FLOW:
		// FlowItemEditPart.this.refreshTargetConnections();
		// break;
		}
	}

	/*
	 * =================== FlowItemFigureContext =========================
	 */

	@Override
	public String getOfficeTaskName() {
		return this.getCastedModel().getOfficeTaskName();
	}

}