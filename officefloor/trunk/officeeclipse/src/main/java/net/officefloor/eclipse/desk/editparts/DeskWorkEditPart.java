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
import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editparts.RemovableEditPart;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.desk.operations.RemoveWorkOperation;
import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.eclipse.skin.desk.DeskWorkFigureContext;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkToInitialTaskModel;
import net.officefloor.model.desk.WorkModel.WorkEvent;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link EditPart} for the {@link WorkModel}.
 * 
 * @author Daniel
 */
// TODO rename to WorkEditPart
public class DeskWorkEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<WorkModel, OfficeFloorFigure>
		implements RemovableEditPart, DeskWorkFigureContext {

	@Override
	protected OfficeFloorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createDeskWorkFigure(this);
	}

	@Override
	protected boolean isFreeformFigure() {
		return true;
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getWorkTasks());
	}

	@Override
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {
				WorkToInitialTaskModel conn = new WorkToInitialTaskModel();
				conn.setWork((WorkModel) source);
				conn.setInitialTask((TaskModel) target);
				conn.connect();
				return conn;
			}
		};
	}

	@Override
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(TaskModel.class);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		WorkToInitialTaskModel initialTask = this.getCastedModel()
				.getInitialTask();
		if (initialTask != null) {
			models.add(initialTask);
		}
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		// Not a target
	}

	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<WorkEvent>(WorkEvent.values()) {
			protected void handlePropertyChange(WorkEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_INITIAL_TASK:
					DeskWorkEditPart.this.refreshSourceConnections();
					break;
				case ADD_WORK_TASK:
				case REMOVE_WORK_TASK:
					DeskWorkEditPart.this.refreshChildren();
					break;
				}
			}
		});
	}

	@Override
	public Operation getRemoveOperation() {
		return new RemoveWorkOperation();
	}

	/*
	 * =============== DeskWorkFigureContext =======================
	 */

	@Override
	public String getWorkName() {
		return this.getCastedModel().getWorkName();
	}

}
