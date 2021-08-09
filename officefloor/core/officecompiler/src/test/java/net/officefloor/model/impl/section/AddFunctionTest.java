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

import java.io.IOException;
import java.sql.SQLException;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;

/**
 * Tests adding a {@link FunctionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddFunctionTest extends AbstractSectionChangesTestCase {

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
	 * {@link SectionModel} (specifically a {@link FunctionNamespaceModel} of
	 * the {@link SectionModel}).
	 */
	public void testManagedFunctionNotOnSection() {

		// Create the managed function not un the section
		ManagedFunctionModel managedFunction = new ManagedFunctionModel("NOT_IN_SECTION");

		// Validate not able to add function if managed function not in section
		ManagedFunctionType<?, ?> functionType = this.constructFunctionType("NOT_IN_SECTION", null);
		Change<FunctionModel> change = this.operations.addFunction("FUNCTION", managedFunction, functionType);
		this.assertChange(change, null, "Add function FUNCTION", false,
				"Managed function NOT_IN_SECTION not in section");
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
