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
package net.officefloor.compile.impl.structure;

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
		 * {@link ManagedFunctionSource} instance to use. If this is specified
		 * it overrides using the {@link Class} name.
		 */
		@SuppressWarnings("unused")
		private final ManagedFunctionSource managedFunctionSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedFunctionSourceClassName
		 *            Class name of the {@link ManagedFunctionSource}.
		 * @param managedFunctionSource
		 *            {@link ManagedFunctionSource} instance to use. If this is
		 *            specified it overrides using the {@link Class} name.
		 */
		public InitialisedState(String managedFunctionSourceClassName, ManagedFunctionSource managedFunctionSource) {
			this.managedFunctionSourceClassName = managedFunctionSourceClassName;
			this.managedFunctionSource = managedFunctionSource;
		}
	}

	/**
	 * Instantiate.
	 * 
	 * @param namespaceName
	 *            Name of this {@link SectionFunctionNamespace}.
	 * @param section
	 *            {@link OfficeSection} containing this
	 *            {@link FunctionNamespaceNode}.
	 * @param context
	 *            {@link NodeContext}.
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
	public String getQualifiedFunctionNamespaceName() {
		return this.section.getSectionQualifiedName(this.namespaceName);
	}

	@Override
	public FunctionNamespaceType loadFunctionNamespaceType() {

		// Obtain the managed function source class
		Class<? extends ManagedFunctionSource> managedFunctionSourceClass = this.context
				.getManagedFunctionSourceClass(this.state.managedFunctionSourceClassName, this);
		if (managedFunctionSourceClass == null) {
			return null; // must obtain managed function source class
		}

		// Load and return the managed function type
		ManagedFunctionLoader managedFunctionLoader = this.context.getManagedFunctionLoader(this);
		return managedFunctionLoader.loadFunctionNamespaceType(managedFunctionSourceClass, this.propertyList);
	}

}