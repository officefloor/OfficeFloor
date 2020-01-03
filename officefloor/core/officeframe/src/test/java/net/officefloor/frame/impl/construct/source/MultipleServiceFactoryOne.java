package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link MultipleServiceFactory} first implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class MultipleServiceFactoryOne implements MultipleServiceFactory {

	@Override
	public Class<? extends MultipleServiceFactory> createService(ServiceContext context) throws Throwable {
		return MultipleServiceFactoryOne.class;
	}

}