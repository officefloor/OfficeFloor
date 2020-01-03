package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link MultipleServiceFactory} second implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class MultipleServiceFactoryTwo implements MultipleServiceFactory {

	@Override
	public Class<? extends MultipleServiceFactory> createService(ServiceContext context) throws Throwable {
		return MultipleServiceFactoryTwo.class;
	}

}