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

import net.officefloor.compile.impl.object.DependentObjectTypeImpl;
import net.officefloor.compile.impl.section.OfficeSectionObjectTypeImpl;
import net.officefloor.compile.impl.section.SectionObjectTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.section.OfficeSectionObjectType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SectionObject;

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
		 * @param objectType Object type.
		 */
		public InitialisedState(String objectType) {
			this.objectType = objectType;
		}
	}

	/**
	 * Annotations.
	 */
	private final List<Object> annotations = new LinkedList<>();

	/**
	 * Type qualifier.
	 */
	private String typeQualifier = null;

	/**
	 * Instantiate.
	 * 
	 * @param objectName Name of the {@link SectionObject}.
	 * @param section    {@link SectionNode} containing this
	 *                   {@link SectionObjectNode}.
	 * @param context    {@link NodeContext}.
	 */
	public SectionObjectNodeImpl(String objectName, SectionNode section, NodeContext context) {
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

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes();
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String objectType) {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState(objectType));
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
	public SectionNode getSectionNode() {
		return this.section;
	}

	@Override
	public SectionObjectType loadSectionObjectType(CompileContext compileContext) {

		// Ensure have name and type
		if (CompileUtil.isBlank(this.objectName)) {
			this.context.getCompilerIssues().addIssue(this, "Null name for " + TYPE);
			return null; // must have name for object
		}
		if (this.state == null) {
			this.context.getCompilerIssues().addIssue(this, "Not initialised");
		}
		if (CompileUtil.isBlank(this.state.objectType)) {
			this.context.getCompilerIssues().addIssue(this,
					"Null type for " + TYPE + " (name=" + this.objectName + ")");
			return null; // must have types for objects
		}

		// Create and return the type
		Object[] annotations = this.annotations.toArray(new Object[this.annotations.size()]);
		return new SectionObjectTypeImpl(this.objectName, this.state.objectType, this.typeQualifier, annotations);
	}

	@Override
	public OfficeSectionObjectType loadOfficeSectionObjectType(CompileContext compileContext) {
		return new OfficeSectionObjectTypeImpl(this.objectName, this.state.objectType, this.typeQualifier);
	}

	/*
	 * =============== DependentObjectNode ===========================
	 */

	@Override
	public DependentObjectType loadDependentObjectType(CompileContext compileContext) {
		TypeQualification[] typeQualifications = new TypeQualification[] {
				new TypeQualificationImpl(this.typeQualifier, this.state.objectType) };
		return new DependentObjectTypeImpl(this.objectName, typeQualifications, new ObjectDependencyType[0]);
	}

	/*
	 * =============== SectionObject ===========================
	 */

	@Override
	public String getSectionObjectName() {
		return this.objectName;
	}

	@Override
	public void addAnnotation(Object annotation) {
		this.annotations.add(annotation);
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
	public OfficeSection getOfficeSection() {
		return this.section;
	}

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
		return LinkUtil.linkObjectNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedObjectNode = link);
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}
