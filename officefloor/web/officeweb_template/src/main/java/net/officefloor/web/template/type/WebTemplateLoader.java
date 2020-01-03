package net.officefloor.web.template.type;

import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplateFactory;

/**
 * Loads the type for the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateLoader extends WebTemplateFactory {

	/**
	 * Loads the {@link WebTemplateType} for the {@link WebTemplate}.
	 * 
	 * @param template
	 *            Configured {@link WebTemplate} to provide the type information.
	 * @return {@link WebTemplateType} for the {@link WebTemplate}.
	 */
	WebTemplateType loadWebTemplateType(WebTemplate template);

}