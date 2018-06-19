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
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.office.OfficeManagedObjectSourceFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.PropertyModel;
import net.officefloor.model.office.OfficeManagedObjectSourceModel.OfficeManagedObjectSourceEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeManagedObjectSourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagedObjectSourceEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeManagedObjectSourceModel, OfficeManagedObjectSourceEvent, OfficeManagedObjectSourceFigure>
		implements OfficeManagedObjectSourceFigureContext {

	/**
	 * Opens the underlying {@link OfficeManagedObjectSourceModel}.
	 * 
	 * @param managedObjectSource
	 *            {@link OfficeManagedObjectSourceModel}.
	 * @param context
	 *            {@link OpenHandlerContext}.
	 */
	public static void openManagedObjectSource(
			OfficeManagedObjectSourceModel managedObjectSource,
			OpenHandlerContext<?> context) {

		// Obtain the managed object source details
		String className = managedObjectSource
				.getManagedObjectSourceClassName();
		PropertyList properties = context.createPropertyList();
		for (PropertyModel property : managedObjectSource.getProperties()) {
			properties.addProperty(property.getName()).setValue(
					property.getValue());
		}

		// Open the managed object source
		ExtensionUtil.openManagedObjectSource(className, properties, context
				.getEditPart().getEditor());
	}

	@Override
	protected OfficeManagedObjectSourceFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFigureFactory()
				.createOfficeManagedObjectSourceFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel()
				.getOfficeManagedObjectSourceFlows());
		childModels.addAll(this.getCastedModel()
				.getOfficeManagedObjectSourceTeams());
		childModels.addAll(this.getCastedModel()
				.getOfficeInputManagedObjectDependencies());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOfficeManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<OfficeManagedObjectSourceModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeChanges, OfficeManagedObjectSourceModel>() {
			@Override
			public String getInitialValue() {
				return OfficeManagedObjectSourceEditPart.this.getCastedModel()
						.getOfficeManagedObjectSourceName();
			}

			@Override
			public IFigure getLocationFigure() {
				return OfficeManagedObjectSourceEditPart.this
						.getOfficeFloorFigure()
						.getOfficeManagedObjectSourceNameFigure();
			}

			@Override
			public Change<OfficeManagedObjectSourceModel> createChange(
					OfficeChanges changes,
					OfficeManagedObjectSourceModel target, String newValue) {
				return changes
						.renameOfficeManagedObjectSource(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<OfficeManagedObjectSourceModel> policy) {
		policy.allowOpening(new OpenHandler<OfficeManagedObjectSourceModel>() {
			@Override
			public void doOpen(
					OpenHandlerContext<OfficeManagedObjectSourceModel> context) {
				OfficeManagedObjectSourceEditPart.openManagedObjectSource(
						context.getModel(), context);
			}
		});
	}

	@Override
	protected Class<OfficeManagedObjectSourceEvent> getPropertyChangeEventType() {
		return OfficeManagedObjectSourceEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeManagedObjectSourceEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_MANAGED_OBJECT_SOURCE_NAME:
			this.getOfficeFloorFigure().setOfficeManagedObjectName(
					this.getCastedModel().getOfficeManagedObjectSourceName());
			break;

		case ADD_OFFICE_MANAGED_OBJECT:
		case REMOVE_OFFICE_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;

		case ADD_OFFICE_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_OFFICE_MANAGED_OBJECT_SOURCE_FLOW:
		case ADD_OFFICE_MANAGED_OBJECT_SOURCE_TEAM:
		case REMOVE_OFFICE_MANAGED_OBJECT_SOURCE_TEAM:
		case ADD_OFFICE_INPUT_MANAGED_OBJECT_DEPENDENCY:
		case REMOVE_OFFICE_INPUT_MANAGED_OBJECT_DEPENDENCY:
			this.refreshChildren();
			break;

		case CHANGE_MANAGED_OBJECT_SOURCE_CLASS_NAME:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
		case CHANGE_OBJECT_TYPE:
		case CHANGE_TIMEOUT:
			// Non visual change
			break;
		}
	}

	/*
	 * ================== ManagedObjectSourceFigureContext =====================
	 */

	@Override
	public String getOfficeManagedObjectSourceName() {
		return this.getCastedModel().getOfficeManagedObjectSourceName();
	}

}