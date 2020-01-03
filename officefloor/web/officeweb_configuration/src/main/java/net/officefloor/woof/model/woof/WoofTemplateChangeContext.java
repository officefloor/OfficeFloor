package net.officefloor.woof.model.woof;

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.model.change.Change;
import net.officefloor.woof.model.woof.WoofTemplateModel;

/**
 * Context for changing a {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateChangeContext extends SourceContext {

	/**
	 * <p>
	 * Obtains the {@link ConfigurationContext}.
	 * <p>
	 * The {@link ConfigurationContext} is at the root of the Project source.
	 * <p>
	 * Note that Projects are anticipated to follow the standard
	 * <a href="http://maven.apache.org">Maven</a> project structure.
	 * 
	 * @return {@link ConfigurationContext}.
	 */
	ConfigurationContext getConfigurationContext();

	/**
	 * Obtains the {@link WoofChangeIssues} to allow reporting issue in
	 * attempting the {@link Change}.
	 * 
	 * @return {@link WoofChangeIssues} to allow reporting issue in attempting
	 *         the {@link Change}.
	 */
	WoofChangeIssues getWoofChangeIssues();

}