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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.impl.office.OfficeTeamTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeTeam;

/**
 * {@link OfficeTeamNode} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeTeamNodeImpl implements OfficeTeamNode {

	/**
	 * {@link OfficeTeam} name.
	 */
	private final String teamName;

	/**
	 * Parent {@link OfficeNode}.
	 */
	private final OfficeNode office;

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
	 * {@link TypeQualification} instances for this {@link ManagedObjectNode}.
	 */
	private final List<TypeQualification> typeQualifications = new LinkedList<TypeQualification>();

	/**
	 * Instantiate.
	 * 
	 * @param teamName
	 *            {@link OfficeTeam} name.
	 * @param office
	 *            Parent {@link OfficeNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeTeamNodeImpl(String teamName, OfficeNode office, NodeContext context) {
		this.teamName = teamName;
		this.office = office;
		this.context = context;
	}

	/*
	 * ====================== Node ===========================
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
		return this.office;
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
	 * ====================== OfficeTeam ===========================
	 */

	@Override
	public String getOfficeTeamName() {
		return this.teamName;
	}

	@Override
	public void addTypeQualification(String qualifier, String type) {
		this.typeQualifications.add(new TypeQualificationImpl(qualifier, type));
	}

	/*
	 * ==================== OfficeTeamNode =========================
	 */

	@Override
	public TypeQualification[] getTypeQualifications() {
		return this.typeQualifications.stream().toArray(TypeQualification[]::new);
	}

	@Override
	public OfficeTeamType loadOfficeTeamType(CompileContext compileContext) {

		// Ensure have name
		if (CompileUtil.isBlank(this.teamName)) {
			this.context.getCompilerIssues().addIssue(this, "Null name for " + TYPE);
			return null; // must have name
		}

		// Create and return type
		return new OfficeTeamTypeImpl(this.teamName, this.getTypeQualifications());
	}

	/*
	 * ====================== LinkTeamNode ===========================
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
