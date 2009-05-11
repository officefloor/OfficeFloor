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
 * @author Daniel
 */
public class OfficeTeamEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeTeamModel, OfficeTeamEvent, OfficeTeamFigure>
		implements OfficeTeamFigureContext {

	@Override
	protected OfficeTeamFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeTeamFigure(this);
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOfficeSectionResponsibilities());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<OfficeTeamModel> policy) {
		policy
				.allowDirectEdit(new DirectEditAdapter<OfficeChanges, OfficeTeamModel>() {
					@Override
					public String getInitialValue() {
						return OfficeTeamEditPart.this.getCastedModel()
								.getOfficeTeamName();
					}

					@Override
					public IFigure getLocationFigure() {
						return OfficeTeamEditPart.this.getOfficeFloorFigure()
								.getOfficeTeamNameFigure();
					}

					@Override
					public Change<OfficeTeamModel> createChange(
							OfficeChanges changes, OfficeTeamModel target,
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
	protected void handlePropertyChange(OfficeTeamEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_TEAM_NAME:
			this.getOfficeFloorFigure().setOfficeTeamName(
					this.getCastedModel().getOfficeTeamName());
			break;
		case ADD_OFFICE_SECTION_RESPONSIBILITY:
		case REMOVE_OFFICE_SECTION_RESPONSIBILITY:
			this.refreshTargetConnections();
			break;
		// case ADD_ADMINISTRATOR:
		// case REMOVE_ADMINISTRATOR:
		// ExternalTeamEditPart.this.refreshTargetConnections();
		// break;
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