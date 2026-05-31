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
 * Abstract property meta-data.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPropertyMetaData {

	/**
	 * Default constructor.
	 */
	public AbstractPropertyMetaData() {
	}

	/**
	 * Convenience constructor.
	 * 
	 * @param name        Name.
	 * @param type        Type.
	 * @param description Description.
	 */
	public AbstractPropertyMetaData(String name, String type, String description) {
		this.name = name;
		this.type = type;
		this.description = description;
	}

	/**
	 * Obtains the capitalised name.
	 * 
	 * @return Prpoerty name capitalised.
	 */
	public String getCapitalisedName() {
		return GraphNodeMetaData.capitalise(this.name);
	}

	/**
	 * Obtains the camel case name.
	 * 
	 * @return Property name in camel case.
	 */
	public String getCamelCaseName() {
		return GraphNodeMetaData.camelCase(this.name);
	}

	/**
	 * Obtains the property name.
	 * 
	 * @return Property name.
	 */
	public String getPropertyName() {
		return GraphNodeMetaData.propertyCase(this.name);
	}

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Obtains the name.
	 *
	 * @return Name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name Name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Type.
	 */
	private String type;

	/**
	 * Obtains the type.
	 *
	 * @return Type.
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type Type.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Flag indicating to cascade remove.
	 */
	private String cascadeRemove;

	/**
	 * Obtains the cascade remove flag.
	 *
	 * @return Cascade remove flag.
	 */
	public String getCascadeRemove() {
		return this.cascadeRemove;
	}

	/**
	 * Sets the cascade remove flag.
	 *
	 * @param cascadeRemove Cascade remove flag.
	 */
	public void setCascadeRemove(String cascadeRemove) {
		this.cascadeRemove = cascadeRemove;
	}

	/**
	 * Indicates if cascade remove.
	 *
	 * @return <code>true</code> if cascade remove.
	 */
	public boolean isCascadeRemove() {
		return Boolean.valueOf(this.cascadeRemove).booleanValue();
	}

	/**
	 * Description.
	 */
	private String description;

	/**
	 * Obtains the description.
	 *
	 * @return Description.
	 */
	public String getDescription() {
		if (this.description == null) {
			// Construct description from name
			return GraphNodeMetaData.titleCase(this.name) + ".";
		} else {
			return this.description;
		}
	}

	/**
	 * Sets the description.
	 *
	 * @param description Description.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
