/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
