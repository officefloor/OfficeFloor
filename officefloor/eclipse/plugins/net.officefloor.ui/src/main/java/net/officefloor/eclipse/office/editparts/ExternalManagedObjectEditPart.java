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
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.ExternalManagedObjectModel.ExternalManagedObjectEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link ExternalManagedObjectModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExternalManagedObjectEditPart
		extends
		AbstractOfficeFloorEditPart<ExternalManagedObjectModel, ExternalManagedObjectEvent, ExternalManagedObjectFigure>
		implements ExternalManagedObjectFigureContext {

	@Override
	protected ExternalManagedObjectFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createExternalManagedObjectFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		models.addAll(this.getCastedModel().getAdministrators());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOfficeSectionObjects());
		models.addAll(this.getCastedModel().getDependentOfficeManagedObjects());
		models.addAll(this.getCastedModel()
				.getDependentOfficeInputManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<ExternalManagedObjectModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeChanges, ExternalManagedObjectModel>() {
			@Override
			public String getInitialValue() {
				return ExternalManagedObjectEditPart.this.getCastedModel()
						.getExternalManagedObjectName();
			}

			@Override
			public IFigure getLocationFigure() {
				return ExternalManagedObjectEditPart.this
						.getOfficeFloorFigure()
						.getExternalManagedObjectNameFigure();
			}

			@Override
			public Change<ExternalManagedObjectModel> createChange(
					OfficeChanges changes, ExternalManagedObjectModel target,
					String newValue) {
				return changes.renameExternalManagedObject(target, newValue);
			}
		});
	}

	@Override
	protected Class<ExternalManagedObjectEvent> getPropertyChangeEventType() {
		return ExternalManagedObjectEvent.class;
	}

	@Override
	protected void handlePropertyChange(ExternalManagedObjectEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case ADD_OFFICE_GOVERNANCE:
		case REMOVE_OFFICE_GOVERNANCE:
			// TODO provide governance configuration
			break;

		case CHANGE_EXTERNAL_MANAGED_OBJECT_NAME:
			this.getOfficeFloorFigure().setExternalManagedObjectName(
					this.getCastedModel().getExternalManagedObjectName());
			break;

		case ADD_ADMINISTRATOR:
		case REMOVE_ADMINISTRATOR:
			this.refreshSourceConnections();
			break;

		case ADD_OFFICE_SECTION_OBJECT:
		case REMOVE_OFFICE_SECTION_OBJECT:
		case ADD_DEPENDENT_OFFICE_MANAGED_OBJECT:
		case REMOVE_DEPENDENT_OFFICE_MANAGED_OBJECT:
		case ADD_DEPENDENT_OFFICE_INPUT_MANAGED_OBJECT:
		case REMOVE_DEPENDENT_OFFICE_INPUT_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;

		case CHANGE_OBJECT_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ==================== ExternalManagedObjectFigureContext ================
	 */

	@Override
	public String getExternalManagedObjectName() {
		return this.getCastedModel().getExternalManagedObjectName();
	}

}