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
package net.officefloor.plugin.web.template.parse;

/**
 * {@link ParsedTemplateSectionContent} that references a property of the bean
 * to render.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyParsedTemplateSectionContent implements ParsedTemplateSectionContent {

	/**
	 * Property name.
	 */
	private final String propertyName;

	/**
	 * Initiate.
	 * 
	 * @param propertyName
	 *            Property name.
	 */
	public PropertyParsedTemplateSectionContent(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * Obtains the name of the property to render.
	 * 
	 * @return Name of the property to render.
	 */
	public String getPropertyName() {
		return this.propertyName;
	}

}