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
package net.officefloor.eclipse.section.operations;

import org.eclipse.gef.EditPart;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.eclipse.section.editparts.ManagedFunctionEditPart;
import net.officefloor.eclipse.util.ModelUtil;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.SectionChanges;

/**
 * Creates a {@link FunctionModel} from a {@link ManagedFunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class CreateFunctionFromManagedFunctionOperation
		extends AbstractSectionChangeOperation<ManagedFunctionEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public CreateFunctionFromManagedFunctionOperation(SectionChanges sectionChanges) {
		super("Add as function", ManagedFunctionEditPart.class, sectionChanges);
	}

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the managed function to create function
		ManagedFunctionEditPart editPart = context.getEditPart();
		ManagedFunctionModel managedFunction = editPart.getCastedModel();

		// Obtain the namespace
		EditPart namespaceEditPart = editPart.getParent();
		FunctionNamespaceModel namespace = (FunctionNamespaceModel) namespaceEditPart.getModel();

		// Obtain the namespace type
		FunctionNamespaceType namespaceType = ModelUtil.getFunctionNamespaceType(namespace, editPart.getEditor());
		if (namespaceType == null) {
			return null; // must have work type
		}

		// Obtain the function type
		ManagedFunctionType<?, ?> functionType = null;
		String functionName = managedFunction.getManagedFunctionName();
		for (ManagedFunctionType<?, ?> function : namespaceType.getManagedFunctionTypes()) {
			if (functionName.equals(function.getFunctionName())) {
				functionType = function;
			}
		}
		if (functionType == null) {
			editPart.messageError("Function " + functionName + " is not on namespace.\n\nPlease conform namespace.");
			return null; // must have function type
		}

		// Create the change to add the function
		Change<FunctionModel> change = changes.addFunction(managedFunction.getManagedFunctionName(), managedFunction,
				functionType);

		// Position the function
		FunctionModel function = change.getTarget();
		context.positionModel(function);
		function.setX(function.getX() + 100); // position function to right

		// Return the change
		return change;
	}

}