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
package net.officefloor.maven;

/**
 * Section of a {@link Chapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class Section {

	/**
	 * Id.
	 */
	public final String id;

	/**
	 * Name.
	 */
	public final String name;

	/**
	 * Reference.
	 */
	public final String reference;

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id.
	 * @param name
	 *            Name.
	 * @param reference
	 *            Reference.
	 */
	public Section(String id, String name, String reference) {
		this.id = id;
		this.name = name;
		this.reference = reference;
	}

}