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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.skin.desk.WorkFigure;
import net.officefloor.eclipse.skin.desk.WorkFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkModel.WorkEvent;
import net.officefloor.model.desk.WorkToInitialTaskModel;

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

	/**
	 * Opens the {@link WorkSource} for the {@link WorkModel}.
	 * 
	 * @param work
	 *            {@link WorkModel}.
	 * @param context
	 *            {@link OpenHandlerContext}.
	 */
	public static void openWorkSource(WorkModel work,
			OpenHandlerContext<?> context) {

		// Obtain the details about the work
		String workSourceClassName = work.getWorkSourceClassName();
		PropertyList properties = context.createPropertyList();
		for (PropertyModel property : work.getProperties()) {
			properties.addProperty(property.getName()).setValue(
					property.getValue());
		}

		// Open the work source
		ExtensionUtil.openWorkSource(workSourceClassName, properties, context
				.getEditPart().getEditor());
	}

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
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<WorkModel> policy) {
		policy.allowOpening(new OpenHandler<WorkModel>() {
			@Override
			public void doOpen(OpenHandlerContext<WorkModel> context) {
				WorkEditPart.openWorkSource(context.getModel(), context);
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

		case CHANGE_WORK_SOURCE_CLASS_NAME:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
			// Non visual change
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