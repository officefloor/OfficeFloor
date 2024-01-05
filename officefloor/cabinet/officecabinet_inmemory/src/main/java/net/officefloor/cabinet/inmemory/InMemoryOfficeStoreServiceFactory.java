package net.officefloor.cabinet.inmemory;

import net.officefloor.cabinet.source.OfficeStoreServiceFactory;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * In memory {@link OfficeStoreServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class InMemoryOfficeStoreServiceFactory implements OfficeStoreServiceFactory {

	/*
	 * ================== OfficeStoreServiceFactory ==================
	 */

	@Override
	public OfficeStore createService(ServiceContext context) throws Throwable {
		return new InMemoryOfficeStore();
	}

}