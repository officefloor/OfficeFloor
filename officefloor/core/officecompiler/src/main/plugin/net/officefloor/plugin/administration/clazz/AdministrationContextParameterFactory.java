package net.officefloor.plugin.administration.clazz;

import net.officefloor.frame.api.administration.AdministrationContext;

/**
 * {@link AdministrationParameterFactory} to obtain the
 * {@link AdministrationContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationContextParameterFactory implements AdministrationParameterFactory {

	/*
	 * ==================== ParameterFactory ========================
	 */

	@Override
	public Object createParameter(AdministrationContext<?, ?, ?> context) throws Exception {
		return context;
	}

}