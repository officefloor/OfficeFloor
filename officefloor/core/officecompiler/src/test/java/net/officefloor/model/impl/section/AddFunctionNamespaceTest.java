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

import java.sql.Connection;

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.model.change.Change;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;

/**
 * Tests the {@link SectionChanges}.
 * 
 * @author Daniel Sagenschneider
 */
public class AddFunctionNamespaceTest extends AbstractSectionChangesTestCase {

	/**
	 * Ensure can add {@link FunctionNamespaceModel} that uses indexing.
	 */
	public void testAddNamespaceWithoutKeys() {

		// Create the namespace type to add
		FunctionNamespaceType namespace = this.constructNamespaceType(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				// First function
				FunctionTypeConstructor functionOne = context.addFunction("FUNCTION_ONE");
				functionOne.addObject(String.class, null);
				functionOne.addObject(Integer.class, null);
				functionOne.addFlow(String.class, null); // ignored
				functionOne.addEscalation(Throwable.class); // ignored

				// Second function (for labels)
				FunctionTypeConstructor functionTwo = context.addFunction("FUNCTION_TWO");
				functionTwo.addObject(Object.class, null).setLabel("LABEL");
			}
		});

		// Validate adding the namespace and reverting
		Change<FunctionNamespaceModel> change = this.operations.addFunctionNamespace("NAMESPACE",
				"net.example.ExampleManagedFunctionSource",
				new PropertyListImpl("name.one", "value.one", "name.two", "value.two"), namespace);
		this.assertChange(change, null, "Add function namespace NAMESPACE", true);
		change.apply();
		assertEquals("Ensure correct target", this.model.getFunctionNamespaces().get(0), change.getTarget());
	}

	/**
	 * Ensure can add {@link FunctionNamespaceModel} that uses keys.
	 */
	public void testAddNamespaceWithKeys() {

		// Create the namespace type to add
		FunctionNamespaceType namespace = this.constructNamespaceType(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				// First function
				FunctionTypeConstructor functionOne = context.addFunction("FUNCTION_ONE");
				functionOne.addObject(String.class, FunctionObjectKeys.ONE);
				functionOne.addObject(Integer.class, FunctionObjectKeys.TWO);
				functionOne.addFlow(String.class, null); // ignored
				functionOne.addEscalation(Throwable.class); // ignored

				// Second function (for labels)
				FunctionTypeConstructor functionTwo = context.addFunction("FUNCTION_TWO");
				functionTwo.addObject(Object.class, FunctionObjectKeys.ONE).setLabel("LABEL_ONE");
				functionTwo.addObject(Connection.class, FunctionObjectKeys.TWO).setLabel("LABEL_TWO");
			}
		});

		// Validate adding the namespace and reverting
		Change<FunctionNamespaceModel> change = this.operations.addFunctionNamespace("NAMESPACE",
				"net.example.ExampleManagedFunctionSource", new PropertyListImpl("name", "value"), namespace);
		this.assertChange(change, null, "Add function namespace NAMESPACE", true);
	}

	/**
	 * Keys identifying {@link ManagedFunctionObjectType} instances.
	 */
	private enum FunctionObjectKeys {
		ONE, TWO
	}

	/**
	 * Ensure can add {@link FunctionNamespaceModel} specifying the functions to
	 * be added.
	 */
	public void testAddNamespaceWithSubsetOfFunctions() {

		// Create the namespace type to add
		FunctionNamespaceType namespace = this.constructNamespaceType(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				// Add many functions
				context.addFunction("FUNCTION_ONE");
				context.addFunction("FUNCTION_TWO");
				context.addFunction("FUNCTION_THREE");
				context.addFunction("FUNCTION_FOUR");
			}
		});

		// Validate adding the namespace and reverting
		Change<FunctionNamespaceModel> change = this.operations.addFunctionNamespace("NAMESPACE",
				"net.example.ExampleManagedFunctionSource", new PropertyListImpl(), namespace, "FUNCTION_ONE",
				"FUNCTION_THREE");
		this.assertChange(change, null, "Add function namespace NAMESPACE", true);
	}

	/**
	 * Ensure can add {@link FunctionNamespaceModel} and that the
	 * {@link ManagedFunctionModel} instances are ordered by the
	 * {@link ManagedFunction} name to make merging the XML files easier under
	 * SCM.
	 */
	public void testAddNamespaceEnsuringManagedFunctionOrdering() {

		// Create the namespace type to add
		FunctionNamespaceType namespace = this.constructNamespaceType(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				// Add in wrong order
				context.addFunction("D");
				context.addFunction("A");
				context.addFunction("C");
				context.addFunction("B");
			}
		});

		// Validate adding the namespace and reverting
		Change<FunctionNamespaceModel> change = this.operations.addFunctionNamespace("NAMESPACE",
				"net.example.ExampleManagedFunctionSource", new PropertyListImpl(), namespace);
		this.assertChange(change, null, "Add function namespace NAMESPACE", true);
	}

	/**
	 * Ensure can add multiple {@link FunctionNamespaceModel} instances ensuring
	 * ordering of the {@link FunctionNamespaceModel} instances.
	 */
	public void testAddMultipleNamespaceEnsuringNamespaceOrdering() {

		// Create the namespace type
		FunctionNamespaceType namespace = this.constructNamespaceType(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				context.addFunction("FUNCTION");
			}
		});

		// Add namespace multiple times
		Change<FunctionNamespaceModel> changeB = this.operations.addFunctionNamespace("NAMESPACE_B",
				"net.example.ExampleManagedFunctionSource", new PropertyListImpl(), namespace);
		Change<FunctionNamespaceModel> changeA = this.operations.addFunctionNamespace("NAMESPACE_A",
				"net.example.ExampleManagedFunctionSource", new PropertyListImpl(), namespace);
		Change<FunctionNamespaceModel> changeC = this.operations.addFunctionNamespace("NAMESPACE_C",
				"net.example.ExampleManagedFunctionSource", new PropertyListImpl(), namespace);

		// Apply the changes
		changeB.apply();
		changeA.apply();
		changeC.apply();
		this.validateModel();

		// Ensure can revert changes (undo)
		changeC.revert();
		changeA.revert();
		changeB.revert();
		this.validateAsSetupModel();
	}

}
