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

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigure;
import net.officefloor.eclipse.skin.desk.ExternalFlowFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalFlowModel.ExternalFlowEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ExternalFlowModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExternalFlowEditPart
		extends
		AbstractOfficeFloorEditPart<ExternalFlowModel, ExternalFlowEvent, ExternalFlowFigure>
		implements ExternalFlowFigureContext {

	@Override
	protected ExternalFlowFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createExternalFlowFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getTaskFlows());
		models.addAll(this.getCastedModel().getTaskEscalations());
		models.addAll(this.getCastedModel().getPreviousTasks());
		models.addAll(this.getCastedModel().getDeskManagedObjectSourceFlows());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<ExternalFlowModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<DeskChanges, ExternalFlowModel>() {
			@Override
			public String getInitialValue() {
				return ExternalFlowEditPart.this.getCastedModel()
						.getExternalFlowName();
			}

			@Override
			public IFigure getLocationFigure() {
				return ExternalFlowEditPart.this.getOfficeFloorFigure()
						.getExternalFlowNameFigure();
			}

			@Override
			public Change<ExternalFlowModel> createChange(DeskChanges changes,
					ExternalFlowModel target, String newValue) {
				return changes.renameExternalFlow(target, newValue);
			}
		});
	}

	@Override
	protected Class<ExternalFlowEvent> getPropertyChangeEventType() {
		return ExternalFlowEvent.class;
	}

	@Override
	protected void handlePropertyChange(ExternalFlowEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_EXTERNAL_FLOW_NAME:
			this.getOfficeFloorFigure().setExternalFlowName(
					this.getCastedModel().getExternalFlowName());
			break;

		case ADD_TASK_FLOW:
		case REMOVE_TASK_FLOW:
		case ADD_TASK_ESCALATION:
		case REMOVE_TASK_ESCALATION:
		case ADD_PREVIOUS_TASK:
		case REMOVE_PREVIOUS_TASK:
		case ADD_DESK_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_DESK_MANAGED_OBJECT_SOURCE_FLOW:
			this.refreshTargetConnections();
			break;

		case CHANGE_ARGUMENT_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ================= ExternalFlowFigureContext =====================
	 */

	@Override
	public String getExternalFlowName() {
		return this.getCastedModel().getExternalFlowName();
	}

	@Override
	public String getArgumentTypeName() {
		return this.getCastedModel().getArgumentType();
	}

}