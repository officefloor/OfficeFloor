/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.model.impl.section;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;

/**
 * Tests removing a {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveFunctionNamespaceTest extends AbstractSectionChangesTestCase {

	/**
	 * Specific setup file per test.
	 */
	public RemoveFunctionNamespaceTest() {
		super(true);
	}

	/**
	 * Ensure no {@link Change} for removing {@link FunctionNamespaceModel} not
	 * on the {@link SectionModel}.
	 */
	public void testRemoveFunctionNamespaceNotInSection() {
		// Attempt to remove namespace not in the section
		FunctionNamespaceModel namespaceNotOnSection = new FunctionNamespaceModel("NOT IN SECTION", null);
		Change<FunctionNamespaceModel> change = this.operations.removeFunctionNamespace(namespaceNotOnSection);
		this.assertChange(change, namespaceNotOnSection, "Remove namespace NOT IN SECTION", false,
				"Function namespace NOT IN SECTION not in section");
	}

	/**
	 * Ensure can remove {@link FunctionNamespaceModel} without any connections.
	 */
	public void testRemoveFunctionNamespaceWithNoConnections() {
		// Obtain the namespace and remove it
		FunctionNamespaceModel namespace = this.model.getFunctionNamespaces().get(0);
		Change<FunctionNamespaceModel> change = this.operations.removeFunctionNamespace(namespace);
		this.assertChange(change, namespace, "Remove namespace NAMESPACE", true);
	}

	/**
	 * Ensure can remove {@link FunctionNamespaceModel} with a
	 * {@link FunctionModel}.
	 */
	public void testRemoveFunctionNamespaceWithAFunction() {
		// Obtain the namespace and remove it
		FunctionNamespaceModel namespace = this.model.getFunctionNamespaces().get(0);
		Change<FunctionNamespaceModel> change = this.operations.removeFunctionNamespace(namespace);
		this.assertChange(change, namespace, "Remove namespace NAMESPACE", true);
	}

	/**
	 * Ensure can remove {@link FunctionNamespaceModel} with a
	 * {@link FunctionModel} while there are other
	 * {@link FunctionNamespaceModel} and {@link FunctionModel} instances.
	 */
	public void testRemoveFunctionNamespaceWhenOtherFunctionNamespaceAndFunctions() {
		// Obtain the namespace and remove it
		FunctionNamespaceModel namespace = this.model.getFunctionNamespaces().get(1);
		Change<FunctionNamespaceModel> change = this.operations.removeFunctionNamespace(namespace);
		this.assertChange(change, namespace, "Remove namespace NAMESPACE_B", true);
	}

	/**
	 * Ensure can remove {@link FunctionNamespaceModel} with
	 * {@link ConnectionModel} instances connected.
	 */
	public void testRemoveFunctionNamespaceWithConnections() {
		// Obtain the namespace and remove it
		FunctionNamespaceModel namespace = this.model.getFunctionNamespaces().get(0);
		Change<FunctionNamespaceModel> change = this.operations.removeFunctionNamespace(namespace);
		this.assertChange(change, namespace, "Remove namespace NAMESPACE_A", true);
	}

}
