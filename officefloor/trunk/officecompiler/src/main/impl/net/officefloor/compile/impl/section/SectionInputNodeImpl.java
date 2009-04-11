/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.section;

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.SectionInputNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSectionInput;

/**
 * {@link SectionInputNode} node.
 * 
 * @author Daniel
 */
public class SectionInputNodeImpl implements SectionInputNode {

	/**
	 * Name of the {@link SectionInputType}.
	 */
	private final String inputName;

	/**
	 * Location of the {@link OfficeSection} containing this
	 * {@link SectionInputNode}.
	 */
	private final String sectionLocation;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

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
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SectionInputNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public SectionInputNodeImpl(String inputName, String sectionLocation,
			CompilerIssues issues) {
		this.inputName = inputName;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/**
	 * Initiate initialised.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInputType}.
	 * @param parameterType
	 *            Parameter type.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SectionInputNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public SectionInputNodeImpl(String inputName, String parameterType,
			String sectionLocation, CompilerIssues issues) {
		this.inputName = inputName;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
		this.initialise(parameterType);
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
			this.issues.addIssue(LocationType.SECTION, this.sectionLocation,
					null, null, "Input " + this.inputName
							+ " linked more than once");
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