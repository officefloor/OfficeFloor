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

/**
 * Tests renaming the {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RenameFunctionNamespaceTest extends AbstractSectionChangesTestCase {

	/**
	 * Ensures handles {@link FunctionNamespaceModel} not being on the
	 * {@link SectionModel}.
	 */
	public void testRenameFunctionNamespaceNotOnSection() {
		FunctionNamespaceModel namespace = new FunctionNamespaceModel("NOT_IN_SECTION", null);
		Change<FunctionNamespaceModel> change = this.operations.renameFunctionNamespace(namespace, "NEW_NAME");
		this.assertChange(change, namespace, "Rename namespace NOT_IN_SECTION to NEW_NAME", false,
				"Function namespace NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can rename the {@link FunctionNamespaceModel}.
	 */
	public void testRenameFunctionNamespace() {
		FunctionNamespaceModel namespace = this.model.getFunctionNamespaces().get(0);
		Change<FunctionNamespaceModel> change = this.operations.renameFunctionNamespace(namespace, "NEW_NAME");
		this.assertChange(change, namespace, "Rename namespace OLD_NAME to NEW_NAME", true);
	}

	/**
	 * Ensures on renaming the {@link FunctionNamespaceModel} that order is
	 * maintained.
	 */
	public void testRenameFunctionNamespaceCausingFunctionNamespaceOrderChange() {
		this.useTestSetupModel();
		FunctionNamespaceModel namespace = this.model.getFunctionNamespaces().get(0);
		Change<FunctionNamespaceModel> change = this.operations.renameFunctionNamespace(namespace, "NAMESPACE_C");
		this.assertChange(change, namespace, "Rename namespace NAMESPACE_A to NAMESPACE_C", true);
	}
}
