package net.officefloor.tutorial.rawhttpserver;

import net.officefloor.web.template.NotEscaped;

/**
 * Example template logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
public class TemplateLogic {

	/**
	 * Able to use <code>this</code> as bean for populating.
	 * 
	 * @return {@link TemplateLogic} to provide properties.
	 */
	public TemplateLogic getTemplateData() {
		return this;
	}

	/**
	 * Provides the raw HTML to render. It is not escaped due to the
	 * {@link NotEscaped} annotation.
	 * 
	 * @return Raw HTML to render.
	 */
	@NotEscaped
	public String getRawHtml() {
		return "<p style=\"color: blue\">" + "<img src=\"./images/OfficeFloorLogo.png\" />"
				+ " Web on OfficeFloor (WoOF)</p>";
	}

}
// END SNIPPET: example