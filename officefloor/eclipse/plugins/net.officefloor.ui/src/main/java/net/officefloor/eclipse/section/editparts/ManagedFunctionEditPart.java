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

import org.eclipse.gef.EditPart;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.action.OperationUtil;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart;
import net.officefloor.eclipse.section.operations.CreateFunctionFromManagedFunctionOperation;
import net.officefloor.eclipse.skin.section.ManagedFunctionFigure;
import net.officefloor.eclipse.skin.section.ManagedFunctionFigureContext;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionModel.ManagedFunctionEvent;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;

/**
 * {@link EditPart} for the {@link ManagedFunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionEditPart
		extends AbstractOfficeFloorEditPart<ManagedFunctionModel, ManagedFunctionEvent, ManagedFunctionFigure>
		implements ManagedFunctionFigureContext {

	/**
	 * Obtains the {@link FunctionNamespaceModel} that contains the
	 * {@link ManagedFunctionModel}.
	 * 
	 * @param managedFunction
	 *            {@link ManagedFunctionModel}.
	 * @param section
	 *            {@link SectionModel} containing the
	 *            {@link ManagedFunctionModel}.
	 * @return {@link FunctionNamespaceModel} or <code>null</code> if not
	 *         contained by a {@link FunctionNamespaceModel}.
	 */
	public static FunctionNamespaceModel getFunctionNamespace(ManagedFunctionModel managedFunction,
			SectionModel section) {

		// Ensure have managed function
		if (managedFunction == null) {
			return null;
		}

		// Obtain the containing function namespace
		for (FunctionNamespaceModel namespace : section.getFunctionNamespaces()) {
			for (ManagedFunctionModel check : namespace.getManagedFunctions()) {
				if (managedFunction == check) {
					// Found managed function, so use the containing namespace
					return namespace;
				}
			}
		}

		// As here no containing namespace
		return null;
	}

	@Override
	protected ManagedFunctionFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getSectionFigureFactory().createManagedFunctionFigure(this);
	}

	@Override
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getManagedFunctionObjects());
	}

	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		models.addAll(this.getCastedModel().getFunctions());
	}

	@Override
	protected Class<ManagedFunctionEvent> getPropertyChangeEventType() {
		return ManagedFunctionEvent.class;
	}

	@Override
	protected void handlePropertyChange(ManagedFunctionEvent property, PropertyChangeEvent evt) {
		switch (property) {
		case ADD_FUNCTION:
		case REMOVE_FUNCTION:
			ManagedFunctionEditPart.this.refreshSourceConnections();
			break;

		case ADD_MANAGED_FUNCTION_OBJECT:
		case REMOVE_MANAGED_FUNCTION_OBJECT:
			ManagedFunctionEditPart.this.refreshChildren();
			break;

		case CHANGE_MANAGED_FUNCTION_NAME:
			this.getOfficeFloorFigure().setManagedFunctionName(this.getManagedFunctionName());
			break;
		}
	}

	/*
	 * ============ DeskTaskFigureContext ============
	 */

	@Override
	public String getManagedFunctionName() {
		return this.getCastedModel().getManagedFunctionName();
	}

	@Override
	public void createAsNewFunction() {

		// Obtain the section changes
		SectionChanges sectionChanges = (SectionChanges) this.getEditor().getModelChanges();

		// Execute operation to add function (right of this managed function)
		OperationUtil.execute(new CreateFunctionFromManagedFunctionOperation(sectionChanges),
				this.getCastedModel().getX() + 100, this.getCastedModel().getY(), this);
	}

}