package net.officefloor.web.template.parse;

/**
 * Test configuration.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyTemplateSectionContentConfig implements
		TemplateSectionContentConfig {

	/**
	 * Property name for lookup of content.
	 */
	public String propertyName;

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
}