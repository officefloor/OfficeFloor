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

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSectionInput;

/**
 * {@link SectionInputNode} node.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionInputNodeImpl implements SectionInputNode {

	/**
	 * Name of the {@link SectionInputType}.
	 */
	private final String inputName;

	/**
	 * {@link SectionNode} containing this {@link SectionInputNode}.
	 */
	private final SectionNode section;

	/**
	 * Location of the {@link OfficeSection} containing this
	 * {@link SectionInputNode}.
	 */
	private final String sectionLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Indicates if this {@link SectionInputType} is initialised.
	 */
	private boolean isInitialised = false;

	/**
	 * Parameter type.
	 */
	private String parameterType;

	/**
	 * Initiate not initialised.
	 * 
	 * @param inputName
	 *            Name of the {@link SubSectionInput} (which is the name of the
	 *            {@link SectionInputType}).
	 * @param section
	 *            {@link SectionNode} containing this {@link SectionInputNode}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SectionInputNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionInputNodeImpl(String inputName, SectionNode section,
			String sectionLocation, NodeContext context) {
		this.inputName = inputName;
		this.section = section;
		this.sectionLocation = sectionLocation;
		this.context = context;
	}

	/**
	 * Initiate initialised.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInputType}.
	 * @param section
	 *            {@link SectionNode} containing this {@link SectionInputNode}.
	 * @param parameterType
	 *            Parameter type.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SectionInputNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionInputNodeImpl(String inputName, SectionNode section,
			String parameterType, String sectionLocation, NodeContext context) {
		this.inputName = inputName;
		this.section = section;
		this.sectionLocation = sectionLocation;
		this.context = context;
		this.initialise(parameterType);
	}

	/*
	 * =================== OfficeInputType ===========================
	 */

	@Override
	public String getOfficeSectionName() {
		return this.section.getOfficeSectionName();
	}

	/*
	 * ===================== SectionInputNode ===========================
	 */

	@Override
	public boolean isInitialised() {
		return this.isInitialised;
	}

	@Override
	public void initialise(String parameterType) {
		this.parameterType = parameterType;
		this.isInitialised = true;
	}

	/*
	 * ================= SectionInputType =========================
	 */

	@Override
	public String getSectionInputName() {
		return this.inputName;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

	/*
	 * =================== SubSectionInput ========================
	 */

	@Override
	public String getSubSectionInputName() {
		return this.inputName;
	}

	/*
	 * ===================== OfficeSectionInput =====================
	 */

	@Override
	public String getOfficeSectionInputName() {
		return this.inputName;
	}

	/*
	 * ====================== DeployedOfficeInput ====================
	 */

	@Override
	public String getDeployedOfficeInputName() {
		return this.inputName;
	}

	/*
	 * =================== LinkFlowNode ============================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode = null;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {

		// Ensure not already linked
		if (this.linkedFlowNode != null) {
			this.context.getCompilerIssues().addIssue(LocationType.SECTION,
					this.sectionLocation, null, null,
					"Input " + this.inputName + " linked more than once");
			return false; // already linked
		}

		// Link
		this.linkedFlowNode = node;
		return true;
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

}