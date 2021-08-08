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

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagedObjectTeamNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.ResponsibleTeamNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.section.OfficeSectionManagedObjectTeamType;
import net.officefloor.compile.spi.office.OfficeTeam;

/**
 * {@link ResponsibleTeamNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectTeamNodeImpl implements ManagedObjectTeamNode {

	/**
	 * Name of this {@link OfficeTeam}.
	 */
	private final String teamName;

	/**
	 * {@link ManagedObjectSourceNode} containing this
	 * {@link ManagedObjectTeamNode}.
	 */
	private final ManagedObjectSourceNode managedObjectSourceNode;

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
	 * @param teamName            Name of this {@link OfficeTeam}.
	 * @param managedObjectSource {@link ManagedObjectSourceNode} containing this
	 *                            {@link ManagedObjectTeamNode}.
	 * @param context             {@link NodeContext}.
	 */
	public ManagedObjectTeamNodeImpl(String teamName, ManagedObjectSourceNode managedObjectSource,
			NodeContext context) {
		this.teamName = teamName;
		this.managedObjectSourceNode = managedObjectSource;
		this.context = context;
	}

	/*
	 * ================== Node =========================
	 */

	@Override
	public String getNodeName() {
		return this.teamName;
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
		return this.managedObjectSourceNode;
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
	 * ================ ManagedObjectTeamNode =======================
	 */

	@Override
	public OfficeSectionManagedObjectTeamType loadOfficeSectionManagedObjectTeamType(CompileContext compileContext) {
		// TODO implement
		// ManagedObjectTeamNode.loadOfficeSectionManagedObjectTeamType
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectTeamNode.loadOfficeSectionManagedObjectTeamType");

	}

	/*
	 * ================== ManagedObjectTeam =========================
	 */

	@Override
	public String getManagedObjectTeamName() {
		return this.teamName;
	}

	@Override
	public void addTypeQualification(String qualifier, String type) {
		// TODO implement
		// ManagedObjectTeamNode.addTypeQualification
		throw new UnsupportedOperationException("TODO implement ManagedObjectTeamNode.addTypeQualification");
	}

	/*
	 * ============= AugmentedManagedObjectNode ====================
	 */

	@Override
	public boolean isLinked() {
		return (this.linkedTeamNode != null);
	}

	/*
	 * ================== LinkTeamNode ============================
	 */

	/**
	 * Linked {@link LinkTeamNode}.
	 */
	private LinkTeamNode linkedTeamNode = null;

	@Override
	public boolean linkTeamNode(LinkTeamNode node) {
		return LinkUtil.linkTeamNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedTeamNode = link);
	}

	@Override
	public LinkTeamNode getLinkedTeamNode() {
		return this.linkedTeamNode;
	}

}
