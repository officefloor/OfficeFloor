package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceFactory} to source property.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyServiceFactory implements ServiceFactory<String> {

	public static final String PROPERTY_NAME = "property";

	@Override
	public String createService(ServiceContext context) throws Throwable {
		return context.getProperty(PROPERTY_NAME);
	}

}