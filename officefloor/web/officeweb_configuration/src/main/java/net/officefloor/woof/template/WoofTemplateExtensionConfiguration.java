package net.officefloor.woof.template;

import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.woof.model.woof.WoofTemplateExtension;
import net.officefloor.woof.model.woof.WoofTemplateModel;

/**
 * Configuration for a {@link WoofTemplateExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtensionConfiguration extends SourceProperties {

	/**
	 * Obtains the application path for the {@link WoofTemplateModel}.
	 * 
	 * @return Application path for the {@link WoofTemplateModel}.
	 */
	String getApplicationPath();

}