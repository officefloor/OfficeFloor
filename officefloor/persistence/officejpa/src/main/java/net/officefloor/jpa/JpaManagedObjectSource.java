/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.jpa;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler;
import net.officefloor.compile.impl.compile.OfficeFloorJavaCompiler.ClassName;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
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
	 * {@link Property} to obtain the {@link Class} name for the
	 * {@link PersistenceFactory}.
	 */
	public static final String PROPERTY_PERSISTENCE_FACTORY = "persistence.factory";

	/**
	 * Dependencies.
	 */
	public static enum Dependencies {
		CONNECTION
	}

	/**
	 * <p>
	 * {@link FunctionalInterface} to create the {@link EntityManagerFactory}.
	 * <p>
	 * Note: the {@link EntityManagerFactory} is required to be configured with the
	 * input {@link DataSource}.
	 */
	@FunctionalInterface
	public static interface PersistenceFactory {

		/**
		 * Creates the {@link EntityManagerFactory}.
		 * 
		 * @param persistenceUnitName Persistence Unit name.
		 * @param dataSource          {@link DataSource} to use for the
		 *                            {@link EntityManagerFactory}.
		 * @param properties          Existing properties configured to the
		 *                            {@link JpaManagedObjectSource}.
		 * @return Configuration for the {@link EntityManagerFactory} to use the
		 *         {@link DataSource}.
		 * @throws Exception If fails to create the {@link EntityManagerFactory}.
		 */
		EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, DataSource dataSource,
				Properties properties) throws Exception;
	}

	/**
	 * Factory creating the {@link EntityManager} wrapper.
	 */
	public static interface EntityManagerWrapperFactory {

		/**
		 * Wraps the {@link EntityManager}.
		 * 
		 * @param jpaManagedObject {@link JpaManagedObject} providing the
		 *                         {@link EntityManager}.
		 * @return {@link EntityManager}.
		 * @throws Exception If fails to create wrapper for {@link EntityManager}.
		 */
		EntityManager wrap(JpaManagedObject jpaManagedObject) throws Exception;
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
	 * {@link PersistenceFactory}.
	 */
	private PersistenceFactory persistenceFactory;

	/**
	 * {@link EntityManagerFactory}.
	 */
	private EntityManagerFactory entityManagerFactory;

	/**
	 * {@link EntityManagerWrapperFactory}.
	 */
	private EntityManagerWrapperFactory wrapperFactory;

	/**
	 * Obtains the {@link Connection}.
	 * 
	 * @return {@link Connection}.
	 */
	public Connection getConnection() {
		return this.dataSourceConnection.get();
	}

	/**
	 * <p>
	 * Obtains the {@link PersistenceFactory}.
	 * <p>
	 * Specific vendor implementations may override this method to specify the
	 * {@link PersistenceFactory}.
	 * <p>
	 * By default, this method uses the {@link #PROPERTY_PERSISTENCE_FACTORY}
	 * {@link Property} to load the {@link PersistenceFactory}.
	 * 
	 * @param context {@link MetaDataContext}.
	 * @return {@link PersistenceFactory}.
	 * @throws Exception If fails to create the {@link PersistenceFactory}.
	 */
	protected PersistenceFactory getPersistenceFactory(MetaDataContext<Dependencies, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the class name
		String className = mosContext.getProperty(PROPERTY_PERSISTENCE_FACTORY);

		// Create instance and return
		return (PersistenceFactory) mosContext.loadClass(className).getDeclaredConstructor().newInstance();
	}

	/*
	 * ================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_PERSISTENCE_UNIT, "Persistence Unit");

		// Determine if using default implementation
		if (this.getClass() == JpaManagedObjectSource.class) {
			context.addProperty(PROPERTY_PERSISTENCE_FACTORY, "Persistence Factory");
		}
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		context.setObjectClass(EntityManager.class);
		context.setManagedObjectClass(JpaManagedObject.class);
		context.addDependency(Dependencies.CONNECTION, Connection.class);
		context.addManagedObjectExtension(EntityManager.class,
				(managedObject) -> ((JpaManagedObject) managedObject).entityManager);
		context.getManagedObjectSourceContext().getRecycleFunction(new RecycleFunction()).linkParameter(0,
				RecycleManagedObjectParameter.class);

		// Obtain the details to create Entity Manager Factory
		this.persistenceUnitName = mosContext.getProperty(PROPERTY_PERSISTENCE_UNIT);
		this.properties = mosContext.getProperties();
		ClassLoader classLoader = mosContext.getClassLoader();

		// Determine compiler available
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(classLoader);
		if (compiler == null) {

			// Fall back to proxy for data source wrapper
			this.dataSource = (DataSource) Proxy.newProxyInstance(classLoader, new Class[] { DataSource.class },
					(proxy, method, args) -> {
						switch (method.getName()) {
						case "getConnection":
							return this.dataSourceConnection.get();
						}
						throw new UnsupportedOperationException("Method " + method.getName()
								+ " not available from JPA proxy " + DataSource.class.getSimpleName());
					});

			// Create fall back proxy entity manager wrapper
			Class<?>[] interfaces = new Class[] { EntityManager.class };
			this.wrapperFactory = (mo) -> (EntityManager) Proxy.newProxyInstance(classLoader, interfaces, mo);

		} else {
			// Use compiled implementation of data source
			StringWriter sourceBuffer = new StringWriter();
			PrintWriter source = new PrintWriter(sourceBuffer);

			// Obtain wrapper class name
			ClassName className = compiler.createClassName(DataSource.class.getName());

			// Write declaration
			source.println("package " + className.getPackageName() + ";");
			source.println("@" + compiler.getSourceName(SuppressWarnings.class) + "(\"unchecked\")");
			source.println("public class " + className.getClassName() + " implements "
					+ compiler.getSourceName(DataSource.class) + "{");

			// Write constructor
			compiler.writeConstructor(source, className.getClassName(),
					compiler.createField(JpaManagedObjectSource.class, "mos"));

			// Write the methods
			for (Method method : DataSource.class.getMethods()) {

				// Write the method signature
				source.print("  public ");
				compiler.writeMethodSignature(source, method);
				source.println(" {");

				// Write the implementation
				switch (method.getName()) {
				case "getConnection":
					source.println("    return this.mos.getConnection();");
					break;
				default:
					source.println(
							"    throw new " + compiler.getSourceName(SQLFeatureNotSupportedException.class) + "();");
					break;
				}

				// Complete the method
				source.println("  }");
			}

			// Complete class
			source.println("}");
			source.flush();

			// Compile the data source
			Class<?> dataSourceClass = compiler.addSource(className, sourceBuffer.toString()).compile();
			this.dataSource = (DataSource) dataSourceClass.getConstructor(JpaManagedObjectSource.class)
					.newInstance(this);

			// Use compiled implementation of entity manager wrapper
			Class<?> wrapperClass = compiler.addWrapper(EntityManager.class, JpaManagedObject.class,
					"this.delegate.getEntityManager()", (wrapperContext) -> {
						switch (wrapperContext.getMethod().getName()) {
						case "getTransaction":
							wrapperContext.write("    throw new " + compiler.getSourceName(IllegalStateException.class)
									+ "(\"" + compiler.getSourceName(EntityManager.class)
									+ ".getTransaction() may not be invoked as transaction managed by "
									+ compiler.getSourceName(OfficeFloor.class) + "\");");
							break;

						case "close":
							wrapperContext.write("    throw new " + compiler.getSourceName(IllegalStateException.class)
									+ "(\"" + compiler.getSourceName(EntityManager.class)
									+ ".close() may not be invoked as managed by "
									+ compiler.getSourceName(OfficeFloor.class) + "\");");
							break;
						}
					}).compile();
			Constructor<?> constructor = wrapperClass.getConstructor(JpaManagedObject.class);
			this.wrapperFactory = (mo) -> (EntityManager) constructor.newInstance(mo);
		}

		// Obtain the persistence factory
		this.persistenceFactory = this.getPersistenceFactory(context);

	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new JpaManagedObject();
	}

	/**
	 * JPA {@link ManagedObject}.
	 */
	public class JpaManagedObject implements CoordinatingManagedObject<Dependencies>, InvocationHandler {

		/**
		 * {@link Connection}.
		 */
		private Connection connection;

		/**
		 * {@link Proxy} {@link EntityManager}.
		 */
		private EntityManager proxy;

		/**
		 * {@link EntityManager}.
		 */
		private EntityManager entityManager;

		/**
		 * Obtains the {@link EntityManager}.
		 * 
		 * @return {@link EntityManager}.
		 */
		public EntityManager getEntityManager() {

			// Specify the connection
			// All entity manager operations synchronous so will use
			JpaManagedObjectSource.this.dataSourceConnection.set(this.connection);

			// Return the entity manager
			return this.entityManager;
		}

		/*
		 * ================== ManagedObject =======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the connection
			this.connection = (Connection) registry.getObject(Dependencies.CONNECTION);

			// Easy access to managed object source
			JpaManagedObjectSource mos = JpaManagedObjectSource.this;

			// Specify connection for setup of entity manager factory and entity manager
			mos.dataSourceConnection.set(this.connection);

			// If still reference to persistence factory (then likely factory not created)
			if (mos.persistenceFactory != null) {

				// Attempt to ensure only one factory created
				synchronized (mos) {
					if (mos.entityManagerFactory == null) {

						// Create the entity manager factory
						mos.entityManagerFactory = mos.persistenceFactory
								.createEntityManagerFactory(mos.persistenceUnitName, mos.dataSource, mos.properties);
					}

					// Factory created (so indicate no need to create anymore)
					mos.persistenceFactory = null;
				}
			}

			// Create the entity manager
			EntityManagerFactory factory = JpaManagedObjectSource.this.entityManagerFactory;
			this.entityManager = factory.createEntityManager();

			// Run entity manager within managed transaction
			this.entityManager.getTransaction().begin();

			// Provide proxy entity manager (to specify connection)
			this.proxy = mos.wrapperFactory.wrap(this);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.proxy;
		}

		/*
		 * ================ InnovationHandler ============================
		 */

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			// Disallow access to transaction (as managed)
			switch (method.getName()) {
			case "getTransaction":
				throw new IllegalStateException(EntityManager.class.getSimpleName()
						+ ".getTransaction() may not be invoked as transaction managed by "
						+ OfficeFloor.class.getSimpleName());

			case "close":
				throw new IllegalStateException(EntityManager.class.getSimpleName()
						+ ".close() may not be invoked as managed by " + OfficeFloor.class.getName());
			}

			// Obtain the entity manager
			EntityManager entityManager = this.getEntityManager();

			// Invoke method on entity manager
			return entityManager.getClass().getMethod(method.getName(), method.getParameterTypes())
					.invoke(entityManager, args);
		}
	}

	/**
	 * Recycles the {@link EntityManager}.
	 */
	private static class RecycleFunction extends StaticManagedFunction<Indexed, None> {

		@Override
		public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Obtain the entity manager
			RecycleManagedObjectParameter<JpaManagedObject> recycle = RecycleManagedObjectParameter
					.getRecycleManagedObjectParameter(context);
			EntityManager entityManager = recycle.getManagedObject().entityManager;
			try {

				// If no escalations, then commit changes
				CleanupEscalation[] escalations = recycle.getCleanupEscalations();
				if ((escalations != null) && (escalations.length > 0)) {
					// Escalations, so rollback transaction
					entityManager.getTransaction().rollback();
				} else {
					// No escalations, so commit the transaction
					entityManager.getTransaction().commit();
				}

			} finally {
				// Ensure close the entity manager
				entityManager.close();
			}

			// Reuse the connection
			recycle.reuseManagedObject();

			// Nothing further
			return null;
		}
	}

}