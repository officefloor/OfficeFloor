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

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.skin.office.AdministrationFigure;
import net.officefloor.eclipse.skin.office.AdministrationFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.AdministrationModel.AdministrationEvent;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.PropertyModel;

/**
 * {@link EditPart} for the {@link AdministrationModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationEditPart
		extends AbstractOfficeFloorEditPart<AdministrationModel, AdministrationEvent, AdministrationFigure>
		implements AdministrationFigureContext {

	@Override
	protected AdministrationFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory().createAdministrationFigure(this);
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil.addToList(models, this.getCastedModel().getOfficeTeam());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getExternalManagedObjects());
		models.addAll(this.getCastedModel().getOfficeManagedObjects());
		models.addAll(this.getCastedModel().getPreOfficeFunctions());
		models.addAll(this.getCastedModel().getPostOfficeFunctions());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(OfficeFloorDirectEditPolicy<AdministrationModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeChanges, AdministrationModel>() {
			@Override
			public String getInitialValue() {
				return AdministrationEditPart.this.getCastedModel().getAdministrationName();
			}

			@Override
			public IFigure getLocationFigure() {
				return AdministrationEditPart.this.getOfficeFloorFigure().getAdministrationNameFigure();
			}

			@Override
			public Change<AdministrationModel> createChange(OfficeChanges changes, AdministrationModel target,
					String newValue) {
				return changes.renameAdministration(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(OfficeFloorOpenEditPolicy<AdministrationModel> policy) {
		policy.allowOpening(new OpenHandler<AdministrationModel>() {
			@Override
			public void doOpen(OpenHandlerContext<AdministrationModel> context) {

				// Obtain the administration details
				AdministrationModel admin = context.getModel();
				String className = admin.getAdministrationSourceClassName();
				PropertyList properties = context.createPropertyList();
				for (PropertyModel property : admin.getProperties()) {
					properties.addProperty(property.getName()).setValue(property.getValue());
				}

				// Open the administration source
				ExtensionUtil.openAdministrationSource(className, properties, context.getEditPart().getEditor());
			}
		});
	}

	@Override
	protected Class<AdministrationEvent> getPropertyChangeEventType() {
		return AdministrationEvent.class;
	}

	@Override
	protected void handlePropertyChange(AdministrationEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_ADMINISTRATION_NAME:
			this.getOfficeFloorFigure().setAdministrationName(this.getCastedModel().getAdministrationName());
			break;

		case CHANGE_OFFICE_TEAM:
			this.refreshSourceConnections();
			break;

		case ADD_EXTERNAL_MANAGED_OBJECT:
		case REMOVE_EXTERNAL_MANAGED_OBJECT:
		case ADD_OFFICE_MANAGED_OBJECT:
		case REMOVE_OFFICE_MANAGED_OBJECT:
		case ADD_PRE_OFFICE_FUNCTION:
		case REMOVE_PRE_OFFICE_FUNCTION:
		case ADD_POST_OFFICE_FUNCTION:
		case REMOVE_POST_OFFICE_FUNCTION:
			this.refreshTargetConnections();
			break;

		case CHANGE_ADMINISTRATION_SOURCE_CLASS_NAME:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
			// Non visual change
			break;
		}
	}

	/*
	 * ==================== AdministrationFigureContext ======================
	 */

	@Override
	public String getAdministrationName() {
		return this.getCastedModel().getAdministrationName();
	}

}