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
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.skin.office.OfficeTeamFigure;
import net.officefloor.eclipse.skin.office.OfficeTeamFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.office.OfficeTeamModel.OfficeTeamEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeTeamModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeTeamEditPart extends AbstractOfficeFloorEditPart<OfficeTeamModel, OfficeTeamEvent, OfficeTeamFigure>
		implements OfficeTeamFigureContext {

	@Override
	protected OfficeTeamFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory().createOfficeTeamFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOfficeFunctions());
		models.addAll(this.getCastedModel().getOfficeManagedObjectSourceTeams());
		models.addAll(this.getCastedModel().getAdministrations());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(OfficeFloorDirectEditPolicy<OfficeTeamModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeChanges, OfficeTeamModel>() {
			@Override
			public String getInitialValue() {
				return OfficeTeamEditPart.this.getCastedModel().getOfficeTeamName();
			}

			@Override
			public IFigure getLocationFigure() {
				return OfficeTeamEditPart.this.getOfficeFloorFigure().getOfficeTeamNameFigure();
			}

			@Override
			public Change<OfficeTeamModel> createChange(OfficeChanges changes, OfficeTeamModel target,
					String newValue) {
				return changes.renameOfficeTeam(target, newValue);
			}
		});
	}

	@Override
	protected Class<OfficeTeamEvent> getPropertyChangeEventType() {
		return OfficeTeamEvent.class;
	}

	@Override
	protected void handlePropertyChange(OfficeTeamEvent property, PropertyChangeEvent evt) {
		switch (property) {

		case CHANGE_OFFICE_TEAM_NAME:
			this.getOfficeFloorFigure().setOfficeTeamName(this.getCastedModel().getOfficeTeamName());
			break;

		case ADD_OFFICE_FUNCTION:
		case REMOVE_OFFICE_FUNCTION:
		case ADD_OFFICE_MANAGED_OBJECT_SOURCE_TEAM:
		case REMOVE_OFFICE_MANAGED_OBJECT_SOURCE_TEAM:
		case ADD_ADMINISTRATION:
		case REMOVE_ADMINISTRATION:
			this.refreshTargetConnections();
			break;
		}
	}

	/*
	 * =================== ExternalTeamFigureContext ========================
	 */

	@Override
	public String getOfficeTeamName() {
		return this.getCastedModel().getOfficeTeamName();
	}

}