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
		return new Class[] { GoogleSignin.class, User.class, Domain.class };
	}

}