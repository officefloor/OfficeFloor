/*-
 * #%L
 * Model Generator
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
