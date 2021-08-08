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
public class AddManagedFunctionTest extends AbstractSectionChangesTestCase {

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
		this.assertChange(change, null, "Add managed function FUNCTION", false,
				"Function FUNCTION already added to namespace NAMESPACE");
	}

}
