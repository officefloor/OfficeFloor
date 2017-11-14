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
 * Child {@link TableRowBean}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChildTableRowBean extends TableRowBean {

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
	public ChildTableRowBean(String name, String description,
			PropertyBean property) {
		super(name, description, property);
	}

}
