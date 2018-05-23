/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.jpa;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * JPA {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JpaManagedObjectSource extends AbstractManagedObjectSource<JpaManagedObjectSource.Dependencies, None> {

	/**
	 * {@link Property} for the name of the persistence unit.
	 */
	public static final String PROPERTY_PERSISTENCE_UNIT = "persistence.unit.name";

	/**
	 * Dependencies.
	 */
	public static enum Dependencies {
		CONNECTION
	}

	/**
	 * {@link DataSource} {@link Connection}.
	 */
	private final ThreadLocal<Connection> dataSourceConnection = new ThreadLocal<>();

	/**
	 * {@link DataSource}.
	 */
	private DataSource dataSource;

	/**
	 * Persistence unit name.
	 */
	private String persistenceUnitName;

	/**
	 * {@link Properties} for the {@link EntityManagerFactory}.
	 */
	private Properties properties;

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader;

	/**
	 * {@link EntityManagerFactory}.
	 */
	private volatile EntityManagerFactory factory;

	/**
	 * Allows overriding the creation of the {@link EntityManager}.
	 * 
	 * @param factory
	 *            {@link EntityManagerFactory} configured with properties of this
	 *            {@link JpaManagedObjectSource}.
	 * @param connection
	 *            {@link Connection} to use for the {@link EntityManager}.
	 * @return Created {@link EntityManager}.
	 */
	protected EntityManager createEntityManager(EntityManagerFactory factory, Connection connection) {

		// Create properties to provide connection to entity manager
		Map<String, Object> properties = new HashMap<>(1);
		// properties.put(key, value);

		// Create the entity manager
		return factory.createEntityManager(properties);
	}

	/*
	 * ================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_PERSISTENCE_UNIT, "Persistence Unit");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		context.setObjectClass(EntityManager.class);
		context.setManagedObjectClass(JpaManagedObject.class);
		context.addDependency(Dependencies.CONNECTION, Connection.class);

		// Obtain the details to create Entity Manager Factory
		this.persistenceUnitName = mosContext.getProperty(PROPERTY_PERSISTENCE_UNIT);
		this.properties = mosContext.getProperties();
		this.classLoader = mosContext.getClassLoader();

		// Create the DataSource
		this.dataSource = (DataSource) Proxy.newProxyInstance(this.classLoader, new Class[] { DataSource.class },
				(proxy, method, args) -> {
					switch (method.getName()) {
					case "getConnection":
						return this.dataSourceConnection.get();
					}
					throw new UnsupportedOperationException("Method " + method.getName()
							+ " not available from JPA proxy " + DataSource.class.getSimpleName());
				});
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new JpaManagedObject();
	}

	/**
	 * JPA {@link ManagedObject}.
	 */
	private class JpaManagedObject implements CoordinatingManagedObject<Dependencies> {

		/**
		 * {@link EntityManager}.
		 */
		private EntityManager entityManager;

		/*
		 * ================== ManagedObject =======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the connection
			Connection connection = (Connection) registry.getObject(Dependencies.CONNECTION);

			// Easy access to managed object source
			JpaManagedObjectSource mos = JpaManagedObjectSource.this;

			// Provide proxy connection (that does not close connection)
			Connection proxyConnection = (Connection) Proxy.newProxyInstance(mos.classLoader,
					new Class<?>[] { Connection.class }, (proxy, method, args) -> {

						// Do not close
						if ("close".equals(method.getName())) {
							return null;
						}

						// Invoke connection
						return connection.getClass().getMethod(method.getName(), method.getParameterTypes())
								.invoke(connection, args);
					});

			// Ensure have factory
			if (mos.factory == null) {

				// Specify connection for setup of entity manager factory
				mos.dataSourceConnection.set(proxyConnection);

				// Create the entity manager factory
				Map map = new HashMap(mos.properties);

				// TODO provide means to make this configurable for different vendors
				map.put("datanucleus.ConnectionFactory", mos.dataSource);

				mos.factory = Persistence.createEntityManagerFactory(persistenceUnitName, map);
			}

			// Create the entity manager
			EntityManagerFactory factory = JpaManagedObjectSource.this.factory;
			EntityManager entityManager = factory.createEntityManager();

			// Provide proxy entity manager (specifying connection)
			this.entityManager = (EntityManager) Proxy.newProxyInstance(mos.classLoader,
					new Class<?>[] { EntityManager.class }, (proxy, method, args) -> {

						// Specify the connection
						mos.dataSourceConnection.set(proxyConnection);

						// Invoke entity manager method with appropriate connection
						return entityManager.getClass().getMethod(method.getName(), method.getParameterTypes())
								.invoke(entityManager, args);
					});
		}

		@Override
		public Object getObject() throws Throwable {
			return this.entityManager;
		}
	}

}