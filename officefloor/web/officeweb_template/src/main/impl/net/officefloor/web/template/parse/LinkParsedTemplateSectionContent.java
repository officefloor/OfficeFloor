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