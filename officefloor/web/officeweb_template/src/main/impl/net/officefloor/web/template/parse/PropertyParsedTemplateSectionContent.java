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