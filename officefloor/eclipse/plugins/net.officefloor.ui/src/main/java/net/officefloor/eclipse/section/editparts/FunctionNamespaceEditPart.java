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
package net.officefloor.eclipse.section.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.common.editpolicies.directedit.DirectEditAdapter;
import net.officefloor.eclipse.common.editpolicies.directedit.OfficeFloorDirectEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OfficeFloorOpenEditPolicy;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandler;
import net.officefloor.eclipse.common.editpolicies.open.OpenHandlerContext;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.skin.section.FunctionNamespaceFigure;
import net.officefloor.eclipse.skin.section.FunctionNamespaceFigureContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.FunctionNamespaceModel.FunctionNamespaceEvent;
import net.officefloor.model.section.PropertyModel;
import net.officefloor.model.section.SectionChanges;

/**
 * {@link EditPart} for the {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionNamespaceEditPart
		extends AbstractOfficeFloorEditPart<FunctionNamespaceModel, FunctionNamespaceEvent, FunctionNamespaceFigure>
		implements FunctionNamespaceFigureContext {

	/**
	 * Opens the {@link ManagedFunctionSource} for the
	 * {@link FunctionNamespaceModel}.
	 * 
	 * @param namespace
	 *            {@link FunctionNamespaceModel}.
	 * @param context
	 *            {@link OpenHandlerContext}.
	 */
	public static void openManagedFunctionSource(FunctionNamespaceModel namespace, OpenHandlerContext<?> context) {

		// Obtain the details about the namespace
		String managedFunctionSourceClassName = namespace.getManagedFunctionSourceClassName();
		PropertyList properties = context.createPropertyList();
		for (PropertyModel property : namespace.getProperties()) {
			properties.addProperty(property.getName()).setValue(property.getValue());
		}

		// Open the managed function source
		ExtensionUtil.openManagedFunctionSource(managedFunctionSourceClassName, properties,
				context.getEditPart().getEditor());
	}

	@Override
	protected FunctionNamespaceFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory().createFunctionNamespaceFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getManagedFunctions());
	}

	@Override
	protected void populateOfficeFloorDirectEditPolicy(OfficeFloorDirectEditPolicy<FunctionNamespaceModel> policy) {
		policy.allowDirectEdit(new DirectEditAdapter<SectionChanges, FunctionNamespaceModel>() {
			@Override
			public String getInitialValue() {
				return FunctionNamespaceEditPart.this.getCastedModel().getFunctionNamespaceName();
			}

			@Override
			public IFigure getLocationFigure() {
				return FunctionNamespaceEditPart.this.getOfficeFloorFigure().getFunctionNamespaceNameFigure();
			}

			@Override
			public Change<FunctionNamespaceModel> createChange(SectionChanges changes, FunctionNamespaceModel target,
					String newValue) {
				return changes.renameFunctionNamespace(target, newValue);
			}
		});
	}

	@Override
	protected void populateOfficeFloorOpenEditPolicy(OfficeFloorOpenEditPolicy<FunctionNamespaceModel> policy) {
		policy.allowOpening(new OpenHandler<FunctionNamespaceModel>() {
			@Override
			public void doOpen(OpenHandlerContext<FunctionNamespaceModel> context) {
				FunctionNamespaceEditPart.openManagedFunctionSource(context.getModel(), context);
			}
		});
	}

	@Override
	protected Class<FunctionNamespaceEvent> getPropertyChangeEventType() {
		return FunctionNamespaceEvent.class;
	}

	@Override
	protected void handlePropertyChange(FunctionNamespaceEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case CHANGE_FUNCTION_NAMESPACE_NAME:
			this.getOfficeFloorFigure().setFunctionNamespaceName(this.getCastedModel().getFunctionNamespaceName());
			break;

		case ADD_MANAGED_FUNCTION:
		case REMOVE_MANAGED_FUNCTION:
			FunctionNamespaceEditPart.this.refreshChildren();
			break;

		case CHANGE_MANAGED_FUNCTION_SOURCE_CLASS_NAME:
		case ADD_PROPERTY:
		case REMOVE_PROPERTY:
			// Non visual change
			break;
		}
	}

	/*
	 * =============== DeskWorkFigureContext =======================
	 */

	@Override
	public String getFunctionNamespaceName() {
		return this.getCastedModel().getFunctionNamespaceName();
	}

}