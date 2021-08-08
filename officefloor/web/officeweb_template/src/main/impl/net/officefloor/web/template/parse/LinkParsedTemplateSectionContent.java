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
