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

import java.util.Map;

import net.officefloor.eclipse.common.action.Operation;
import net.officefloor.eclipse.section.editparts.FunctionNamespaceEditPart;
import net.officefloor.eclipse.wizard.managedfunctionsource.FunctionNamespaceInstance;
import net.officefloor.eclipse.wizard.managedfunctionsource.ManagedFunctionSourceWizard;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.SectionChanges;

/**
 * {@link Operation} to refactor the {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorFunctionNamespaceOperation extends AbstractSectionChangeOperation<FunctionNamespaceEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param sectionChanges
	 *            {@link SectionChanges}.
	 */
	public RefactorFunctionNamespaceOperation(SectionChanges sectionChanges) {
		super("Refactor function namespace", FunctionNamespaceEditPart.class, sectionChanges);
	}

	/*
	 * ================= AbstractDeskChangeOperation ================
	 */

	@Override
	protected Change<?> getChange(SectionChanges changes, Context context) {

		// Obtain the function namespace model
		FunctionNamespaceEditPart editPart = context.getEditPart();
		FunctionNamespaceModel namespace = editPart.getCastedModel();

		// Obtain the refactored namespace instance
		FunctionNamespaceInstance namespaceInstance = ManagedFunctionSourceWizard.getFunctionNamespaceInstance(editPart,
				new FunctionNamespaceInstance(namespace));
		if (namespaceInstance == null) {
			return null; // namespace not being refactored
		}

		// Obtain the align details
		Map<String, String> managedFunctionNameMapping = namespaceInstance.getManagedFunctionNameMapping();
		Map<String, Map<String, String>> managedFunctionToObjectNameMapping = namespaceInstance
				.getFunctionObjectNameMappingForManagedFunction();
		Map<String, Map<String, String>> functionToFlowNameMapping = namespaceInstance
				.getFunctionFlowNameMappingForFunction();
		Map<String, Map<String, String>> functionToEscalationTypeMapping = namespaceInstance
				.getFunctionEscalationTypeMappingForFunction();

		// Return change for refactoring the namespace
		return changes.refactorFunctionNamespace(namespace, namespaceInstance.getFunctionNamespaceName(),
				namespaceInstance.getManagedFunctionSourceClassName(), namespaceInstance.getPropertyList(),
				namespaceInstance.getFunctionNamespaceType(), managedFunctionNameMapping,
				managedFunctionToObjectNameMapping, functionToFlowNameMapping, functionToEscalationTypeMapping,
				namespaceInstance.getManagedFunctionTypeNames());
	}

}