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
 * List meta-data.
 * 
 * @author Daniel Sagenschneider
 */
public class ListMetaData extends AbstractPropertyMetaData {

	/**
	 * Default constructor.
	 */
	public ListMetaData() {
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
	public ListMetaData(String name, String type, String description) {
		super(name, type, description);
	}

	/**
	 * Plural name.
	 */
	private String plural;

	public String getPluralName() {
		if (plural == null) {
			return this.getCamelCaseName() + "s";
		} else {
			return GraphNodeMetaData.camelCase(plural);
		}
	}

	public void setPlural(String plural) {
		this.plural = plural;
	}
}
