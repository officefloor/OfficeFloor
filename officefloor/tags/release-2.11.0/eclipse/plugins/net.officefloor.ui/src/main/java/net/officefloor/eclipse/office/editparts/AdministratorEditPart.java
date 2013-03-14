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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.skin.office.AdministratorFigure;
import net.officefloor.eclipse.skin.office.AdministratorFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.AdministratorModel;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.PropertyModel;
import net.officefloor.model.office.AdministratorModel.AdministratorEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link AdministratorModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorEditPart
		extends
		AbstractOfficeFloorEditPart<AdministratorModel, AdministratorEvent, AdministratorFigure>
		implements AdministratorFigureContext {

	@Override
	protected AdministratorFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createAdministratorFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getDuties());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getOfficeTeam());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getExternalManagedObjects());
		models.addAll(this.getCastedModel().getOfficeManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<AdministratorModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeChanges, AdministratorModel>() {
			@Override
			public String getInitialValue() {
				return AdministratorEditPart.this.getCastedModel()
						.getAdministratorName();
			}

			@Override
			public IFigure getLocationFigure() {
				return AdministratorEditPart.this.getOfficeFloorFigure()
						.getAdministratorNameFigure();
			}

			@Override
			public Change<AdministratorModel> createChange(
					OfficeChanges changes, AdministratorModel target,
					String newValue) {
				return changes.renameAdministrator(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<AdministratorModel> policy) {
		policy.allowOpening(new OpenHandler<AdministratorModel>() {
			@Override
			public void doOpen(OpenHandlerContext<AdministratorModel> context) {

				// Obtain the administrator details
				AdministratorModel admin = context.getModel();
				String className = admin.getAdministratorSourceClassName();
				PropertyList properties = context.createPropertyList();
				for (PropertyModel property : admin.getProperties()) {
					properties.addProperty(property.getName()).setValue(
							property.getValue());
				}

				// Open the administrator source
				ExtensionUtil.openAdministratorSource(className, properties,
						context.getEditPart().getEditor());
			}
		});
	}

	@Override
	protected Class<AdministratorEvent> getPropertyChangeEventType() {
		return AdministratorEvent.class;
	}

	@Override
	protected void handlePropertyChange(AdministratorEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_ADMINISTRATOR_NAME:
			this.getOfficeFloorFigure().setAdministratorName(
					this.getCastedModel().getAdministratorName());
			break;

		case ADD_DUTY:
		case REMOVE_DUTY:
			this.refreshChildren();
			break;

		case CHANGE_OFFICE_TEAM:
			this.refreshSourceConnections();
			break;

		case ADD_EXTERNAL_MANAGED_OBJECT:
		case REMOVE_EXTERNAL_MANAGED_OBJECT:
		case ADD_OFFICE_MANAGED_OBJECT:
		case REMOVE_OFFICE_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;

		case CHANGE_ADMINISTRATOR_SOURCE_CLASS_NAME:
		case CHANGE_ADMINISTRATOR_SCOPE:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
			// Non visual change
			break;
		}
	}

	/*
	 * ==================== AdministratorFigureContext ======================
	 */

	@Override
	public String getAdministratorName() {
		return this.getCastedModel().getAdministratorName();
	}

}