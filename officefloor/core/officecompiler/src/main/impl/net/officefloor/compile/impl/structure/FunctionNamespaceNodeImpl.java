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

package net.officefloor.compile.impl.structure;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.ManagedFunctionRegistry;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;

/**
 * {@link FunctionNamespaceNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class FunctionNamespaceNodeImpl implements FunctionNamespaceNode {

	/**
	 * Name of this {@link SectionFunctionNamespace}.
	 */
	private final String namespaceName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link OfficeSection} containing this {@link FunctionNamespaceNode}.
	 */
	private final SectionNode section;

	/**
	 * {@link ManagedFunctionRegistry}.
	 */
	private final ManagedFunctionRegistry functionRegistry;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initialised state.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {

		/**
		 * Class name of the {@link ManagedFunctionSource}.
		 */
		private final String managedFunctionSourceClassName;

		/**
		 * {@link ManagedFunctionSource} instance to use. If this is specified it
		 * overrides using the {@link Class} name.
		 */
		private final ManagedFunctionSource managedFunctionSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedFunctionSourceClassName Class name of the
		 *                                       {@link ManagedFunctionSource}.
		 * @param managedFunctionSource          {@link ManagedFunctionSource} instance
		 *                                       to use. If this is specified it
		 *                                       overrides using the {@link Class} name.
		 */
		public InitialisedState(String managedFunctionSourceClassName, ManagedFunctionSource managedFunctionSource) {
			this.managedFunctionSourceClassName = managedFunctionSourceClassName;
			this.managedFunctionSource = managedFunctionSource;
		}
	}

	/**
	 * Used {@link ManagedFunctionSource}.
	 */
	private ManagedFunctionSource usedManagedFunctionSource = null;

	/**
	 * Instantiate.
	 * 
	 * @param namespaceName Name of this {@link SectionFunctionNamespace}.
	 * @param section       {@link OfficeSection} containing this
	 *                      {@link FunctionNamespaceNode}.
	 * @param context       {@link NodeContext}.
	 */
	public FunctionNamespaceNodeImpl(String namespaceName, SectionNode section, NodeContext context) {
		this.namespaceName = namespaceName;
		this.section = section;
		this.functionRegistry = section;
		this.context = context;

		// Create additional objects
		this.propertyList = this.context.createPropertyList();
	}

	/*
	 * ======================== Node ============================
	 */

	@Override
	public String getNodeName() {
		return this.namespaceName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return this.section;
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes();
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String managedFunctionSourceClassName, ManagedFunctionSource managedFunctionSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(managedFunctionSourceClassName, managedFunctionSource));
	}

	/*
	 * ====================== SectionFunctionNamespace ======================
	 */

	@Override
	public String getSectionFunctionNamespaceName() {
		return this.namespaceName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public SectionFunction addSectionFunction(String functionName, String functionTypeName) {
		return this.functionRegistry.addManagedFunctionNode(functionName, functionTypeName, this);
	}

	/*
	 * ===================== FunctionNamespaceNode =====================
	 */

	@Override
	public SectionNode getSectionNode() {
		return this.section;
	}

	@Override
	public FunctionNamespaceType loadFunctionNamespaceType(boolean isLoadingType) {

		// Obtain the managed function source class
		ManagedFunctionSource managedFunctionSource = this.state.managedFunctionSource;
		if (managedFunctionSource == null) {

			// Load the managed function source class
			Class<? extends ManagedFunctionSource> managedFunctionSourceClass = this.context
					.getManagedFunctionSourceClass(this.state.managedFunctionSourceClassName, this);
			if (managedFunctionSourceClass == null) {
				return null; // must obtain managed function source class
			}

			// Load the managed function source
			managedFunctionSource = CompileUtil.newInstance(managedFunctionSourceClass, ManagedFunctionSource.class,
					this, this.context.getCompilerIssues());
			if (managedFunctionSource == null) {
				return null; // must obtain managed function source
			}
		}

		// Keep track of the managed function source
		this.usedManagedFunctionSource = managedFunctionSource;

		// Load and return the managed function type
		ManagedFunctionLoader managedFunctionLoader = this.context.getManagedFunctionLoader(this, isLoadingType);
		return managedFunctionLoader.loadManagedFunctionType(managedFunctionSource, this.propertyList);
	}

	@Override
	public void registerAsPossibleMbean(CompileContext compileContext) {
		// Register only once
		if (this.usedManagedFunctionSource != null) {

			// Register as possible MBean
			String qualifiedName = this.section.getQualifiedName(this.namespaceName);
			compileContext.registerPossibleMBean(ManagedFunctionSource.class, qualifiedName,
					this.usedManagedFunctionSource);

			// Clear, so only loaded once
			this.usedManagedFunctionSource = null;
		}
	}

}
