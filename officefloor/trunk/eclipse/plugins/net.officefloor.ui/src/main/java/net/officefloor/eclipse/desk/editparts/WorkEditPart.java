/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.skin.desk.WorkFigure;
import net.officefloor.eclipse.skin.desk.WorkFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkToInitialTaskModel;
import net.officefloor.model.desk.WorkModel.WorkEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WorkModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkEditPart extends
		AbstractOfficeFloorEditPart<WorkModel, WorkEvent, WorkFigure> implements
		WorkFigureContext {

	@Override
	protected WorkFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createWorkFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getWorkTasks());
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
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<WorkModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<DeskChanges, WorkModel>() {
			@Override
			public String getInitialValue() {
				return WorkEditPart.this.getCastedModel().getWorkName();
			}

			@Override
			public IFigure getLocationFigure() {
				return WorkEditPart.this.getOfficeFloorFigure()
						.getWorkNameFigure();
			}

			@Override
			public Change<WorkModel> createChange(DeskChanges changes,
					WorkModel target, String newValue) {
				return changes.renameWork(target, newValue);
			}
		});
	}

	@Override
	protected Class<WorkEvent> getPropertyChangeEventType() {
		return WorkEvent.class;
	}

	@Override
	protected void handlePropertyChange(WorkEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_WORK_NAME:
			this.getOfficeFloorFigure().setWorkName(
					this.getCastedModel().getWorkName());
			break;
		case CHANGE_INITIAL_TASK:
			WorkEditPart.this.refreshSourceConnections();
			break;
		case ADD_WORK_TASK:
		case REMOVE_WORK_TASK:
			WorkEditPart.this.refreshChildren();
			break;
		}
	}

	/*
	 * =============== DeskWorkFigureContext =======================
	 */

	@Override
	public String getWorkName() {
		return this.getCastedModel().getWorkName();
	}

}