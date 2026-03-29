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

import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;

/**
 * Tests removing a {@link ManagedFunctionModel} from a
 * {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveManagedFunctionTest extends AbstractSectionChangesTestCase {

	/**
	 * {@link FunctionNamespaceModel}.
	 */
	private FunctionNamespaceModel namespace;

	/**
	 * Initiate to use specific setup {@link SectionModel}.
	 */
	public RemoveManagedFunctionTest() {
		super(true);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the namespace model
		this.namespace = this.model.getFunctionNamespaces().get(0);
	}

	/**
	 * Tests attempting to remove a {@link ManagedFunctionModel} not on the
	 * {@link FunctionNamespaceModel}.
	 */
	public void testRemoveManagedFunctionNotOnFunctionNamespace() {
		ManagedFunctionModel managedFunction = new ManagedFunctionModel("NOT_ON_NAMESPACE");
		Change<ManagedFunctionModel> change = this.operations.removeManagedFunction(this.namespace, managedFunction);
		this.assertChange(change, managedFunction, "Remove managed function NOT_ON_NAMESPACE", false,
				"Managed function NOT_ON_NAMESPACE not on namespace NAMESPACE");
	}

	/**
	 * Ensure can remove the {@link ManagedFunctionModel} from the
	 * {@link FunctionNamespaceModel} when other {@link ManagedFunctionModel}
	 * instances on the {@link FunctionNamespaceModel}.
	 */
	public void testRemoveManagedFunctionWhenOtherManagedFunctions() {
		ManagedFunctionModel managedFunction = this.namespace.getManagedFunctions().get(1);
		Change<ManagedFunctionModel> change = this.operations.removeManagedFunction(this.namespace, managedFunction);
		this.assertChange(change, managedFunction, "Remove managed function FUNCTION_B", true);
	}

	/**
	 * Ensure can remove the connected {@link ManagedFunctionModel} from the
	 * {@link FunctionNamespaceModel}.
	 */
	public void testRemoveManagedFunctionWithConnections() {
		ManagedFunctionModel managedFunction = this.namespace.getManagedFunctions().get(0);
		Change<ManagedFunctionModel> change = this.operations.removeManagedFunction(this.namespace, managedFunction);
		this.assertChange(change, managedFunction, "Remove managed function MANAGED_FUNCTION_A", true);
	}

}
