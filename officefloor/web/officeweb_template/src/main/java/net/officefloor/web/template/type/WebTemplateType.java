package net.officefloor.web.template.type;

import net.officefloor.web.template.build.WebTemplate;

/**
 * <code>Type definition</code> of a {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateType {

	/**
	 * Obtains the {@link WebTemplateOutputType} definitions for the outputs from
	 * the {@link WebTemplateType}.
	 * 
	 * @return {@link WebTemplateOutputType} definitions for the outputs from the
	 *         {@link WebTemplateType}.
	 */
	WebTemplateOutputType[] getWebTemplateOutputTypes();

}