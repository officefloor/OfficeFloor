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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.action.OperationUtil;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.desk.operations.CreateFlowItemFromDeskTaskOperation;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.WorkTaskFigureContext;
import net.officefloor.model.desk.WorkTaskModel;
import net.officefloor.model.desk.WorkTaskModel.WorkTaskEvent;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WorkTaskModel}.
 * 
 * @author Daniel
 */
public class WorkTaskEditPart
		extends
		AbstractOfficeFloorEditPart<WorkTaskModel, WorkTaskEvent, OfficeFloorFigure>
		implements WorkTaskFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createWorkTaskFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getTaskObjects());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		models.addAll(this.getCastedModel().getTasks());
	}

	@Override
	protected Class<WorkTaskEvent> getPropertyChangeEventType() {
		return WorkTaskEvent.class;
	}

	@Override
	protected void handlePropertyChange(WorkTaskEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_TASK:
		case REMOVE_TASK:
			WorkTaskEditPart.this.refreshSourceConnections();
			break;
		case ADD_TASK_OBJECT:
		case REMOVE_TASK_OBJECT:
			WorkTaskEditPart.this.refreshChildren();
			break;
		}
	}

	/*
	 * ============ DeskTaskFigureContext ============
	 */

	@Override
	public String getWorkTaskName() {
		return this.getCastedModel().getWorkTaskName();
	}

	@Override
	public void createAsNewTask() {
		OperationUtil.execute(new CreateFlowItemFromDeskTaskOperation(), 100,
				100, this);
	}

}