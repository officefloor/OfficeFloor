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

import net.officefloor.web.template.build.WebTemplate;

/**
 * Parsed section of the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParsedTemplateSection {

	/**
	 * {@link ParsedTemplateSection} name.
	 */
	private final String sectionName;

	/**
	 * Raw {@link ParsedTemplateSection} content.
	 */
	private final String rawSectionContent;

	/**
	 * {@link ParsedTemplateSectionContent} instances.
	 */
	private final ParsedTemplateSectionContent[] content;

	/**
	 * Initiate.
	 * 
	 * @param sectionName
	 *            {@link ParsedTemplateSection} name.
	 * @param rawSectionContent
	 *            Raw {@link ParsedTemplateSection} content.
	 * @param content
	 *            {@link ParsedTemplateSectionContent} instances.
	 */
	public ParsedTemplateSection(String sectionName, String rawSectionContent, ParsedTemplateSectionContent[] content) {
		this.sectionName = sectionName;
		this.rawSectionContent = rawSectionContent;
		this.content = content;
	}

	/**
	 * Obtains the name of this section.
	 * 
	 * @return Name of this section.
	 */
	public String getSectionName() {
		return this.sectionName;
	}

	/**
	 * Obtains the raw content for this section.
	 * 
	 * @return Raw content for this section.
	 */
	public String getRawSectionContent() {
		return this.rawSectionContent;
	}

	/**
	 * Obtains the {@link ParsedTemplateSectionContent} instances that comprise
	 * the content for this {@link ParsedTemplateSection}.
	 * 
	 * @return {@link ParsedTemplateSectionContent} instances that comprise the
	 *         content for this {@link ParsedTemplateSection}.
	 */
	public ParsedTemplateSectionContent[] getContent() {
		return this.content;
	}

}
