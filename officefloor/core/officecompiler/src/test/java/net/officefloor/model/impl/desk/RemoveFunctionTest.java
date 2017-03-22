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
package net.officefloor.model.impl.desk;

import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.FunctionModel;

/**
 * Tests removing a {@link FunctionModel} from a {@link DeskModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveFunctionTest extends AbstractDeskChangesTestCase {

	/**
	 * Initiate to use specific setup {@link DeskModel}.
	 */
	public RemoveFunctionTest() {
		super(true);
	}

	/**
	 * Tests attempting to remove a {@link FunctionModel} not on the
	 * {@link DeskModel}.
	 */
	public void testRemoveFunctionNotOnDesk() {
		FunctionModel function = new FunctionModel("NOT_ON_DESK", false, "WORK",
				"WORK_Function", null);
		Change<FunctionModel> change = this.operations.removeFunction(function);
		this.assertChange(change, function, "Remove function NOT_ON_DESK", false,
				"Function NOT_ON_DESK not on desk");
	}

	/**
	 * Ensure can remove the {@link FunctionModel} from the {@link DeskModel} when
	 * other {@link FunctionModel} instances on the {@link DeskModel}.
	 */
	public void testRemoveFunctionWhenOtherFunctions() {
		FunctionModel function = this.model.getFunctions().get(1);
		Change<FunctionModel> change = this.operations.removeFunction(function);
		this.assertChange(change, function, "Remove function FUNCTION_B", true);
	}

	/**
	 * Ensure can remove the connected {@link FunctionModel} from the
	 * {@link DeskModel}.
	 */
	public void testRemoveFunctionWithConnections() {
		FunctionModel function = this.model.getFunctions().get(0);
		Change<FunctionModel> change = this.operations.removeFunction(function);
		this.assertChange(change, function, "Remove function FUNCTION_A", true);
	}

}