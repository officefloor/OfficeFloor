/*-
 * #%L
 * JPA Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
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
import java.util.function.Consumer;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;

import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler.ClassName;
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
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupCompletion;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;

/**
 * JPA {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JpaManagedObjectSource extends AbstractManagedObjectSource<Indexed, None> {

	/**
	 * {@link Property} to specify the dependency type for the
	 * {@link EntityManager}.
	 */
	public static final String PROPERTY_DEPENDENCY_TYPE = "persistence.dependency";

	/**
	 * Dependency type.
	 */
	public static enum DependencyType {

		/**
		 * Value for {{@link #PROPERTY_DEPENDENCY_TYPE} indicating depending on
		 * {@link Connection}.
		 */
		connection,

		/**
		 * Value for {@link #PROPERTY_DEPENDENCY_TYPE} indicating depending on
		 * {@link DataSource}.
		 */
		datasource,

		/**
		 * Value for {@link #PROPERTY_DEPENDENCY_TYPE} indicating the
		 * {@link EntityManager} will manage connectivity.
		 */
		managed
	}

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
		 *                            {@link EntityManagerFactory}. <code>null</code> if
		 *                            {@link EntityManagerFactory} to create and manage
		 *                            the {@link DataSource}.
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
	 * Extracts the {@link Connection}.
	 */
	@FunctionalInterface
	private static interface JpaConnectionFactory {

		/**
		 * Obtains the {@link Connection}.
		 * 
		 * @param dependencyRetriever Retrieves the indexed dependency.
		 * @return {@link Connection}.
		 */
		Connection createConnection(Function<Integer, Object> dependencyRetriever);
	}

	/**
	 * Extracts the {@link DataSource}.
	 */
	@FunctionalInterface
	private static interface JpaDataSourceFactory {

		/**
		 * Obtains the {@link DataSource}.
		 * 
		 * @param dependencyRetriever Retrieves the dependency for the index.
		 * @return {@link DataSource}.
		 */
		DataSource createDataSource(ManagedFunctionContext<Indexed, None> context);
	}

	/**
	 * Wraps the {@link EntityManager}.
	 */
	public static interface EntityManagerWrapper extends EntityManager {

		/**
		 * Obtains the unwrapped {@link EntityManager}.
		 * 
		 * @return Unwrapped {@link EntityManager}.
		 */
		EntityManager getUnwrappedEntityManager();
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
		EntityManagerWrapper wrap(JpaManagedObject jpaManagedObject) throws Exception;
	}

	/**
	 * <p>
	 * Begins the transaction on the {@link EntityManager}.
	 * <p>
	 * As {@link OfficeFloor} may manage the transaction for the
	 * {@link EntityManager}, it can not be begun. This enables beginning the
	 * transaction.
	 * 
	 * @param entityManager {@link EntityManager}.
	 */
	public static void beginTransaction(EntityManager entityManager) {

		// Begin the transaction
		EntityManager unwrapped = getUnwrappedEntityManager(entityManager);
		EntityTransaction transaction = unwrapped.getTransaction();
		transaction.begin();
	}

	/**
	 * <p>
	 * Commits the transaction on the {@link EntityManager}.
	 * <p>
	 * As {@link OfficeFloor} may manage the transaction for the
	 * {@link EntityManager}, it can not be committed. This enables committing the
	 * transaction.
	 * 
	 * @param entityManager {@link EntityManager}.
	 */
	public static void commitTransaction(EntityManager entityManager) {

		// Commit the transaction
		EntityManager unwrapped = getUnwrappedEntityManager(entityManager);
		EntityTransaction transaction = unwrapped.getTransaction();
		transaction.commit();

		// Restart the transaction
		transaction.begin();
	}

	/**
	 * Obtains the unwrapped {@link EntityManager}.
	 * 
	 * @param entityManager {@link EntityManager}.
	 * @return Unwrapped {@link EntityManager}.
	 */
	private static EntityManager getUnwrappedEntityManager(EntityManager entityManager) {

		// Ensure can unwrap
		if (!(entityManager instanceof EntityManagerWrapper)) {
			throw new IllegalArgumentException(EntityManager.class.getSimpleName() + " is not managed by "
					+ JpaManagedObjectSource.class.getName());
		}
		EntityManagerWrapper wrapper = (EntityManagerWrapper) entityManager;

		// Return the unwrapped entity manager
		return wrapper.getUnwrappedEntityManager();
	}

	/**
	 * {@link DataSource} {@link Connection}.
	 */
	private final ThreadLocal<Connection> dataSourceConnection = new ThreadLocal<>();

	/**
	 * {@link JpaConnectionFactory}.
	 */
	private JpaConnectionFactory jpaConnectionFactory;

	/**
	 * {@link JpaDataSourceFactory}.
	 */
	private JpaDataSourceFactory jpaDataSourceFactory;

	/**
	 * Persistence unit name.
	 */
	private String persistenceUnitName;

	/**
	 * {@link Properties} for the {@link EntityManagerFactory}.
	 */
	private Properties properties;

	/**
	 * {@link EntityManagerFactory}.
	 */
	private volatile EntityManagerFactory entityManagerFactory;

	/**
	 * Setup for the {@link EntityManager}.
	 */
	private Consumer<JpaManagedObject> entityManagerSetup;

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
	 * @param context {@link SourceContext}.
	 * @return {@link PersistenceFactory}.
	 * @throws Exception If fails to create the {@link PersistenceFactory}.
	 */
	protected PersistenceFactory getPersistenceFactory(SourceContext context) throws Exception {

		// Obtain the class name
		String className = context.getProperty(PROPERTY_PERSISTENCE_FACTORY);

		// Create instance and return
		return (PersistenceFactory) context.loadClass(className).getDeclaredConstructor().newInstance();
	}

	/**
	 * Indicates if required to run within {@link EntityTransaction}.
	 * 
	 * @return <code>true</code> if required to run within
	 *         {@link EntityTransaction}.
	 */
	protected boolean isRunWithinTransaction() {
		return true;
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
	protected void loadMetaData(MetaDataContext<Indexed, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		context.setObjectClass(EntityManager.class);
		context.setManagedObjectClass(JpaManagedObject.class);
		context.addManagedObjectExtension(EntityManager.class,
				(managedObject) -> ((JpaManagedObject) managedObject).entityManager);
		context.getManagedObjectSourceContext().getRecycleFunction(new RecycleFunction()).linkParameter(0,
				RecycleManagedObjectParameter.class);

		// Obtain the details to create Entity Manager Factory
		this.persistenceUnitName = mosContext.getProperty(PROPERTY_PERSISTENCE_UNIT);
		this.properties = mosContext.getProperties();
		ClassLoader classLoader = mosContext.getClassLoader();

		// Obtain the dependency type
		String dependencyTypeName = mosContext.getProperty(PROPERTY_DEPENDENCY_TYPE, DependencyType.managed.name());
		DependencyType dependencyType;
		try {
			dependencyType = DependencyType.valueOf(dependencyTypeName.toLowerCase());
		} catch (IllegalArgumentException ex) {
			throw new Exception("Unknown " + PROPERTY_DEPENDENCY_TYPE + " property value '" + dependencyTypeName + "'");
		}

		// Register the start up function
		ManagedObjectStartupCompletion startupCompletion = mosContext.createStartupCompletion();
		final String startupFunctionName = "startup";
		ManagedObjectFunctionBuilder<Indexed, None> startupFunction = mosContext.addManagedFunction(startupFunctionName,
				new JpaStartupFunction(mosContext, startupCompletion));
		mosContext.addStartupFunction(startupFunctionName, null);

		// Determine the means to access data source
		switch (dependencyType) {

		case connection:
			// Add dependency on connection
			context.addDependency(Connection.class).setLabel(Connection.class.getSimpleName());

			// Setup entity manager by making connection available
			this.entityManagerSetup = (mo) -> this.dataSourceConnection.set(mo.connection);

			// Obtain the connection
			this.jpaConnectionFactory = (dependencyRetriever) -> (Connection) dependencyRetriever.apply(0);

			// Determine compiler available
			OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(mosContext);
			if (compiler == null) {

				// Fall back to proxy for data source wrapper
				DataSource dataSource = (DataSource) Proxy.newProxyInstance(classLoader,
						new Class[] { DataSource.class }, (proxy, method, args) -> {
							switch (method.getName()) {
							case "getConnection":
								Connection connection = this.dataSourceConnection.get();
								if (this.isRunWithinTransaction() && connection.getAutoCommit()) {
									connection.setAutoCommit(false);
								}
								return connection;
							default:
								throw new UnsupportedOperationException("Method " + method.getName()
										+ " not available from JPA proxy " + DataSource.class.getSimpleName());
							}
						});
				this.jpaDataSourceFactory = (registry) -> dataSource;

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
						source.println("    " + compiler.getSourceName(Connection.class)
								+ " connection = this.mos.getConnection();");
						if (this.isRunWithinTransaction()) {
							source.println("    if (connection.getAutoCommit()) {");
							source.println("      connection.setAutoCommit(false);");
							source.println("    }");
						}
						source.println("    return connection;");
						break;
					default:
						source.println("    throw new " + compiler.getSourceName(SQLFeatureNotSupportedException.class)
								+ "();");
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
				DataSource dataSource = (DataSource) dataSourceClass.getConstructor(JpaManagedObjectSource.class)
						.newInstance(this);
				this.jpaDataSourceFactory = (registry) -> dataSource;
			}

			// Provide connection to start up function
			startupFunction.linkObject(0, mosContext.addFunctionDependency("CONNECTION", Connection.class));
			break;

		case datasource:
			// Add dependency on data source
			context.addDependency(DataSource.class).setLabel(DataSource.class.getSimpleName());

			// No setup for entity manager
			this.entityManagerSetup = (mo) -> {
			};

			// No connection (as provided directly via data source)
			this.jpaConnectionFactory = (registry) -> null;

			// Obtain the data source
			this.jpaDataSourceFactory = (mfContext) -> (DataSource) mfContext.getObject(0);

			// Provide data source to start up function
			startupFunction.linkObject(0, mosContext.addFunctionDependency("DATA_SOURCE", DataSource.class));
			break;

		case managed:
			// No dependencies as data source managed by entity manager
			this.entityManagerSetup = (mo) -> {
			};
			this.jpaConnectionFactory = (registry) -> null;
			this.jpaDataSourceFactory = (registry) -> null;
			break;

		default:
			throw new IllegalArgumentException(
					"Unknown " + PROPERTY_DEPENDENCY_TYPE + " configuration value '" + dependencyType + "'");
		}

		// Determine compiler available
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(mosContext);
		if (compiler == null) {

			// Create fall back proxy entity manager wrapper
			Class<?>[] interfaces = new Class[] { EntityManagerWrapper.class };
			this.wrapperFactory = (mo) -> (EntityManagerWrapper) Proxy.newProxyInstance(classLoader, interfaces, mo);

		} else {
			// Use compiled implementation of entity manager wrapper
			Class<?> wrapperClass = compiler.addWrapper(new Class[] { EntityManagerWrapper.class },
					JpaManagedObject.class, "this.delegate.getEntityManager()", null, (wrapperContext) -> {
						switch (wrapperContext.getMethod().getName()) {

						case "getUnwrappedEntityManager":
							wrapperContext.write("    return this.delegate.getEntityManager();");
							break;

						case "getTransaction":
							if (this.isRunWithinTransaction()) {
								wrapperContext
										.write("    throw new " + compiler.getSourceName(IllegalStateException.class)
												+ "(\"" + compiler.getSourceName(EntityManager.class)
												+ ".getTransaction() may not be invoked as transaction managed by "
												+ compiler.getSourceName(OfficeFloor.class) + "\");");
							}
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
			this.wrapperFactory = (mo) -> (EntityManagerWrapper) constructor.newInstance(mo);
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new JpaManagedObject();
	}

	/**
	 * JPA {@link ManagedObject}.
	 */
	public class JpaManagedObject implements CoordinatingManagedObject<Indexed>, InvocationHandler {

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

			// Setup the entity manager for use
			JpaManagedObjectSource.this.entityManagerSetup.accept(this);

			// Return the entity manager
			return this.entityManager;
		}

		/**
		 * Loads the {@link EntityManager}.
		 * 
		 * @param dependencyRetriever Retrieves the dependency.
		 * @throws Exception If fails to load the {@link EntityManager}.
		 */
		protected void loadEntityManager(Function<Integer, Object> dependencyRetriever) throws Exception {

			// Easy access to managed object source
			JpaManagedObjectSource mos = JpaManagedObjectSource.this;

			// Setup the possible connection
			this.connection = mos.jpaConnectionFactory.createConnection(dependencyRetriever);
			mos.entityManagerSetup.accept(this);

			// Create the entity manager
			this.entityManager = mos.entityManagerFactory.createEntityManager();

			// Run entity manager within managed transaction
			if (mos.isRunWithinTransaction()) {
				this.entityManager.getTransaction().begin();
			}

			// Provide proxy entity manager (to specify connection)
			this.proxy = mos.wrapperFactory.wrap(this);
		}

		/*
		 * ================== ManagedObject =======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {
			this.loadEntityManager((index) -> registry.getObject(index));
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

			case "getUnwrappedEntityManager":
				return this.getEntityManager();

			case "getTransaction":
				throw new IllegalStateException(EntityManager.class.getSimpleName()
						+ ".getTransaction() may not be invoked as transaction managed by "
						+ OfficeFloor.class.getSimpleName());

			case "close":
				throw new IllegalStateException(EntityManager.class.getSimpleName()
						+ ".close() may not be invoked as managed by " + OfficeFloor.class.getName());

			default:
				// Obtain the entity manager
				EntityManager entityManager = this.getEntityManager();

				// Obtain the delegate method
				return entityManager.getClass().getMethod(method.getName(), method.getParameterTypes())
						.invoke(entityManager, args);
			}
		}
	}

	/**
	 * Recycles the {@link EntityManager}.
	 */
	private static class RecycleFunction extends StaticManagedFunction<Indexed, None> {

		@Override
		public void execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Obtain the entity manager
			RecycleManagedObjectParameter<JpaManagedObject> recycle = RecycleManagedObjectParameter
					.getRecycleManagedObjectParameter(context);
			EntityManager entityManager = recycle.getManagedObject().entityManager;
			try {

				// Handle possible transaction
				if (entityManager.isJoinedToTransaction()) {

					// If no escalations, then commit changes
					CleanupEscalation[] escalations = recycle.getCleanupEscalations();
					if ((escalations != null) && (escalations.length > 0)) {
						// Escalations, so rollback transaction
						entityManager.getTransaction().rollback();
					} else {
						// No escalations, so commit the transaction
						entityManager.getTransaction().commit();
					}
				}

			} finally {
				// Ensure close the entity manager
				entityManager.close();
			}

			// Reuse the connection
			recycle.reuseManagedObject();
		}
	}

	/**
	 * Undertakes the start up of JPA.
	 */
	private class JpaStartupFunction extends StaticManagedFunction<Indexed, None> {

		/**
		 * {@link SourceContext}.
		 */
		private final SourceContext sourceContext;

		/**
		 * {@link ManagedObjectStartupCompletion}.
		 */
		private final ManagedObjectStartupCompletion startupCompletion;

		/**
		 * Instantiate.
		 * 
		 * @param sourceContext     {@link SourceContext}.
		 * @param startupCompletion {@link ManagedObjectStartupCompletion}.
		 */
		private JpaStartupFunction(SourceContext sourceContext, ManagedObjectStartupCompletion startupCompletion) {
			this.sourceContext = sourceContext;
			this.startupCompletion = startupCompletion;
		}

		/*
		 * ===================== ManagedFunction ========================
		 */

		@Override
		public void execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Easy access to managed object
			JpaManagedObjectSource mos = JpaManagedObjectSource.this;

			// Undertake start up of JPA
			try {

				// Obtain the data source
				DataSource dataSource = mos.jpaDataSourceFactory.createDataSource(context);

				// Setup possible connection for data source
				JpaManagedObject mo = new JpaManagedObject();
				mo.connection = mos.jpaConnectionFactory.createConnection((index) -> context.getObject(index));
				mos.entityManagerSetup.accept(mo);

				// Create the entity manager factory
				mos.entityManagerFactory = mos.getPersistenceFactory(this.sourceContext)
						.createEntityManagerFactory(mos.persistenceUnitName, dataSource, mos.properties);

				// Flag ready to start up
				this.startupCompletion.complete();

			} catch (Throwable ex) {
				// Indicate JPA failure (will be invalid to start up)
				this.startupCompletion.failOpen(ex);
			}
		}
	}

}
