package net.officefloor.web.template.parse;

/**
 * Test configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticTemplateSectionContentConfig implements
		TemplateSectionContentConfig {

	/**
	 * Content.
	 */
	public String content;

	public void setContent(String content) {
		this.content = content;
	}
}
