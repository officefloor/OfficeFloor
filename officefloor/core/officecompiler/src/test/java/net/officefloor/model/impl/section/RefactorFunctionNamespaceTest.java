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

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.FunctionEscalationModel;
import net.officefloor.model.section.FunctionFlowModel;
import net.officefloor.model.section.FunctionModel;
import net.officefloor.model.section.FunctionNamespaceModel;
import net.officefloor.model.section.ManagedFunctionModel;
import net.officefloor.model.section.ManagedFunctionObjectModel;

/**
 * Tests refactoring the {@link FunctionNamespaceModel} to a
 * {@link FunctionNamespaceType} via the {@link SectionChanges}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorFunctionNamespaceTest extends AbstractRefactorNamespaceTest {

	/**
	 * Tests renaming the {@link FunctionNamespaceModel}.
	 */
	public void testRenameFunctionNamespace() {
		this.refactor_namespaceName("NEW_NAME");
		this.doRefactor();
	}

	/**
	 * Tests changing the {@link ManagedFunctionSource} for the
	 * {@link FunctionNamespaceModel}.
	 */
	public void testChangeManagedFunctionSourceClass() {
		this.refactor_managedFunctionSourceClassName("net.another.AnotherManagedFunctionSource");
		this.doRefactor();
	}

	/**
	 * Tests changing the {@link PropertyList} for the
	 * {@link FunctionNamespaceModel}.
	 */
	public void testChangeProperties() {
		this.refactor_addProperty("ANOTHER_NAME", "ANOTHER_VALUE");
		this.doRefactor();
	}

	/**
	 * Tests removing a {@link ManagedFunctionModel}.
	 */
	public void testRemoveManagedFunction() {
		this.refactor_mapFunction("FUNCTION_B", "FUNCTION_B");
		this.refactor_includeFunctions("FUNCTION_B");
		this.doRefactor(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				context.addFunction("FUNCTION_B");
			}
		});
	}

	/**
	 * Tests removing a {@link ManagedFunctionModel} with connections.
	 */
	public void testRemoveManagedFunctionWithConnections() {
		this.refactor_mapFunction("FUNCTION_B", "FUNCTION_B");
		this.refactor_includeFunctions("FUNCTION_B");
		this.doRefactor(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				context.addFunction("FUNCTION_B");
			}
		});
	}

	/**
	 * Tests not including the {@link ManagedFunctionModel}.
	 */
	public void testNotIncludeManagedFunction() {
		this.refactor_mapFunction("FUNCTION_A", "FUNCTION_A");
		this.refactor_mapFunction("FUNCTION_B", "FUNCTION_B");
		this.refactor_includeFunctions("FUNCTION_B"); // not include FUNCTION_A
		this.doRefactor(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				// Function A
				FunctionTypeConstructor functionA = context.addFunction("FUNCTION_A");
				functionA.addFlow(Object.class, null).setLabel("FLOW_A");
				functionA.addFlow(Object.class, null).setLabel("FLOW_B");
				functionA.addEscalation(SQLException.class);
				functionA.addEscalation(IOException.class);
				functionA.addObject(Object.class, null).setLabel("OBJECT");

				// Function B
				context.addFunction("FUNCTION_B");
			}
		});
	}

	/**
	 * Tests refactoring the {@link ManagedFunctionModel} and
	 * {@link FunctionModel}.
	 */
	public void testRefactorManagedFunction() {
		this.refactor_mapFunction("FUNCTION_NEW", "FUNCTION_OLD");
		this.refactor_includeFunctions("FUNCTION_NEW");
		this.doRefactor(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				FunctionTypeConstructor function = context.addFunction("FUNCTION_NEW");
				function.getBuilder().setReturnType(String.class);
			}
		});
	}

	/**
	 * Keys.
	 */
	public static enum Key {
		KEY_ONE, KEY_TWO
	}

	/**
	 * Tests refactoring the {@link ManagedFunctionObjectModel} instances.
	 */
	public void testRefactorManagedFunctionObjects() {
		this.refactor_mapFunction("FUNCTION", "FUNCTION");
		this.refactor_mapObject("FUNCTION", "CHANGE_DETAILS", "CHANGE_DETAILS");
		this.refactor_mapObject("FUNCTION", "RENAME_NEW", "RENAME_OLD");
		this.refactor_mapObject("FUNCTION", "REORDER_A", "REORDER_A");
		this.refactor_mapObject("FUNCTION", "REORDER_B", "REORDER_B");
		this.doRefactor(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				FunctionTypeConstructor function = context.addFunction("FUNCTION");
				function.addObject(String.class, Key.KEY_TWO).setLabel("CHANGE_DETAILS");
				function.addObject(Integer.class, null).setLabel("ADDED");
				function.addObject(String.class, null).setLabel("RENAME_NEW");
				function.addObject(Double.class, null).setLabel("REORDER_B");
				function.addObject(Float.class, null).setLabel("REORDER_A");
			}
		});
	}

	/**
	 * Tests refactoring the {@link FunctionFlowModel} instances.
	 */
	public void testRefactorFunctionFlows() {
		this.refactor_mapFunction("MANAGED_FUNCTION", "MANAGED_FUNCTION");
		this.refactor_mapFlow("FUNCTION", "CHANGE_DETAILS", "CHANGE_DETAILS");
		this.refactor_mapFlow("FUNCTION", "RENAME_NEW", "RENAME_OLD");
		this.refactor_mapFlow("FUNCTION", "REORDER_A", "REORDER_A");
		this.refactor_mapFlow("FUNCTION", "REORDER_B", "REORDER_B");
		this.doRefactor(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				FunctionTypeConstructor function = context.addFunction("MANAGED_FUNCTION");
				function.getBuilder().setReturnType(Long.class);
				function.addFlow(Byte.class, Key.KEY_TWO).setLabel("CHANGE_DETAILS");
				function.addFlow(String.class, null).setLabel("ADDED");
				function.addFlow(Integer.class, null).setLabel("RENAME_NEW");
				function.addFlow(Double.class, null).setLabel("REORDER_B");
				function.addFlow(Float.class, null).setLabel("REORDER_A");
			}
		});
	}

	/**
	 * Tests refactoring the {@link FunctionEscalationModel} instances.
	 */
	public void testRefactorFunctionEscalations() {
		this.refactor_mapFunction("MANAGED_FUNCTION", "MANAGED_FUNCTION");
		this.refactor_mapEscalation("FUNCTION", RuntimeException.class.getName(), NullPointerException.class.getName());
		this.refactor_mapEscalation("FUNCTION", Exception.class.getName(), Exception.class.getName());
		this.refactor_mapEscalation("FUNCTION", Error.class.getName(), Error.class.getName());
		this.doRefactor(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				FunctionTypeConstructor function = context.addFunction("MANAGED_FUNCTION");
				function.addEscalation(RuntimeException.class);
				function.addEscalation(SQLException.class);
				function.addEscalation(Error.class);
				function.addEscalation(Exception.class);
			}
		});
	}

}
