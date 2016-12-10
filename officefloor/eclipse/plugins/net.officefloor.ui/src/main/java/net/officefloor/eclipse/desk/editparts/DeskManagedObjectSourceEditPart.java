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
package net.officefloor.eclipse.desk.editparts;

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
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFigure;
import net.officefloor.eclipse.skin.desk.DeskManagedObjectSourceFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskChanges;
import net.officefloor.model.desk.DeskManagedObjectSourceModel;
import net.officefloor.model.desk.PropertyModel;
import net.officefloor.model.desk.DeskManagedObjectSourceModel.DeskManagedObjectSourceEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link DeskManagedObjectSourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeskManagedObjectSourceEditPart
		extends
		AbstractOfficeFloorEditPart<DeskManagedObjectSourceModel, DeskManagedObjectSourceEvent, DeskManagedObjectSourceFigure>
		implements DeskManagedObjectSourceFigureContext {

	/**
	 * Opens the underlying {@link DeskManagedObjectSourceModel}.
	 * 
	 * @param managedObjectSource
	 *            {@link DeskManagedObjectSourceModel} to open.
	 * @param context
	 *            {@link OpenHandlerContext}.
	 */
	public static void openManagedObjectSource(
			DeskManagedObjectSourceModel managedObjectSource,
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
	protected DeskManagedObjectSourceFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getDeskFigureFactory()
				.createDeskManagedObjectSourceFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel()
				.getDeskManagedObjectSourceFlows());
	}

	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getDeskManagedObjects());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(
			OfficeFloorDirectEditPolicy<DeskManagedObjectSourceModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<DeskChanges, DeskManagedObjectSourceModel>() {
			@Override
			public String getInitialValue() {
				return DeskManagedObjectSourceEditPart.this.getCastedModel()
						.getDeskManagedObjectSourceName();
			}

			@Override
			public IFigure getLocationFigure() {
				return DeskManagedObjectSourceEditPart.this
						.getOfficeFloorFigure()
						.getDeskManagedObjectSourceNameFigure();
			}

			@Override
			public Change<DeskManagedObjectSourceModel> createChange(
					DeskChanges changes, DeskManagedObjectSourceModel target,
					String newValue) {
				return changes.renameDeskManagedObjectSource(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(
			OfficeFloorOpenEditPolicy<DeskManagedObjectSourceModel> policy) {
		policy.allowOpening(new OpenHandler<DeskManagedObjectSourceModel>() {
			@Override
			public void doOpen(
					OpenHandlerContext<DeskManagedObjectSourceModel> context) {
				openManagedObjectSource(context.getModel(), context);
			}
		});
	}

	@Override
	protected Class<DeskManagedObjectSourceEvent> getPropertyChangeEventType() {
		return DeskManagedObjectSourceEvent.class;
	}

	@Override
	protected void handlePropertyChange(DeskManagedObjectSourceEvent property,
			PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_DESK_MANAGED_OBJECT_SOURCE_NAME:
			this.getOfficeFloorFigure().setDeskManagedObjectName(
					this.getCastedModel().getDeskManagedObjectSourceName());
			break;

		case ADD_DESK_MANAGED_OBJECT:
		case REMOVE_DESK_MANAGED_OBJECT:
			this.refreshTargetConnections();
			break;

		case ADD_DESK_MANAGED_OBJECT_SOURCE_FLOW:
		case REMOVE_DESK_MANAGED_OBJECT_SOURCE_FLOW:
			this.refreshChildren();
			break;

		case CHANGE_MANAGED_OBJECT_SOURCE_CLASS_NAME:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
		case CHANGE_TIMEOUT:
		case CHANGE_OBJECT_TYPE:
			// Non visual change
			break;
		}
	}

	/*
	 * ================== ManagedObjectSourceFigureContext =====================
	 */

	@Override
	public String getDeskManagedObjectSourceName() {
		return this.getCastedModel().getDeskManagedObjectSourceName();
	}

}