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
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierType;

/**
 * {@link SuppliedManagedObjectSourceNodeImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectSourceNodeImpl implements SuppliedManagedObjectSourceNode {

	/**
	 * Generates the name for the {@link SuppliedManagedObjectSource}.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type.
	 * @return {@link SuppliedManagedObjectSource} name.
	 */
	public static String getSuppliedManagedObjectSourceName(String qualifier, String type) {
		return CompileUtil.isBlank(qualifier) ? type : qualifier + "-" + type;
	}

	/**
	 * Qualifier. May be <code>null</code>.
	 */
	private final String qualifier;

	/**
	 * Type.
	 */
	private final String type;

	/**
	 * {@link SupplierNode}.
	 */
	private final SupplierNode supplierNode;

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
	}

	/**
	 * Initiate.
	 * 
	 * @param qualifier    Qualifier. May be <code>null</code>.
	 * @param type         Type.
	 * @param supplierNode {@link SupplierNode}.
	 * @param context      {@link NodeContext}.
	 */
	public SuppliedManagedObjectSourceNodeImpl(String qualifier, String type, SupplierNode supplierNode,
			NodeContext context) {
		this.qualifier = qualifier;
		this.type = type;
		this.supplierNode = supplierNode;
		this.context = context;
	}

	/*
	 * ===================== Node =================
	 */

	@Override
	public String getNodeName() {
		return (this.qualifier != null ? this.qualifier + "-" : "") + this.type;
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
		return this.supplierNode;
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
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState());
	}

	/*
	 * ===================== SuppliedManagedObjectSourceNode =================
	 */

	@Override
	public SupplierNode getSupplierNode() {
		return this.supplierNode;
	}

	@Override
	public SuppliedManagedObjectSourceType loadSuppliedManagedObjectSourceType(CompileContext compileContext) {

		// Loads the supplier type
		SupplierType supplierType = compileContext.getOrLoadSupplierType(this.supplierNode);
		if (supplierType == null) {
			return null; // must have type
		}

		// Find the corresponding supplied managed object source
		final AutoWire nodeAutoWire = new AutoWire(this.qualifier, this.type);
		for (SuppliedManagedObjectSourceType type : supplierType.getSuppliedManagedObjectTypes()) {
			if (nodeAutoWire.equals(new AutoWire(type.getQualifier(), type.getObjectType()))) {
				return type; // found corresponding type
			}
		}

		// As here, did not find type
		this.context.getCompilerIssues().addIssue(this,
				"No " + SuppliedManagedObjectSource.class.getSimpleName() + " for " + nodeAutoWire.toString());
		return null;
	}

}
