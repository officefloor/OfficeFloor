package net.officefloor.woof.template;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Context for the {@link WoofTemplateExtensionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionSourceContext extends SourceContext, ConfigurationContext {

	/**
	 * Obtains the application path to the {@link WebTemplate}.
	 * 
	 * @return Application path to the {@link WebTemplate}.
	 */
	String getApplicationPath();

	/**
	 * Obtains the {@link WebTemplate} being extended.
	 * 
	 * @return {@link WebTemplate} being extended.
	 */
	WebTemplate getTemplate();

	/**
	 * Obtains the {@link WebArchitect} that the {@link WebTemplate} has been
	 * added.
	 * 
	 * @return {@link WebArchitect}.
	 */
	WebArchitect getWebArchitect();

	/**
	 * Obtains the {@link OfficeArchitect} that the {@link WebTemplate} has been
	 * added.
	 * 
	 * @return {@link OfficeArchitect}.
	 */
	OfficeArchitect getOfficeArchitect();

}