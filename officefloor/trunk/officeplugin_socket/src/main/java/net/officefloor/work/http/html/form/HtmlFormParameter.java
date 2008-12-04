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
package net.officefloor.work.http.html.form;

/**
 * {@link HtmlForm} parameter.
 * 
 * @author Daniel
 */
public class HtmlFormParameter {

	/**
	 * Name of the parameter.
	 */
	private final String name;

	/**
	 * Value of the parameter.
	 */
	private final String value;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of the parameter.
	 * @param value
	 *            Value of the parameter.
	 */
	public HtmlFormParameter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Obtains the name of the parameter.
	 * 
	 * @return Name of the parameter.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the value of the parameter.
	 * 
	 * @return Value of the parameter.
	 */
	public String getValue() {
		return this.value;
	}
}
