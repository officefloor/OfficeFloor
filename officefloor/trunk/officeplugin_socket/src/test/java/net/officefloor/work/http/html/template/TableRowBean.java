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
package net.officefloor.work.http.html.template;

/**
 * Table row bean for listing of beans in template.
 * 
 * @author Daniel
 */
public class TableRowBean {

	/**
	 * Name.
	 */
	private String name;

	/**
	 * Description.
	 */
	private String description;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param description
	 *            Description.
	 */
	public TableRowBean(String name, String description) {
		this.name = name;
		this.description = description;
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
}
