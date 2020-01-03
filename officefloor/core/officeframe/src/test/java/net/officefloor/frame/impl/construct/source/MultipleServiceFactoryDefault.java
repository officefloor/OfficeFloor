package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link MultipleServiceFactory} implementation that is not configured.
 * 
 * @author Daniel Sagenschneider
 */
public class MultipleServiceFactoryDefault implements MultipleServiceFactory {

	@Override
	public Class<? extends MultipleServiceFactory> createService(ServiceContext context) throws Throwable {
		return MultipleServiceFactoryDefault.class;
	}

}