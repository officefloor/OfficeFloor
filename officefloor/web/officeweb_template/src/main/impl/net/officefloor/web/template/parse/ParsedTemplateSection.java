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
