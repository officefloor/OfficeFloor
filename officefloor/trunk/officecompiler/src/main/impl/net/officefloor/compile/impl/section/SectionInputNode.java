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
import net.officefloor.compile.spi.section.SubSectionInput;

/**
 * {@link SectionInput} node.
 * 
 * @author Daniel
 */
public class SectionInputNode implements SectionInputType, SectionInput,
		SubSectionInput {

	/**
	 * Name of the {@link SectionInputType}.
	 */
	private final String inputName;

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
	 */
	public SectionInputNode(String inputName) {
		this.inputName = inputName;
	}

	/**
	 * Initiate initialised.
	 * 
	 * @param inputName
	 *            Name of the {@link SectionInputType}.
	 * @param parameterType
	 *            Parameter type.
	 */
	public SectionInputNode(String inputName, String parameterType) {
		this.inputName = inputName;
		this.initialise(parameterType);
	}

	/**
	 * Indicates if this {@link SectionInputType} has been initialised.
	 * 
	 * @return <code>true</code> if initialised.
	 */
	public boolean isInitialised() {
		return this.isInitialised;
	}

	/**
	 * Initialises this {@link SectionInputType}.
	 * 
	 * @param parameterType
	 *            Parameter type.
	 */
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

}