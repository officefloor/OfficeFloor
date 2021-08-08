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
