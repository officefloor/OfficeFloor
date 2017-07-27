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

import net.officefloor.eclipse.section.editparts.SectionEditPart;
import net.officefloor.eclipse.wizard.managedfunctionsource.ManagedFunctionSourceWizard;
import net.officefloor.eclipse.wizard.managedfunctionsource.FunctionNamespaceInstance;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;

/**
 * Adds a {@link FunctionNamespaceModel} to the {@link SectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddFunctionNamespaceOperation extends AbstractSectionChangeOperation<SectionEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public AddFunctionNamespaceOperation(SectionChanges sectionChanges) {
		super("Add function namespace", SectionEditPart.class, sectionChanges);
	}

	/*
	 * ================ AbstractSectionChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the namespace instance
		FunctionNamespaceInstance namespace = ManagedFunctionSourceWizard.getFunctionNamespaceInstance(context.getEditPart(), null);
		if (namespace == null) {
			return null; // must have namespace to add
		}

		// Obtain the add namespace change
		Change<FunctionNamespaceModel> change = changes.addFunctionNamespace(namespace.getFunctionNamespaceName(),
				namespace.getManagedFunctionSourceClassName(), namespace.getPropertyList(), namespace.getFunctionNamespaceType(),
				namespace.getManagedFunctionTypeNames());

		// Position the namespace
		context.positionModel(change.getTarget());

		// Return the change
		return change;
	}

}