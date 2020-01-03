package net.officefloor.web.template.parse;

import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.parse.ParsedTemplate;
import net.officefloor.web.template.parse.ParsedTemplateSection;

/**
 * Parsed {@link WebTemplate} .
 *
 * @author Daniel Sagenschneider
 */
public class ParsedTemplate {

	/**
	 * {@link ParsedTemplateSection} instances.
	 */
	private final ParsedTemplateSection[] sections;

	/**
	 * Initiate.
	 *
	 * @param sections
	 *            {@link ParsedTemplateSection} instances.
	 */
	public ParsedTemplate(ParsedTemplateSection[] sections) {
		this.sections = sections;
	}

	/**
	 * Obtains the {@link ParsedTemplateSection} instances for this
	 * {@link ParsedTemplate}.
	 *
	 * @return {@link ParsedTemplateSection} instances for this
	 *         {@link ParsedTemplate}.
	 */
	public ParsedTemplateSection[] getSections() {
		return this.sections;
	}

}