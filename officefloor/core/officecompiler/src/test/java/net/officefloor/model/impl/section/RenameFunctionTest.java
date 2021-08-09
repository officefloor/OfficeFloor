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
import net.officefloor.model.section.FunctionModel;

/**
 * Tests renaming the {@link FunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RenameFunctionTest extends AbstractSectionChangesTestCase {

	/**
	 * Ensures handles {@link FunctionModel} not being on the
	 * {@link SectionModel}.
	 */
	public void testRenameFunctionNotOnSection() {
		FunctionModel function = new FunctionModel("NOT_IN_SECTION", false, "NAMESPACE", "MANAGED_FUNCTION", null);
		Change<FunctionModel> change = this.operations.renameFunction(function, "NEW_NAME");
		this.assertChange(change, function, "Rename function NOT_IN_SECTION to NEW_NAME", false,
				"Function NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can rename the {@link FunctionModel}.
	 */
	public void testRenameFunction() {
		FunctionModel function = this.model.getFunctions().get(0);
		assertNotNull("Ensure have function", function);
		Change<FunctionModel> change = this.operations.renameFunction(function, "NEW_NAME");
		this.assertChange(change, function, "Rename function OLD_NAME to NEW_NAME", true);
	}

	/**
	 * Ensures on renaming the {@link FunctionModel} that order is maintained.
	 */
	public void testRenameFunctionCausingFunctionOrderChange() {
		this.useTestSetupModel();
		FunctionModel function = this.model.getFunctions().get(0);
		Change<FunctionModel> change = this.operations.renameFunction(function, "FUNCTION_C");
		this.assertChange(change, function, "Rename function FUNCTION_A to FUNCTION_C", true);
	}
}
