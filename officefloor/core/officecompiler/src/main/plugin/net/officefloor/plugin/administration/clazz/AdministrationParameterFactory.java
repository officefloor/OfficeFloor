package net.officefloor.plugin.administration.clazz;

import net.officefloor.frame.api.administration.AdministrationContext;

/**
 * Creates the parameter for the {@link ClassAdministration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationParameterFactory {

	/**
	 * Creates the parameter from the {@link AdministrationContext}.
	 * 
	 * @param context
	 *            {@link AdministrationContext}.
	 * @return Parameter.
	 * @throws Exception
	 *             If fails to create the parameter.
	 */
	Object createParameter(AdministrationContext<?, ?, ?> context) throws Exception;

}