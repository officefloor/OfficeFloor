/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * {@link ManagedObjectSource} for a hibernate {@link Session}.
 * 
 * @author Daniel
 */
public class HibernateManagedObjectSource
		extends
		AbstractManagedObjectSource<HibernateManagedObjectSource.HibernateDependenciesEnum, None> {

	/**
	 * {@link SessionFactory}.
	 */
	private SessionFactory sessionFactory;

	/**
	 * Dummy {@link Connection} to trick {@link Session} to think the
	 * {@link Connection} was user supplied.
	 */
	private Connection dummyConnection;

	/*
	 * ====================================================================
	 * AbstractManagedObjectSource
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadSpecification(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.SpecificationContext)
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource#loadMetaData(net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext)
	 */
	@Override
	protected void loadMetaData(
			MetaDataContext<HibernateDependenciesEnum, None> context)
			throws Exception {

		// Specify types
		context.setObjectClass(Session.class);
		context.setManagedObjectClass(HibernateManagedObject.class);

		// Obtain location of configuration file
		String configFilePath = context.getManagedObjectSourceContext()
				.getProperty("configuration", "hibernate.cfg.xml");

		// Create the dummy connection
		this.dummyConnection = (Connection) Proxy.newProxyInstance(this
				.getClass().getClassLoader(), new Class[] { Connection.class },
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						// Should never be invoked
						throw new IllegalStateException(
								"No Connection was supplied to Hibernate Session (via dependencies)");
					}
				});

		// Require connection dependency
		context.getDependencyLoader(HibernateDependenciesEnum.class)
				.mapDependencyType(HibernateDependenciesEnum.CONNECTION,
						Connection.class);

		// Create the Session Factory
		this.sessionFactory = new Configuration().configure(
				context.getManagedObjectSourceContext().getResourceLocator()
						.locateURL(configFilePath)).buildSessionFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource#getManagedObject()
	 */
	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Create the Session (tricking it to believe it has a user supplied
		// connection)
		Session session = this.sessionFactory.openSession(this.dummyConnection);

		// Create the Managed Object
		HibernateManagedObject mo = new HibernateManagedObject(session);

		// Return the Managed Object
		return mo;
	}

	/**
	 * Provides the dependency keys.
	 */
	public static enum HibernateDependenciesEnum {

		/**
		 * {@link Connection}.
		 */
		CONNECTION
	}

}
