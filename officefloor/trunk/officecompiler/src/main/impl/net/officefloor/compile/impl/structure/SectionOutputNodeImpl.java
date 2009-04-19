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
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.SectionOutputNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link SectionOutputNode} implementation.
 * 
 * @author Daniel
 */
public class SectionOutputNodeImpl implements SectionOutputNode {

	/**
	 * Name of the {@link SectionOutputType}.
	 */
	private final String outputName;

	/**
	 * Location of the {@link OfficeSection} containing this
	 * {@link SubSectionOutput}.
	 */
	private final String sectionLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Indicates if this {@link SectionOutputType} is initialised.
	 */
	private boolean isInitialised = false;

	/**
	 * Argument type.
	 */
	private String argumentType;

	/**
	 * Flag indicating if escalation only.
	 */
	private boolean isEscalationOnly;

	/**
	 * Flag indicating if within {@link Office} context.
	 */
	private boolean isInOfficeContext = false;

	/**
	 * Location of the {@link Office}.
	 */
	private String officeLocation;

	/**
	 * Initiate not initialised.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutputType}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SubSectionOutput}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionOutputNodeImpl(String outputName, String sectionLocation,
			NodeContext context) {
		this.outputName = outputName;
		this.sectionLocation = sectionLocation;
		this.context = context;
	}

	/**
	 * Initiate initialised.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutputType}.
	 * @param argumentType
	 *            Argument type.
	 * @param isEscalationOnly
	 *            Flag indicating if escalation only.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SubSectionOutput}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public SectionOutputNodeImpl(String outputName, String argumentType,
			boolean isEscalationOnly, String sectionLocation,
			NodeContext context) {
		this.outputName = outputName;
		this.sectionLocation = sectionLocation;
		this.context = context;
		this.initialise(argumentType, isEscalationOnly);
	}

	/*
	 * ================== SectionOutputNode =======================
	 */

	@Override
	public boolean isInitialised() {
		return this.isInitialised;
	}

	@Override
	public void initialise(String argumentType, boolean isEscalationOnly) {
		this.argumentType = argumentType;
		this.isEscalationOnly = isEscalationOnly;
		this.isInitialised = true;
	}

	@Override
	public void addOfficeContext(String officeLocation) {
		this.officeLocation = officeLocation;
		this.isInOfficeContext = true;
	}

	/*
	 * ================ SectionOutputType =========================
	 */

	@Override
	public String getSectionOutputName() {
		return this.outputName;
	}

	@Override
	public String getArgumentType() {
		return this.argumentType;
	}

	@Override
	public boolean isEscalationOnly() {
		return this.isEscalationOnly;
	}

	/*
	 * ================ SubSectionOutput ===========================
	 */

	@Override
	public String getSubSectionOutputName() {
		return this.outputName;
	}

	/*
	 * =================== OfficeSectionOutput ======================
	 */

	@Override
	public String getOfficeSectionOutputName() {
		return this.outputName;
	}

	/*
	 * ==================== LinkFlowNode ===========================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {

		// Ensure not already linked
		if (this.linkedFlowNode != null) {
			if (this.isInOfficeContext) {
				// Office section output
				this.context.getCompilerIssues().addIssue(
						LocationType.OFFICE,
						this.officeLocation,
						null,
						null,
						"Office section output " + this.outputName
								+ " linked more than once");
			} else {
				// Sub section output
				this.context.getCompilerIssues().addIssue(
						LocationType.SECTION,
						this.sectionLocation,
						null,
						null,
						"Sub section output " + this.outputName
								+ " linked more than once");
			}
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