package net.officefloor.woof.template;

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Source that allows extending behaviour of a {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionSource {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	WoofTemplateExtensionSourceSpecification getSpecification();

	/**
	 * <p>
	 * This is only invoked by the WoOF editor to enable managing configuration
	 * for the {@link WoofTemplateExtensionSource}. It is not used during
	 * extension of the {@link WebTemplate}.
	 * <p>
	 * This method is to create a potential {@link Change} to the configuration
	 * necessary for the {@link WoofTemplateExtensionSource}. Should no
	 * {@link Change} be required it should return <code>null</code>.
	 * <p>
	 * {@link WoofTemplateExtensionSource} implementations may require
	 * configuration by extra files within the application. This method allows
	 * the {@link WoofTemplateExtensionSource} to create/update/delete the files
	 * within the {@link ConfigurationContext} (i.e. Java raw source project).
	 * <p>
	 * Note that all actions must be undertaken by the returned {@link Change}
	 * as this method may be invoked to validate configuration. This is to avoid
	 * side effects by the WoOF editor.
	 * <p>
	 * Should configuration of the {@link WoofTemplateExtensionSource} be
	 * invalid, this method should return a {@link Change} with a
	 * {@link Conflict} instance explaining the reason the configuration is
	 * invalid.
	 * 
	 * @param context
	 *            {@link WoofTemplateExtensionChangeContext}.
	 * @return {@link Change} or <code>null</code> if no change is necessary.
	 */
	Change<?> createConfigurationChange(WoofTemplateExtensionChangeContext context);

	/**
	 * Extends the {@link WebTemplate}.
	 * 
	 * @param context
	 *            {@link WoofTemplateExtensionSourceContext}.
	 * @throws Exception
	 *             If fails to extend the {@link WebTemplate}.
	 */
	void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception;

}