/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * {@link ManagedObjectSource} for a hibernate {@link Session}.
 * 
 * @author Daniel Sagenschneider
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
	 * ================ AbstractAsyncManagedObjectSource =======================
	 */
	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<HibernateDependenciesEnum, None> context)
			throws Exception {
		ManagedObjectSourceContext<None> mosContext = context
				.getManagedObjectSourceContext();

		// Specify types
		context.setObjectClass(Session.class);
		context.setManagedObjectClass(HibernateManagedObject.class);

		// Obtain location of configuration file
		String configFilePath = context.getManagedObjectSourceContext()
				.getProperty("configuration", "hibernate.cfg.xml");

		// Create the dummy connection
		this.dummyConnection = (Connection) Proxy.newProxyInstance(
				mosContext.getClassLoader(), new Class[] { Connection.class },
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						// Should never be invoked
						throw new IllegalStateException(
								"No Connection was supplied to Hibernate Session (via dependencies)");
					}
				});

		// Require connection dependency
		context.addDependency(HibernateDependenciesEnum.CONNECTION,
				Connection.class);

		// Create the Session Factory
		this.sessionFactory = new Configuration().configure(
				mosContext.getClassLoader().getResource(configFilePath))
				.buildSessionFactory();
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// Create the Session (with user supplied connection)
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