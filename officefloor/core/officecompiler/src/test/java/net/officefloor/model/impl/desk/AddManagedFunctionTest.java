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

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;

/**
 * Tests adding a {@link ManagedFunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddManagedFunctionTest extends AbstractDeskChangesTestCase {

	/**
	 * {@link FunctionNamespaceModel}.
	 */
	private FunctionNamespaceModel namespace;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the namespace model
		this.namespace = this.model.getFunctionNamespaces().get(0);
	}

	/**
	 * Ensure can add the {@link ManagedFunctionModel} that has
	 * {@link ManagedFunctionObjectModel} instances indexed.
	 */
	public void testAddManagedFunctionWithoutKeys() {

		// Create the function type
		ManagedFunctionType<?, ?> function = this.constructFunctionType("FUNCTION", new FunctionConstructor() {
			@Override
			public void construct(FunctionTypeConstructor function) {
				function.addObject(Integer.class, null);
				function.addObject(String.class, null);
				// Should ignore flows and escalations
				function.addFlow(Object.class, null);
				function.addEscalation(Throwable.class);
			}
		});

		// Validate adding the managed function and reverting
		Change<ManagedFunctionModel> change = this.operations.addManagedFunction(this.namespace, function);
		this.assertChange(change, null, "Add managed function FUNCTION", true);
		change.apply();
		assertEquals("Ensure correct target", this.namespace.getManagedFunctions().get(0), change.getTarget());
	}

	/**
	 * Ensure can add the {@link ManagedFunctionModel} that has
	 * {@link ManagedFunctionObjectModel} instances keyed.
	 */
	public void testAddManagedFunctionWithKeys() {

		// Create the function type
		ManagedFunctionType<?, ?> function = this.constructFunctionType("FUNCTION", new FunctionConstructor() {
			@Override
			public void construct(FunctionTypeConstructor function) {
				function.addObject(Integer.class, FunctionObjectKeys.ONE);
				function.addObject(String.class, FunctionObjectKeys.TWO);
			}
		});

		// Validate adding the namespace function and reverting
		Change<ManagedFunctionModel> change = this.operations.addManagedFunction(this.namespace, function);
		this.assertChange(change, null, "Add managed function FUNCTION", true);
	}

	/**
	 * {@link ManagedFunctionObjectModel} keys.
	 */
	private enum FunctionObjectKeys {
		ONE, TWO
	}

	/**
	 * Ensure can add the {@link ManagedFunctionModel} that has
	 * {@link ManagedFunctionObjectModel} instances with labels.
	 */
	public void testAddManagedFunctionWithLabels() {

		// Create the function type
		ManagedFunctionType<?, ?> function = this.constructFunctionType("FUNCTION", new FunctionConstructor() {
			@Override
			public void construct(FunctionTypeConstructor function) {
				function.addObject(Integer.class, null).setLabel("OBJECT_ONE");
				function.addObject(String.class, null).setLabel("OBJECT_TWO");
			}
		});

		// Validate adding the managed function and reverting
		Change<ManagedFunctionModel> change = this.operations.addManagedFunction(this.namespace, function);
		this.assertChange(change, null, "Add managed function FUNCTION", true);
	}

	/**
	 * Ensure can add the {@link ManagedFunctionModel} that has
	 * {@link ManagedFunctionObjectModel} instances with labels.
	 */
	public void testAddMultipleManagedFunctionsEnsuringOrdering() {

		// Create the function type
		ManagedFunctionType<?, ?> functionB = this.constructFunctionType("FUNCTION_B", null);
		ManagedFunctionType<?, ?> functionA = this.constructFunctionType("FUNCTION_A", null);
		ManagedFunctionType<?, ?> functionC = this.constructFunctionType("FUNCTION_C", null);

		// Create the changes to add the managed functions
		Change<ManagedFunctionModel> changeB = this.operations.addManagedFunction(this.namespace, functionB);
		Change<ManagedFunctionModel> changeA = this.operations.addManagedFunction(this.namespace, functionA);
		Change<ManagedFunctionModel> changeC = this.operations.addManagedFunction(this.namespace, functionC);

		// Add the managed functions and ensure ordering
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

	/**
	 * Ensure can add the {@link ManagedFunctionType} only once to the
	 * {@link FunctionNamespaceModel}.
	 */
	public void testAddManagedFunctionOnlyOnceFornamespace() {

		// Create the function type and have it added
		ManagedFunctionType<?, ?> function = this.constructFunctionType("FUNCTION", null);
		this.operations.addManagedFunction(this.namespace, function).apply();

		// Create the change to add the function
		Change<ManagedFunctionModel> change = this.operations.addManagedFunction(this.namespace, function);
		this.assertChange(change, null, "Add managed function FUNCTION", false, "Function FUNCTION already added to namespace NAMESPACE");
	}

}