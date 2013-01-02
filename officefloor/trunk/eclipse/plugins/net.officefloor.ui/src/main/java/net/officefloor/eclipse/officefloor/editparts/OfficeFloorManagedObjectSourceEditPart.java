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
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectSourceFigureContext;
import net.officefloor.eclipse.util.EclipseUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.officefloor.OfficeFloorChanges;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel;
import net.officefloor.model.officefloor.PropertyModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceModel.OfficeFloorManagedObjectSourceEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link OfficeFloorManagedObjectSourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorManagedObjectSourceEditPart
		extends
		AbstractOfficeFloorEditPart<OfficeFloorManagedObjectSourceModel, OfficeFloorManagedObjectSourceEvent, OfficeFloorManagedObjectSourceFigure>
		implements OfficeFloorManagedObjectSourceFigureContext {

	/**
	 * Opens the underlying {@link OfficeFloorManagedObjectSourceModel}.
	 * 
	 * @param managedObjectSource
	 *            {@link OfficeFloorManagedObjectSourceModel}.
	 * @param context
	 *            {@link OpenHandlerContext}.
	 */
	public static void openManagedObjectSource(
			OfficeFloorManagedObjectSourceModel managedObjectSource,
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
	protected OfficeFloorManagedObjectSourceFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getOfficeFloorFigureFactory()
				.createOfficeFloorManagedObjectSourceFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceFlows());
		childModels.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceTeams());
		childModels.addAll(this.getCastedModel()
				.getOfficeFloorManagedObjectSourceInputDependencies());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		EclipseUtil
				.addToList(models, this.getCastedModel().getManagingOffice());
		EclipseUtil.addToList(models, this.getCastedModel()
				.getOfficeFloorInputManagedObject());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOfficeFloorManagedObjects());
		models.addAll(this.getCastedModel()
				.getBoundOfficeFloorInputManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<OfficeFloorManagedObjectSourceModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<OfficeFloorChanges, OfficeFloorManagedObjectSourceModel>() {
			@Override
			public String getInitialValue() {
				return OfficeFloorManagedObjectSourceEditPart.this
						.getCastedModel()
						.getOfficeFloorManagedObjectSourceName();
			}

			@Override
			public IFigure getLocationFigure() {
				return OfficeFloorManagedObjectSourceEditPart.this
						.getOfficeFloorFigure()
						.getOfficeFloorManagedObjectSourceNameFigure();
			}

			@Override
			public Change<OfficeFloorManagedObjectSourceModel> createChange(
					OfficeFloorChanges changes,
					OfficeFloorManagedObjectSourceModel target, String newValue) {
				return changes.renameOfficeFloorManagedObjectSource(target,
						newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<OfficeFloorManagedObjectSourceModel> policy) {
		policy.allowOpening(new OpenHandler<OfficeFloorManagedObjectSourceModel>() {
			@Override
			public void doOpen(
					OpenHandlerContext<OfficeFloorManagedObjectSourceModel> context) {
				OfficeFloorManagedObjectSourceEditPart.openManagedObjectSource(
						context.getModel(), context);
			}
		});
	}

	@Override
	protected Class<OfficeFloorManagedObjectSourceEvent> getPropertyChangeEventType() {
		return OfficeFloorManagedObjectSourceEvent.class;
	}

	@Override
	protected void handlePropertyChange(
			OfficeFloorManagedObjectSourceEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_OFFICE_FLOOR_SUPPLIER:
			// TODO add supplier configuration
			break;

		case CHANGE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_NAME:
			this.getOfficeFloorFigure().setOfficeFloorManagedObjectName(
					this.getCastedModel()
							.getOfficeFloorManagedObjectSourceName());
			break;

		case CHANGE_MANAGING_OFFICE:
		case CHANGE_OFFICE_FLOOR_INPUT_MANAGED_OBJECT:
			this.refreshSourceConnections();
			break;

		case ADD_OFFICE_FLOOR_MANAGED_OBJECT:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT:
		case ADD_BOUND_OFFICE_FLOOR_INPUT_MANAGED_OBJECT:
		case REMOVE_BOUND_OFFICE_FLOOR_INPUT_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;

		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_FLOW:
		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_TEAM:
		case ADD_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT_DEPENDENCY:
		case REMOVE_OFFICE_FLOOR_MANAGED_OBJECT_SOURCE_INPUT_DEPENDENCY:
			this.refreshChildren();
			break;

		case CHANGE_MANAGED_OBJECT_SOURCE_CLASS_NAME:
		case CHANGE_OBJECT_TYPE:
		case CHANGE_TIMEOUT:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
			// Non visual change
			break;
		}
	}

	/*
	 * ================== ManagedObjectSourceFigureContext =====================
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.getCastedModel().getOfficeFloorManagedObjectSourceName();
	}

}