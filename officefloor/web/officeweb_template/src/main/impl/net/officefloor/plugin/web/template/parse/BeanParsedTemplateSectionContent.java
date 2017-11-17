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

import net.officefloor.plugin.web.template.parse.BeanParsedTemplateSectionContent;
import net.officefloor.plugin.web.template.parse.ParsedTemplateSectionContent;

/**
 * {@link ParsedTemplateSectionContent} that references a bean to use.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanParsedTemplateSectionContent implements ParsedTemplateSectionContent {

	/**
	 * Property name to obtain the bean.
	 */
	private final String beanName;

	/**
	 * {@link ParsedTemplateSectionContent} instances for the bean.
	 */
	private final ParsedTemplateSectionContent[] contents;

	/**
	 * Initiate.
	 * 
	 * @param beanName
	 *            Property name to obtain the bean.
	 * @param contents
	 *            {@link ParsedTemplateSectionContent} instances for the bean.
	 */
	public BeanParsedTemplateSectionContent(String beanName, ParsedTemplateSectionContent[] contents) {
		this.beanName = beanName;
		this.contents = contents;
	}

	/**
	 * Obtains the name of the property to obtain the bean.
	 * 
	 * @return Name of the property to obtain the bean.
	 */
	public String getPropertyName() {
		return this.beanName;
	}

	/**
	 * Obtains the {@link ParsedTemplateSectionContent} instances that comprise
	 * the content for this {@link BeanParsedTemplateSectionContent}.
	 * 
	 * @return {@link ParsedTemplateSectionContent} instances that comprise the
	 *         content for this {@link BeanParsedTemplateSectionContent}.
	 */
	public ParsedTemplateSectionContent[] getContent() {
		return this.contents;
	}

}