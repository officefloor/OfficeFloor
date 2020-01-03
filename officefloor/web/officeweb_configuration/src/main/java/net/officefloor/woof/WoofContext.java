package net.officefloor.woof;

import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.template.build.WebTemplateArchitect;

/**
 * Context for the {@link WoofLoader}.
 *
 * @author Daniel Sagenschneider
 */
public interface WoofContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link WebArchitect}.
	 * 
	 * @return {@link WebArchitect}.
	 */
	WebArchitect getWebArchitect();

	/**
	 * Obtains the {@link HttpSecurityArchitect}.
	 * 
	 * @return {@link HttpSecurityArchitect}.
	 */
	HttpSecurityArchitect getHttpSecurityArchitect();

	/**
	 * Obtains the {@link WebTemplateArchitect}.
	 * 
	 * @return {@link WebTemplateArchitect}.
	 */
	WebTemplateArchitect getWebTemplater();

	/**
	 * Obtains the {@link HttpResourceArchitect}.
	 * 
	 * @return {@link HttpResourceArchitect}.
	 */
	HttpResourceArchitect getHttpResourceArchitect();

	/**
	 * Obtains the {@link ProcedureArchitect}.
	 * 
	 * @return {@link ProcedureArchitect}.
	 */
	ProcedureArchitect<OfficeSection> getProcedureArchitect();

	/**
	 * Obtains the {@link OfficeArchitect}.
	 * 
	 * @return {@link OfficeArchitect}.
	 */
	OfficeArchitect getOfficeArchitect();

	/**
	 * Obtains the {@link OfficeExtensionContext}.
	 * 
	 * @return {@link OfficeExtensionContext}.
	 */
	OfficeExtensionContext getOfficeExtensionContext();

}