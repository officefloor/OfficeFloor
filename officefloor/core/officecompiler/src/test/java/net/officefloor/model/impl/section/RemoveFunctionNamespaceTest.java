/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
