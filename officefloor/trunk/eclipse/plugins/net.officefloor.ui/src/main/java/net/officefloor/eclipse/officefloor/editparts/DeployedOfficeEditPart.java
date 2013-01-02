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
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.DeployedOfficeModel.DeployedOfficeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorOfficeModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeployedOfficeEditPart
		extends
		AbstractOfficeFloorEditPart<DeployedOfficeModel, DeployedOfficeEvent, DeployedOfficeFigure>
		implements DeployedOfficeFigureContext {

	@Override
	protected DeployedOfficeFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createDeployedOfficeFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getDeployedOfficeInputs());
		childModels.addAll(this.getCastedModel().getDeployedOfficeTeams());
		childModels.addAll(this.getCastedModel().getDeployedOfficeObjects());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSources());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<DeployedOfficeModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeFloorChanges, DeployedOfficeModel>() {
			@Override
			public String getInitialValue() {
				return DeployedOfficeEditPart.this.getCastedModel()
						.getDeployedOfficeName();
			}

			@Override
			public IFigure getLocationFigure() {
				return DeployedOfficeEditPart.this.getOfficeFloorFigure()
						.getDeployedOfficeNameFigure();
			}

			@Override
			public Change<DeployedOfficeModel> createChange(
					OfficeFloorChanges changes, DeployedOfficeModel target,
					String newValue) {
				return changes.renameDeployedOffice(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<DeployedOfficeModel> policy) {
		policy.allowOpening(new OpenHandler<DeployedOfficeModel>() {
			@Override
			public void doOpen(OpenHandlerContext<DeployedOfficeModel> context) {

				// Obtain the office details
				DeployedOfficeModel office = context.getModel();
				String className = office.getOfficeSourceClassName();
				String location = office.getOfficeLocation();
				PropertyList properties = context.createPropertyList();
				for (PropertyModel property : office.getProperties()) {
					properties.addProperty(property.getName()).setValue(
							property.getValue());
				}

				// Open the office
				ExtensionUtil.openOfficeSource(className, location, properties,
						context.getEditPart().getEditor());
			}
		});
	}

	@Override
	protected Class<DeployedOfficeEvent> getPropertyChangeEventType() {
		return DeployedOfficeEvent.class;
	}

	@Override
	protected void handlePropertyChange(DeployedOfficeEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_DEPLOYED_OFFICE_NAME:
			this.getOfficeFloorFigure().setDeployedOfficeName(
					this.getCastedModel().getDeployedOfficeName());
			break;

		case ADD_DEPLOYED_OFFICE_OBJECT:
		case REMOVE_DEPLOYED_OFFICE_OBJECT:
		case ADD_DEPLOYED_OFFICE_TEAM:
		case REMOVE_DEPLOYED_OFFICE_TEAM:
		case ADD_DEPLOYED_OFFICE_INPUT:
		case REMOVE_DEPLOYED_OFFICE_INPUT:
			this.refreshChildren();
			break;

		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE:
			this.refreshTargetConnections();
			break;

		case CHANGE_OFFICE_SOURCE_CLASS_NAME:
		case CHANGE_OFFICE_LOCATION:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
			// Non visual change
			break;
		}
	}

	/*
	 * ======================= OfficeFigureContext ======================
	 */

	@Override
	public String getDeployedOfficeName() {
		return this.getCastedModel().getDeployedOfficeName();
	}

}