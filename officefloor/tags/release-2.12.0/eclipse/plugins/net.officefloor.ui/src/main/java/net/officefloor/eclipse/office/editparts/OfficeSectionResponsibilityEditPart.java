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
import net.officefloor.eclipse.skin.office.OfficeSectionResponsibilityFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionResponsibilityFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel.OfficeSectionResponsibilityEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeSectionResponsibilityModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSectionResponsibilityEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeSectionResponsibilityModel, OfficeSectionResponsibilityEvent, OfficeSectionResponsibilityFigure>
		implements OfficeSectionResponsibilityFigureContext {

	/*
	 * ================== AbstractOfficeFloorEditPart ======================
	 */

	@Override
	protected OfficeSectionResponsibilityFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeSectionResponsibilityFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getOfficeTeam());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<OfficeSectionResponsibilityModel> policy) {
		policy
				.allowDirectEdit(new DirectEditAdapter<OfficeChanges, OfficeSectionResponsibilityModel>() {
					@Override
					public String getInitialValue() {
						return OfficeSectionResponsibilityEditPart.this
								.getCastedModel()
								.getOfficeSectionResponsibilityName();
					}

					@Override
					public IFigure getLocationFigure() {
						return OfficeSectionResponsibilityEditPart.this
								.getOfficeFloorFigure()
								.getOfficeSectionResponsibilityNameFigure();
					}

					@Override
					public Change<OfficeSectionResponsibilityModel> createChange(
							OfficeChanges changes,
							OfficeSectionResponsibilityModel target,
							String newValue) {
						return changes.renameOfficeSectionResponsibility(
								target, newValue);
					}
				});
	}

	@Override
	protected Class<OfficeSectionResponsibilityEvent> getPropertyChangeEventType() {
		return OfficeSectionResponsibilityEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeSectionResponsibilityEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_SECTION_RESPONSIBILITY_NAME:
			this.getOfficeFloorFigure().setOfficeSectionResponsibilityName(
					this.getCastedModel().getOfficeSectionResponsibilityName());
			break;
			
		case CHANGE_OFFICE_TEAM:
			this.refreshSourceConnections();
			break;
		}
	}

	/*
	 * ==================== OfficeSectionResponsibilityFigureContext =========
	 */

	@Override
	public String getOfficeSectionResponsibilityName() {
		return this.getCastedModel().getOfficeSectionResponsibilityName();
	}

}