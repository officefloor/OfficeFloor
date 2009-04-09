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

import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.spi.section.SectionInput;

/**
 * {@link SectionInput} node.
 * 
 * @author Daniel
 */
public class SectionInputNode implements SectionInputType, SectionInput {

	/**
	 * Name of the {@link SectionInputType}.
	 */
	private final String inputName;

	/**
	 * Parameter type.
	 */
	private final String parameterType;

	/**
	 * Initiate.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInputType}.
	 * @param parameterType
	 *            Parameter type.
	 */
	public SectionInputNode(String inputName, String parameterType) {
		this.inputName = inputName;
		this.parameterType = parameterType;
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

}