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

import java.util.LinkedList;
import java.util.List;

/**
 * Test configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateSectionConfig {

	/**
	 * Name of the section.
	 */
	public String name;

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@link TemplateSectionContentConfig} instances.
	 */
	public List<TemplateSectionContentConfig> contents = new LinkedList<TemplateSectionContentConfig>();

	public void addContent(TemplateSectionContentConfig content) {
		this.contents.add(content);
	}
}
