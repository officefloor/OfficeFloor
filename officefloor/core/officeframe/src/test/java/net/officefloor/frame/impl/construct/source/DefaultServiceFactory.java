package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * Default {@link NotConfiguredServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultServiceFactory implements NotConfiguredServiceFactory {

	@Override
	public Class<NotConfiguredServiceFactory> createService(ServiceContext context) throws Throwable {
		return NotConfiguredServiceFactory.class;
	}

}