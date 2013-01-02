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
package net.officefloor.eclipse.officefloor.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorTeamFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorTeamFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.OfficeFloorTeamModel.OfficeFloorTeamEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorTeamModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorTeamEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeFloorTeamModel, OfficeFloorTeamEvent, OfficeFloorTeamFigure>
		implements OfficeFloorTeamFigureContext {

	@Override
	protected OfficeFloorTeamFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFloorTeamFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getDeployedOfficeTeams());
		models.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceTeams());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<OfficeFloorTeamModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeFloorChanges, OfficeFloorTeamModel>() {
			@Override
			public String getInitialValue() {
				return OfficeFloorTeamEditPart.this.getCastedModel()
						.getOfficeFloorTeamName();
			}

			@Override
			public IFigure getLocationFigure() {
				return OfficeFloorTeamEditPart.this.getOfficeFloorFigure()
						.getOfficeFloorTeamNameFigure();
			}

			@Override
			public Change<OfficeFloorTeamModel> createChange(
					OfficeFloorChanges changes, OfficeFloorTeamModel target,
					String newValue) {
				return changes.renameOfficeFloorTeam(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<OfficeFloorTeamModel> policy) {
		policy.allowOpening(new OpenHandler<OfficeFloorTeamModel>() {
			@Override
			public void doOpen(OpenHandlerContext<OfficeFloorTeamModel> context) {

				// Obtain the team details
				OfficeFloorTeamModel team = context.getModel();
				String className = team.getTeamSourceClassName();
				PropertyList properties = context.createPropertyList();
				for (PropertyModel property : team.getProperties()) {
					properties.addProperty(property.getName()).setValue(
							property.getValue());
				}

				// Open the team
				ExtensionUtil.openTeamSource(className, properties, context
						.getEditPart().getEditor());
			}
		});
	}

	@Override
	protected Class<OfficeFloorTeamEvent> getPropertyChangeEventType() {
		return OfficeFloorTeamEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeFloorTeamEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_FLOOR_TEAM_NAME:
			this.getOfficeFloorFigure().setOfficeFloorTeamName(
					this.getCastedModel().getOfficeFloorTeamName());
			break;

		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
		case ADD_DEPLOYED_OFFICE_TEAM:
		case REMOVE_DEPLOYED_OFFICE_TEAM:
			this.refreshTargetConnections();
			break;

		case CHANGE_TEAM_SOURCE_CLASS_NAME:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
			// Non visual change
			break;
		}
	}

	/*
	 * ======================== TeamFigureContext ============================
	 */

	@Override
	public String getOfficeFloorTeamName() {
		return this.getCastedModel().getOfficeFloorTeamName();
	}

}