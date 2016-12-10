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
package net.officefloor.model.generate.model;

import net.officefloor.model.generate.GraphNodeMetaData;

/**
 * Field meta-data.
 * 
 * @author Daniel Sagenschneider
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
	 * @param endField
	 *            End field.
	 * @param endList
	 *            End list.
	 */
	public FieldMetaData(String name, String type, String description,
			String endField, String endList) {
		super(name, type, description);
		this.endField = endField;
		this.endList = endList;
	}

	/**
	 * End point connect.
	 * 
	 * @return End point connect.
	 */
	public String getEndPointConnect() {
		if (this.endField != null) {
			return "set" + GraphNodeMetaData.camelCase(this.endField)
					+ "(this)";
		} else if (this.endList != null) {
			return "add" + GraphNodeMetaData.camelCase(this.endList) + "(this)";
		} else {
			// No connection
			return null;
		}
	}

	/**
	 * End point remove.
	 * 
	 * @return End point remove.
	 */
	public String getEndPointRemove() {
		if (this.endField != null) {
			return "set" + GraphNodeMetaData.camelCase(this.endField)
					+ "(null)";
		} else if (this.endList != null) {
			return "remove" + GraphNodeMetaData.camelCase(this.endList)
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
