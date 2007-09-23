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
 * List meta-data.
 * 
 * @author Daniel
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
			return GenericMetaData.camelCase(plural);
		}
	}

	public void setPlural(String plural) {
		this.plural = plural;
	}
}
