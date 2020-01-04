/*-
 * #%L
 * Web Template
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.template.parse;

import java.util.LinkedList;
import java.util.List;

/**
 * Test configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class BeanTemplateSectionContentConfig implements
		TemplateSectionContentConfig {

	/**
	 * Property name to obtain the bean.
	 */
	public String beanName;

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Allows overriding the open tag. May have spacing or within comment.
	 */
	public String openTag = null;

	public void setOpenTag(String openTag) {
		this.openTag = openTag;
	}

	/**
	 * Obtains the open tag.
	 * 
	 * @return Open tag.
	 */
	public String getOpenTag() {
		return (this.openTag == null ? ("${" + this.beanName + " ")
				: this.openTag);
	}

	/**
	 * Allows overriding the close tag. May be blank, have spacing or within
	 * comment.
	 */
	public String closeTag = " $}";

	public void setCloseTag(String closeTag) {
		this.closeTag = closeTag;
	}

	/**
	 * Obtains the close tag.
	 * 
	 * @return Close tag.
	 */
	public String getCloseTag() {
		return (this.closeTag == null ? "" : this.closeTag);
	}

	/**
	 * {@link TemplateSectionContentConfig} instances.
	 */
	public List<TemplateSectionContentConfig> contents = new LinkedList<TemplateSectionContentConfig>();

	public void addContent(TemplateSectionContentConfig content) {
		this.contents.add(content);
	}
}
