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

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.model.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.FunctionFlowModel;
import net.officefloor.model.desk.FunctionModel;
import net.officefloor.model.desk.FunctionNamespaceModel;
import net.officefloor.model.desk.ManagedFunctionModel;

/**
 * Tests adding a {@link FunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddFunctionTest extends AbstractDeskChangesTestCase {

	/**
	 * {@link ManagedFunctionModel}.
	 */
	private ManagedFunctionModel managedFunction;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the managed function model
		this.managedFunction = this.model.getFunctionNamespaces().get(0).getManagedFunctions().get(0);
	}

	/**
	 * Ensure can not apply change if {@link ManagedFunctionModel} is not on the
	 * {@link DeskModel} (specifically a {@link FunctionNamespaceModel} of the
	 * {@link DeskModel}).
	 */
	public void testManagedFunctionNotOnDesk() {

		// Create the managed function not on the desk
		ManagedFunctionModel managedFunction = new ManagedFunctionModel("NOT_ON_DESK");

		// Validate not able to add function if managed function not on desk
		ManagedFunctionType<?, ?> functionType = this.constructFunctionType("NOT_ON_DESK", null);
		Change<FunctionModel> change = this.operations.addFunction("FUNCTION", managedFunction, functionType);
		this.assertChange(change, null, "Add function FUNCTION", false, "Managed function NOT_ON_DESK not on desk");
		assertNotNull("Must have target", change.getTarget());
	}

	/**
	 * Ensures can not apply change if the {@link ManagedFunctionModel} name
	 * does not match the {@link ManagedFunctionType} name.
	 */
	public void testManagedFunctionNameMismatch() {

		// Create a function type with mismatched name
		ManagedFunctionType<?, ?> functionType = this.constructFunctionType("MISMATCH_FUNCTION_NAME", null);

		// Validate not able to add function if managed function name mismatch
		Change<FunctionModel> change = this.operations.addFunction("FUNCTION", this.managedFunction, functionType);
		this.assertChange(change, null, "Add function FUNCTION", false,
				"Function type MISMATCH_FUNCTION_NAME does not match managed function MANAGED_FUNCTION");
		assertNotNull("Must have target", change.getTarget());
	}

	/**
	 * Ensure can add {@link FunctionModel} that uses indexing.
	 */
	public void testAddFunctionWithoutKeys() {

		// Create the function type
		ManagedFunctionType<?, ?> function = this.constructFunctionType("MANAGED_FUNCTION", new FunctionConstructor() {
			@Override
			public void construct(FunctionTypeConstructor function) {
				function.addFlow(String.class, null);
				function.addFlow(Integer.class, null);
				function.addEscalation(SQLException.class);
				function.addEscalation(IOException.class);
				function.getBuilder().setReturnType(Double.class);
				// Should ignore objects
				function.addObject(Object.class, null);
			}
		});

		// Validate adding the function and reverting
		Change<FunctionModel> change = this.operations.addFunction("FUNCTION", this.managedFunction, function);
		this.assertChange(change, null, "Add function FUNCTION", true);
		change.apply();
		assertEquals("Ensure correct target", this.model.getFunctions().get(0), change.getTarget());
	}

	/**
	 * Ensure can add {@link FunctionModel} that uses keys.
	 */
	public void testAddFunctionWithKeys() {

		// Create the function type
		ManagedFunctionType<?, ?> function = this.constructFunctionType("MANAGED_FUNCTION", new FunctionConstructor() {
			@Override
			public void construct(FunctionTypeConstructor function) {
				// Can not have argument types for flows
				function.addFlow(null, FunctionFlowKeys.ONE);
				function.addFlow(null, FunctionFlowKeys.TWO);
				function.addEscalation(Exception.class);
			}
		});

		// Validate adding the function and reverting
		Change<FunctionModel> change = this.operations.addFunction("FUNCTION", this.managedFunction, function);
		this.assertChange(change, null, "Add function FUNCTION", true);
	}

	/**
	 * {@link FunctionFlowModel} keys.
	 */
	private enum FunctionFlowKeys {
		ONE, TWO
	}

	/**
	 * Ensure can add {@link FunctionModel} that has {@link FunctionFlowModel}
	 * instances with labels.
	 */
	public void testAddFunctionWithLabels() {

		// Create the function type
		ManagedFunctionType<?, ?> function = this.constructFunctionType("MANAGED_FUNCTION", new FunctionConstructor() {
			@Override
			public void construct(FunctionTypeConstructor function) {
				function.addFlow(String.class, null).setLabel("FLOW_ONE");
				function.addFlow(Integer.class, null).setLabel("FLOW_TWO");
				function.addEscalation(Exception.class).setLabel("ESCALATION");
			}
		});

		// Validate adding the function and reverting
		Change<FunctionModel> change = this.operations.addFunction("FUNCTION", this.managedFunction, function);
		this.assertChange(change, null, "Add function FUNCTION", true);
	}

	/**
	 * Ensure can add multiple {@link FunctionModel} instances ensuring ordering
	 * of the {@link FunctionModel} instances.
	 */
	public void testAddMultipleFunctionsEnsuringOrdering() {

		// Create the function type
		ManagedFunctionType<?, ?> functionType = this.constructFunctionType("MANAGED_FUNCTION", null);

		// Create the changes to add the functions
		Change<FunctionModel> changeB = this.operations.addFunction("FUNCTION_B", this.managedFunction, functionType);
		Change<FunctionModel> changeA = this.operations.addFunction("FUNCTION_A", this.managedFunction, functionType);
		Change<FunctionModel> changeC = this.operations.addFunction("FUNCTION_C", this.managedFunction, functionType);

		// Add the functions and ensure ordering
		changeB.apply();
		changeA.apply();
		changeC.apply();
		this.validateModel();

		// Revert
		changeC.revert();
		changeA.revert();
		changeB.revert();
		this.validateAsSetupModel();
	}

}