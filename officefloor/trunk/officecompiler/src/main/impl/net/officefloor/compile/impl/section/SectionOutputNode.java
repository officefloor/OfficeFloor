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

import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSectionOutput;

/**
 * {@link SectionOutput} node.
 * 
 * @author Daniel
 */
public class SectionOutputNode implements SectionOutputType, SectionOutput,
		SubSectionOutput {

	/**
	 * Name of the {@link SectionOutputType}.
	 */
	private final String outputName;

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
	 * Initiate not initialised.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutputType}.
	 */
	public SectionOutputNode(String outputName) {
		this.outputName = outputName;
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
	 */
	public SectionOutputNode(String outputName, String argumentType,
			boolean isEscalationOnly) {
		this.outputName = outputName;
		this.initialise(argumentType, isEscalationOnly);
	}

	/**
	 * Indicates if this {@link SectionOutputType} has been initialised.
	 * 
	 * @return <code>true</code> if initialised.
	 */
	public boolean isInitialised() {
		return this.isInitialised;
	}

	/**
	 * Initialises this {@link SectionOutputType}.
	 * 
	 * @param argumentType
	 *            Argument type.
	 * @param isEscalationOnly
	 *            Flag indicating if escalation only.
	 */
	public void initialise(String argumentType, boolean isEscalationOnly) {
		this.argumentType = argumentType;
		this.isEscalationOnly = isEscalationOnly;
		this.isInitialised = true;
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

}