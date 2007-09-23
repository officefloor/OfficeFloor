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
package net.officefloor.model.generate.model;

import net.officefloor.model.generate.GenericMetaData;

/**
 * Field meta-data.
 * 
 * @author Daniel
 */
public class FieldMetaData extends AbstractPropertyMetaData {

	/**
	 * Default constructor.
	 */
	public FieldMetaData() {
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param name
	 *            Name.
	 * @param type
	 *            Type.
	 * @param description
	 *            Description.
	 */
	public FieldMetaData(String name, String type, String description,
			String endField, String endList) {
		super(name, type, description);
		this.endField = endField;
		this.endList = endList;
	}

	/**
	 * End point connect.
	 */
	public String getEndPointConnect() {
		if (this.endField != null) {
			return "set" + GenericMetaData.camelCase(this.endField) + "(this)";
		} else if (this.endList != null) {
			return "add" + GenericMetaData.camelCase(this.endList) + "(this)";
		} else {
			// No connection
			return null;
		}
	}

	/**
	 * End point remove.
	 */
	public String getEndPointRemove() {
		if (this.endField != null) {
			return "set" + GenericMetaData.camelCase(this.endField) + "(null)";
		} else if (this.endList != null) {
			return "remove" + GenericMetaData.camelCase(this.endList)
					+ "(this)";
		} else {
			// No connection
			return null;
		}
	}

	/**
	 * End field.
	 */
	private String endField;

	public String getEndField() {
		return this.endField;
	}

	public void setEndField(String endField) {
		this.endField = endField;
	}

	/**
	 * End list.
	 */
	private String endList;

	public String getEndList() {
		return this.endList;
	}

	public void setEndList(String endList) {
		this.endList = endList;
	}

}
