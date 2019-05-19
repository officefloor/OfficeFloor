package net.officefloor.app.subscription.store;

import net.officefloor.nosql.objectify.ObjectifyEntityLocator;

/**
 * {@link ObjectifyEntityLocator} for application.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyEntities implements ObjectifyEntityLocator {

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { AccessKey.class, RefreshKey.class, GoogleSignin.class, User.class, Administration.class,
				Domain.class, Invoice.class, Payment.class };
	}

}