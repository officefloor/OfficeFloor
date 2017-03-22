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
import net.officefloor.model.section.DeskModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;

/**
 * Tests removing a {@link ManagedFunctionModel} from a {@link FunctionNamespaceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveManagedFunctionTest extends AbstractDeskChangesTestCase {

	/**
	 * {@link FunctionNamespaceModel}.
	 */
	private FunctionNamespaceModel namespace;

	/**
	 * Initiate to use specific setup {@link DeskModel}.
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
		Change<ManagedFunctionModel> change = this.operations.removeManagedFunction(
				this.namespace, managedFunction);
		this.assertChange(change, managedFunction, "Remove managed function NOT_ON_NAMESPACE",
				false, "Managed function NOT_ON_NAMESPACE not on namespace NAMESPACE");
	}

	/**
	 * Ensure can remove the {@link ManagedFunctionModel} from the {@link FunctionNamespaceModel}
	 * when other {@link ManagedFunctionModel} instances on the {@link FunctionNamespaceModel}.
	 */
	public void testRemoveManagedFunctionWhenOtherManagedFunctions() {
		ManagedFunctionModel managedFunction = this.namespace.getManagedFunctions().get(1);
		Change<ManagedFunctionModel> change = this.operations.removeManagedFunction(
				this.namespace, managedFunction);
		this.assertChange(change, managedFunction, "Remove managed function FUNCTION_B", true);
	}

	/**
	 * Ensure can remove the connected {@link ManagedFunctionModel} from the
	 * {@link FunctionNamespaceModel}.
	 */
	public void testRemoveManagedFunctionWithConnections() {
		ManagedFunctionModel managedFunction = this.namespace.getManagedFunctions().get(0);
		Change<ManagedFunctionModel> change = this.operations.removeManagedFunction(
				this.namespace, managedFunction);
		this.assertChange(change, managedFunction, "Remove namespace function MANAGED_FUNCTION_A",
				true);
	}

}