package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * Single configured {@link ServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class SingleServiceFactory implements ServiceFactory<Class<SingleServiceFactory>> {

	@Override
	public Class<SingleServiceFactory> createService(ServiceContext context) throws Throwable {
		return SingleServiceFactory.class;
	}

}