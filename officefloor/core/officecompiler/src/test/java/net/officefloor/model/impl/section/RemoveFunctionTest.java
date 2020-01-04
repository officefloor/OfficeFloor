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
