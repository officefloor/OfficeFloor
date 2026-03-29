/*-
 * #%L
 * Web Template
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
