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

import net.officefloor.compile.section.SectionObjectType;

/**
 * {@link SectionObjectType} implementation.
 * 
 * @author Daniel
 */
public class SectionObjectTypeImpl implements SectionObjectType {

	/**
	 * Name of the {@link SectionObjectType}.
	 */
	private String objectName;

	/**
	 * Object type.
	 */
	private String objectType;

	/**
	 * Initiate.
	 * 
	 * @param objectName
	 *            Name of the {@link SectionObjectType}.
	 * @param objectType
	 *            Object type.
	 */
	public SectionObjectTypeImpl(String objectName, String objectType) {
		this.objectName = objectName;
		this.objectType = objectType;
	}

	/*
	 * =============== SectionObjectType ===========================
	 */

	@Override
	public String getObjectName() {
		return this.objectName;
	}

	@Override
	public String getObjectType() {
		return this.objectType;
	}

}