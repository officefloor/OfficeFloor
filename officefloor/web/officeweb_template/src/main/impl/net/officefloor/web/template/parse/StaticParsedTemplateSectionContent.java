package net.officefloor.web.template.parse;

/**
 * {@link ParsedTemplateSectionContent} containing static content.
 *
 * @author Daniel Sagenschneider
 */
public class StaticParsedTemplateSectionContent implements ParsedTemplateSectionContent {

	/**
	 * Static content.
	 */
	private final String staticContent;

	/**
	 * Initiate.
	 *
	 * @param staticContent
	 *            Static content.
	 */
	public StaticParsedTemplateSectionContent(String staticContent) {
		this.staticContent = staticContent;
	}

	/**
	 * Obtains the static content.
	 *
	 * @return Static content.
	 */
	public String getStaticContent() {
		return this.staticContent;
	}

}