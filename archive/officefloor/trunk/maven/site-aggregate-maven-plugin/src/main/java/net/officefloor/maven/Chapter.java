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

import java.util.LinkedList;
import java.util.List;

/**
 * Chapter of the book.
 * 
 * @author Daniel Sagenschneider
 */
public class Chapter {

	/**
	 * Id.
	 */
	public final String id;

	/**
	 * Title.
	 */
	public final String title;

	/**
	 * Sections.
	 */
	public final List<Section> sections = new LinkedList<Section>();

	/**
	 * Initiate.
	 * 
	 * @param id
	 *            Id.
	 * @param title
	 *            Title.
	 */
	public Chapter(String id, String title) {
		this.id = id;
		this.title = title;
	}

}