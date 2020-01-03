package net.officefloor.web.template.extension;

import net.officefloor.web.template.build.WebTemplate;

/**
 * Extension for the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateExtension {

	/**
	 * Extends the {@link WebTemplate}.
	 * 
	 * @param context
	 *            {@link WebTemplateExtensionContext}.
	 * @throws Exception
	 *             If fails to extend the {@link WebTemplate}.
	 */
	void extendWebTemplate(WebTemplateExtensionContext context) throws Exception;

}