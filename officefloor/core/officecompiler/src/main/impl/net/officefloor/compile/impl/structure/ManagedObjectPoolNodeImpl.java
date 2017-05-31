/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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

import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkPoolNode;
import net.officefloor.compile.internal.structure.ManagedObjectPoolNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.pool.ManagedObjectPoolType;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

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
		 * @param managedObjectPoolSourceClassName
		 *            {@link Class} name of the {@link ManagedObjectPoolSource}.
		 * @param managedObjectPoolSource
		 *            Optional {@link ManagedObjectPoolSource}. May be
		 *            <code>null</code>.
		 */
		private InitialisedState(String managedObjectPoolSourceClassName,
				ManagedObjectPoolSource managedObjectPoolSource) {
			this.managedObjectPoolSourceClassName = managedObjectPoolSourceClassName;
			this.managedObjectPoolSource = managedObjectPoolSource;
		}
	}

	/**
	 * Instantiate.
	 * 
	 * @param managedObjectPoolName
	 *            Name of this {@link ManagedObjectPoolNode}.
	 * @param containingSectionNode
	 *            Containing {@link SectionNode}. <code>null</code> if contained
	 *            in the {@link Office} or {@link OfficeFloor}.
	 * @param containingOfficeNode
	 *            Containing {@link OfficeNode}. <code>null</code> if contained
	 *            in the {@link OfficeFloor}.
	 * @param containingOfficeFloorNode
	 *            Containing {@link OfficeFloorNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectPoolNodeImpl(String managedObjectPoolName, SectionNode containingSectionNode,
			OfficeNode containingOfficeNode, OfficeFloorNode containingOfficeFloorNode, NodeContext context) {
		this.managedObjectPoolName = managedObjectPoolName;
		this.containingSectionNode = containingSectionNode;
		this.containingOfficeNode = containingOfficeNode;
		this.containingOfficeFloorNode = containingOfficeFloorNode;
		this.context = context;
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
		// TODO Auto-generated method stub

	}

	/*
	 * ================= ManagedObjectPoolNode ====================
	 */

	@Override
	public boolean sourceManagedObjectPool(CompileContext compileContext) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ManagedObjectPoolType loadManagedObjectPoolType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void buildManagedObjectPool(OfficeFloorBuilder builder, OfficeNode managingOffice,
			OfficeBuilder managingOfficeBuilder, OfficeBindings officeBindings, CompileContext compileContext) {
		// TODO Auto-generated method stub

	}

	/*
	 * ===================== LinkPoolNode =========================
	 */

	@Override
	public boolean linkPoolNode(LinkPoolNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LinkPoolNode getLinkedPoolNode() {
		// TODO Auto-generated method stub
		return null;
	}

}