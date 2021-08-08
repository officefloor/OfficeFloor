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
