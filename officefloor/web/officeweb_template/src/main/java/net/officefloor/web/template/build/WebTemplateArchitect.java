package net.officefloor.web.template.build;

import net.officefloor.web.build.WebArchitect;

/**
 * Architect to create web templates.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateArchitect extends WebTemplateFactory {

	/**
	 * Informs the {@link WebArchitect} of the templates. This is to be invoked once
	 * all templates are configured.
	 */
	void informWebArchitect();

}