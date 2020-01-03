package net.officefloor.web.template.parse;

import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.parse.ParsedTemplateSection;
import net.officefloor.web.template.parse.ParsedTemplateSectionContent;

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