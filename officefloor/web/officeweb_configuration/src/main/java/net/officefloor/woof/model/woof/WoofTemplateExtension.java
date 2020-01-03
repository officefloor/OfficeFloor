package net.officefloor.woof.model.woof;

import net.officefloor.woof.model.woof.WoofTemplateModel;
import net.officefloor.woof.template.WoofTemplateExtensionSource;

/**
 * Extension for a {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtension {

	/**
	 * Obtains the {@link WoofTemplateExtensionSource} fully qualified class
	 * name providing the extension of the {@link WoofTemplateModel}.
	 * 
	 * @return {@link WoofTemplateExtensionSource} fully qualified class name
	 *         providing the extension of the {@link WoofTemplateModel}.
	 */
	String getWoofTemplateExtensionSourceClassName();

	/**
	 * Obtains the {@link WoofTemplateExtensionProperty} instances to configure
	 * the extension of the {@link WoofTemplateModel}.
	 * 
	 * @return {@link WoofTemplateExtensionProperty} instances to configure the
	 *         extension of the {@link WoofTemplateModel}.
	 */
	WoofTemplateExtensionProperty[] getWoofTemplateExtensionProperties();

}