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

import net.officefloor.compile.impl.section.OfficeSectionObjectTypeImpl;
import net.officefloor.compile.impl.section.SectionObjectTypeImpl;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.type.TypeContext;

/**
 * {@link SectionObjectNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionObjectNodeImpl implements SectionObjectNode {

	/**
	 * Name of the {@link SectionObjectType}.
	 */
	private final String objectName;

	/**
	 * {@link SectionNode} containing this {@link SectionObjectNode}.
	 */
	private final SectionNode section;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link InitialisedState}.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {

		/**
		 * Object type.
		 */
		private final String objectType;

		/**
		 * Instantiate.
		 * 
		 * @param objectType
		 *            Object type.
		 */
		public InitialisedState(String objectType) {
			this.objectType = objectType;
		}
	}

	/**
	 * Type qualifier.
	 */
	private String typeQualifier = null;

	/**
	 * Instantiate.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObject}.
	 * @param section
	 *            {@link SectionNode} containing this {@link SectionObjectNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionObjectNodeImpl(String objectName, SectionNode section,
			NodeContext context) {
		this.objectName = objectName;
		this.section = section;
		this.context = context;
	}

	/*
	 * ==================== Node =========================
	 */

	@Override
	public String getNodeName() {
		return this.objectName;
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

	/*
	 * ==================== SectionObject =========================
	 */

	@Override
	public void setTypeQualifier(String qualifier) {
		this.typeQualifier = qualifier;
	}

	/*
	 * ================== SectionObjectNode ========================
	 */

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public SectionObjectNode initialise(String objectType) {

		// Ensure not already initialise
		if (this.isInitialised()) {
			throw new IllegalStateException("SectionObjectNode "
					+ this.objectName + " already initialised");
		}

		// Initialise
		this.state = new InitialisedState(objectType);
		return this;
	}

	@Override
	public SectionNode getSectionNode() {
		return this.section;
	}

	@Override
	public SectionObjectType loadSectionObjectType(TypeContext typeContext) {
		return new SectionObjectTypeImpl(this.objectName,
				this.state.objectType, this.typeQualifier);
	}

	@Override
	public OfficeSectionObjectType loadOfficeSectionObjectType(
			TypeContext typeContext) {
		return new OfficeSectionObjectTypeImpl(this.objectName,
				this.state.objectType, this.typeQualifier);
	}

	/*
	 * =============== SectionObject ===========================
	 */

	@Override
	public String getSectionObjectName() {
		return this.objectName;
	}

	/*
	 * =============== SubSectionObject ============================
	 */

	@Override
	public String getSubSectionObjectName() {
		return this.objectName;
	}

	/*
	 * ==================== OfficeSectionObject =========================
	 */

	@Override
	public String getOfficeSectionObjectName() {
		return this.objectName;
	}

	/*
	 * =============== LinkObjectNode ==============================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectNode;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		return LinkUtil.linkObjectNode(this, node,
				this.context.getCompilerIssues(),
				(link) -> this.linkedObjectNode = link);
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}