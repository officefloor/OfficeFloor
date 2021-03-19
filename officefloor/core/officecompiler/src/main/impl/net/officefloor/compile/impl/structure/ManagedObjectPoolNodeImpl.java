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

package net.officefloor.compile.impl.structure;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkPoolNode;
import net.officefloor.compile.internal.structure.ManagedObjectPoolNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.pool.ManagedObjectPoolType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagedObjectPoolBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListenerFactory;

/**
 * {@link ManagedObjectPoolNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolNodeImpl implements ManagedObjectPoolNode {

	/**
	 * Name of this {@link ManagedObjectPoolNode}.
	 */
	private final String managedObjectPoolName;

	/**
	 * Containing {@link SectionNode}. <code>null</code> if contained in the
	 * {@link Office} or {@link OfficeFloor}.
	 */
	private final SectionNode containingSectionNode;

	/**
	 * Containing {@link OfficeNode}. <code>null</code> if contained in the
	 * {@link OfficeFloor}.
	 */
	private final OfficeNode containingOfficeNode;

	/**
	 * Containing {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode containingOfficeFloorNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link PropertyList} to configure the {@link ManagedObjectPoolSource}.
	 */
	private final PropertyList properties;

	/**
	 * {@link InitialisedState}.
	 */
	private InitialisedState state = null;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {

		/**
		 * {@link Class} name of the {@link ManagedObjectPoolSource}.
		 */
		private final String managedObjectPoolSourceClassName;

		/**
		 * Optional {@link ManagedObjectPoolSource}. May be <code>null</code>.
		 */
		private final ManagedObjectPoolSource managedObjectPoolSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectPoolSourceClassName {@link Class} name of the
		 *                                         {@link ManagedObjectPoolSource}.
		 * @param managedObjectPoolSource          Optional
		 *                                         {@link ManagedObjectPoolSource}. May
		 *                                         be <code>null</code>.
		 */
		private InitialisedState(String managedObjectPoolSourceClassName,
				ManagedObjectPoolSource managedObjectPoolSource) {
			this.managedObjectPoolSourceClassName = managedObjectPoolSourceClassName;
			this.managedObjectPoolSource = managedObjectPoolSource;
		}
	}

	/**
	 * {@link ManagedObjectPoolSource} used to source this
	 * {@link ManagedObjectPoolNode}.
	 */
	private ManagedObjectPoolSource usedManagedObjectPoolSource = null;

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectPoolName     Name of this {@link ManagedObjectPoolNode}.
	 * @param containingSectionNode     Containing {@link SectionNode}.
	 *                                  <code>null</code> if contained in the
	 *                                  {@link Office} or {@link OfficeFloor}.
	 * @param containingOfficeNode      Containing {@link OfficeNode}.
	 *                                  <code>null</code> if contained in the
	 *                                  {@link OfficeFloor}.
	 * @param containingOfficeFloorNode Containing {@link OfficeFloorNode}.
	 * @param context                   {@link NodeContext}.
	 */
	public ManagedObjectPoolNodeImpl(String managedObjectPoolName, SectionNode containingSectionNode,
			OfficeNode containingOfficeNode, OfficeFloorNode containingOfficeFloorNode, NodeContext context) {
		this.managedObjectPoolName = managedObjectPoolName;
		this.containingSectionNode = containingSectionNode;
		this.containingOfficeNode = containingOfficeNode;
		this.containingOfficeFloorNode = containingOfficeFloorNode;
		this.context = context;

		// Create the properties
		this.properties = this.context.createPropertyList();
	}

	/**
	 * Obtains the qualified name for this {@link ManagedObjectPoolNode}.
	 * 
	 * @return Qualified name for this {@link ManagedObjectPoolNode}.
	 */
	private String getQualifiedManagedObjectPoolName() {
		return (this.containingSectionNode != null
				? this.containingSectionNode.getQualifiedName(this.managedObjectPoolName)
				: (this.containingOfficeNode != null
						? this.containingOfficeNode.getQualifiedName(this.managedObjectPoolName)
						: this.managedObjectPoolName));
	}

	/*
	 * ========================= Node =============================
	 */

	@Override
	public String getNodeName() {
		return this.managedObjectPoolName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null; // no location
	}

	@Override
	public Node getParentNode() {
		return (this.containingSectionNode != null ? this.containingSectionNode
				: (this.containingOfficeNode != null ? this.containingOfficeNode : this.containingOfficeFloorNode));
	}

	@Override
	public Node[] getChildNodes() {
		return new Node[0];
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String managedObjectPoolSourceClassName, ManagedObjectPoolSource managedObjectPoolSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(managedObjectPoolSourceClassName, managedObjectPoolSource));
	}

	/*
	 * ============== OfficeFloorManagedObjectPool ================
	 */

	@Override
	public String getOfficeFloorManagedObjectPoolName() {
		return this.managedObjectPoolName;
	}

	/*
	 * ================ OfficeManagedObjectPool ===================
	 */

	@Override
	public String getOfficeManagedObjectPoolName() {
		return this.managedObjectPoolName;
	}

	/*
	 * ================ SectionManagedObjectPool ==================
	 */

	@Override
	public String getSectionManagedObjectPoolName() {
		return this.managedObjectPoolName;
	}

	/**
	 * ================== PropertyConfigurable ====================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	/*
	 * ================= ManagedObjectPoolNode ====================
	 */

	@Override
	public ManagedObjectPoolType loadManagedObjectPoolType(boolean isLoadingType) {

		// Obtain the managed object pool source
		ManagedObjectPoolSource managedObjectPoolSource = this.state.managedObjectPoolSource;
		if (managedObjectPoolSource == null) {

			// Obtain the managed object pool source class
			Class<? extends ManagedObjectPoolSource> managedObjectPoolSourceClass = this.context
					.getManagedObjectPoolSourceClass(this.state.managedObjectPoolSourceClassName, this);
			if (managedObjectPoolSourceClass == null) {
				return null; // must obtain source class
			}

			// Obtain the managed object pool source
			managedObjectPoolSource = CompileUtil.newInstance(managedObjectPoolSourceClass,
					ManagedObjectPoolSource.class, this, this.context.getCompilerIssues());
			if (managedObjectPoolSource == null) {
				return null; // must obtain source
			}
		}

		// Keep track of managed object pool source
		this.usedManagedObjectPoolSource = managedObjectPoolSource;

		// Load and return the managed object pool type
		ManagedObjectPoolLoader loader = this.context.getManagedObjectPoolLoader(this, this.containingOfficeNode,
				isLoadingType);
		return loader.loadManagedObjectPoolType(managedObjectPoolSource, this.properties);
	}

	@Override
	public boolean sourceManagedObjectPool(CompileContext compileContext) {

		// Load the managed object pool type
		ManagedObjectPoolType poolType = compileContext.getOrLoadManagedObjectPoolType(this);
		if (poolType == null) {
			return false; // must load pool type
		}

		// As here, successful
		return true;
	}

	@Override
	public void buildManagedObjectPool(ManagedObjectBuilder<?> builder, ManagedObjectType<?> managedObjectType,
			CompileContext compileContext) {

		// Build the managed object pool type
		ManagedObjectPoolType poolType = compileContext.getOrLoadManagedObjectPoolType(this);
		if (poolType == null) {
			return; // must load pool type
		}

		// Register as possible MBean
		String qualifiedName = this.getQualifiedManagedObjectPoolName();
		compileContext.registerPossibleMBean(ManagedObjectPoolSource.class, qualifiedName,
				this.usedManagedObjectPoolSource);

		// Ensure able to pool managed objects from managed object source
		Class<?> pooledObjectType = poolType.getPooledObjectType();
		Class<?> objectType = managedObjectType.getObjectType();
		if (!pooledObjectType.isAssignableFrom(objectType)) {
			this.context.getCompilerIssues().addIssue(this, "Pooled object " + pooledObjectType.getName()
					+ " must be super (or same) type for ManagedObjectSource object " + objectType.getName());
			return; // must be able pool the object
		}

		// Build the managed object pool
		ManagedObjectPoolBuilder poolBuilder = builder.setManagedObjectPool(poolType.getManagedObjectPoolFactory());
		for (ThreadCompletionListenerFactory threadCompletionListenerFactoy : poolType
				.getThreadCompletionListenerFactories()) {
			poolBuilder.addThreadCompletionListener(threadCompletionListenerFactoy);
		}
	}

	/*
	 * ===================== LinkPoolNode =========================
	 */

	/**
	 * {@link LinkPoolNode}.
	 */
	private LinkPoolNode linkedPoolNode = null;

	@Override
	public boolean linkPoolNode(LinkPoolNode node) {
		return LinkUtil.linkPoolNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedPoolNode = link);
	}

	@Override
	public LinkPoolNode getLinkedPoolNode() {
		return this.linkedPoolNode;
	}

}
