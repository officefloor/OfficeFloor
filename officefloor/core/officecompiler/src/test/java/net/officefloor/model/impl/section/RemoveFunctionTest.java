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
 * Tests removing a {@link FunctionModel} from a {@link SectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveFunctionTest extends AbstractSectionChangesTestCase {

	/**
	 * Initiate to use specific setup {@link SectionModel}.
	 */
	public RemoveFunctionTest() {
		super(true);
	}

	/**
	 * Tests attempting to remove a {@link FunctionModel} not on the
	 * {@link SectionModel}.
	 */
	public void testRemoveFunctionNotInSection() {
		FunctionModel function = new FunctionModel("NOT_IN_SECTION", false, "NAMESPACE", "MANAGED_FUNCTION", null);
		Change<FunctionModel> change = this.operations.removeFunction(function);
		this.assertChange(change, function, "Remove function NOT_IN_SECTION", false,
				"Function NOT_IN_SECTION not in section");
	}

	/**
	 * Ensure can remove the {@link FunctionModel} from the {@link SectionModel}
	 * when other {@link FunctionModel} instances on the {@link SectionModel}.
	 */
	public void testRemoveFunctionWhenOtherFunctions() {
		FunctionModel function = this.model.getFunctions().get(1);
		Change<FunctionModel> change = this.operations.removeFunction(function);
		this.assertChange(change, function, "Remove function FUNCTION_B", true);
	}

	/**
	 * Ensure can remove the connected {@link FunctionModel} from the
	 * {@link SectionModel}.
	 */
	public void testRemoveFunctionWithConnections() {
		FunctionModel function = this.model.getFunctions().get(0);
		Change<FunctionModel> change = this.operations.removeFunction(function);
		this.assertChange(change, function, "Remove function FUNCTION_A", true);
	}

}
