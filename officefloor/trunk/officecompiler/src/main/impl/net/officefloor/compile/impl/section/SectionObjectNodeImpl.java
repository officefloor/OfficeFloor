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

import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.spi.section.SectionObject;

/**
 * {@link SectionObjectNode} implementation.
 * 
 * @author Daniel
 */
public class SectionObjectNodeImpl implements SectionObjectNode {

	/**
	 * Name of the {@link SectionObjectType}.
	 */
	private final String objectName;

	/**
	 * Indicates if this {@link SectionObjectType} is initialised.
	 */
	private boolean isInitialised = false;

	/**
	 * Object type.
	 */
	private String objectType;

	/**
	 * Initiate not initialised.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObject}.
	 */
	public SectionObjectNodeImpl(String objectName) {
		this.objectName = objectName;
	}

	/**
	 * Initiate initialised.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObjectType}.
	 * @param objectType
	 *            Object type.
	 */
	public SectionObjectNodeImpl(String objectName, String objectType) {
		this.objectName = objectName;
		this.initialise(objectType);
	}

	/*
	 * ================== SectionObjectNode ========================
	 */

	@Override
	public boolean isInitialised() {
		return this.isInitialised;
	}

	@Override
	public void initialise(String objectType) {
		this.objectType = objectType;
		this.isInitialised = true;
	}

	/*
	 * =============== SectionObjectType ===========================
	 */

	@Override
	public String getSectionObjectName() {
		return this.objectName;
	}

	@Override
	public String getObjectType() {
		return this.objectType;
	}

	/*
	 * =============== SubSectionObject ============================
	 */

	@Override
	public String getSubSectionObjectName() {
		return this.objectName;
	}

}