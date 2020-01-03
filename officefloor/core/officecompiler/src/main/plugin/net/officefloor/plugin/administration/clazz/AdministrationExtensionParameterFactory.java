package net.officefloor.plugin.administration.clazz;

import net.officefloor.frame.api.administration.AdministrationContext;

/**
 * {@link AdministrationParameterFactory} to obtain the extensions.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationExtensionParameterFactory implements AdministrationParameterFactory {

	/*
	 * ==================== ParameterFactory ========================
	 */

	@Override
	public Object createParameter(AdministrationContext<?, ?, ?> context) throws Exception {
		return context.getExtensions();
	}

}