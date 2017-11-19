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
package net.officefloor.web.template.parse;

/**
 * {@link ParsedTemplateSectionContent} that identifies a link.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkParsedTemplateSectionContent implements ParsedTemplateSectionContent {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 */
	public LinkParsedTemplateSectionContent(String name) {
		this.name = name;
	}

	/**
	 * Obtains the name for the link.
	 * 
	 * @return Name for the link.
	 */
	public String getName() {
		return this.name;
	}

}