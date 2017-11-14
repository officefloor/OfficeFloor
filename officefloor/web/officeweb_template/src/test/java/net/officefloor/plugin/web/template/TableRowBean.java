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
package net.officefloor.plugin.web.template;

/**
 * Table row bean for listing of beans in template.
 * 
 * @author Daniel Sagenschneider
 */
public class TableRowBean {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Description.
	 */
	private final String description;

	/**
	 * {@link PropertyBean}.
	 */
	private final PropertyBean property;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param description
	 *            Description.
	 * @param property
	 *            {@link PropertyBean}.
	 */
	public TableRowBean(String name, String description, PropertyBean property) {
		this.name = name;
		this.description = description;
		this.property = property;
	}

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the description.
	 * 
	 * @return Description.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Obtains the {@link PropertyBean}.
	 * 
	 * @return {@link PropertyBean}.
	 */
	public PropertyBean getProperty() {
		return this.property;
	}
}