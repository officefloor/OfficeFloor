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

/**
 * {@link SectionOutputType} implementation.
 * 
 * @author Daniel
 */
public class SectionOutputImpl implements SectionOutputType, SectionOutput {

	/**
	 * Name of the {@link SectionOutputType}.
	 */
	private final String outputName;

	/**
	 * Argument type.
	 */
	private final String argumentType;

	/**
	 * Flag indicating if escalation only.
	 */
	private final boolean isEscalationOnly;

	/**
	 * Initiate.
	 * 
	 * @param outputName
	 *            Name of the {@link SectionOutputType}.
	 * @param argumentType
	 *            Argument type.
	 * @param isEscalationOnly
	 *            Flag indicating if escalation only.
	 */
	public SectionOutputImpl(String outputName, String argumentType,
			boolean isEscalationOnly) {
		this.outputName = outputName;
		this.argumentType = argumentType;
		this.isEscalationOnly = isEscalationOnly;
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

}