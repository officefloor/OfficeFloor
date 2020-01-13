package net.officefloor.r2dbc;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import reactor.core.publisher.Mono;

/**
 * {@link ManagedObjectSource} for R2DBC {@link Connection}.
 * 
 * @author Daniel Sagenschneider
 */
public class R2dbcManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * {@link ConnectionFactories}.
	 */
	private ConnectionFactoryOptions connectionOptions;

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader;

	/**
	 * {@link ConnectionFactory}.
	 */
	private ConnectionFactory connectionFactory;

	/**
	 * Possible {@link ConnectionPool}.
	 */
	private ConnectionPool pool = null;

	/**
	 * Obtains the {@link ConnectionPool}.
	 * 
	 * @return {@link ConnectionPool}.
	 */
	public ConnectionPool getConnectionPool() {
		return this.pool;
	}

	/*
	 * ===================== ManagedObjectSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(ConnectionFactoryOptions.DRIVER.name(), "Driver");
		context.addProperty(ConnectionFactoryOptions.PROTOCOL.name(), "Protocol");
		context.addProperty(ConnectionFactoryOptions.USER.name(), "User");
		context.addProperty(ConnectionFactoryOptions.PASSWORD.name(), "Password");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Provide meta-data
		context.setObjectClass(R2dbcSource.class);

		// Create the connection configuration
		ConnectionFactoryOptions.Builder builder = ConnectionFactoryOptions.builder();
		for (String name : mosContext.getPropertyNames()) {
			builder = builder.option(Option.valueOf(name), mosContext.getProperty(name));
		}
		this.connectionOptions = builder.build();

		// Capture class loader
		this.classLoader = mosContext.getClassLoader();

		// Provide recycle to close the connection
		mosContext.getRecycleFunction(() -> (managedFunctionContext) -> {
			RecycleManagedObjectParameter<R2dbcManagedObject> parameter = RecycleManagedObjectParameter
					.getRecycleManagedObjectParameter(managedFunctionContext);
			R2dbcManagedObject mo = parameter.getManagedObject();

			// Flag closing (and attempt to close)
			mo.isComplete.set(true);
			mo.close();
		}).linkParameter(0, RecycleManagedObjectParameter.class);
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {

		// Create the connection factory
		this.connectionFactory = ConnectionFactories.get(this.connectionOptions);

		// Determine if connection pooling (to later shut down)
		if (this.connectionFactory instanceof ConnectionPool) {
			this.pool = (ConnectionPool) this.connectionFactory;
		}
	}

	@Override
	public void stop() {

		// Dispose of pool (if configured)
		if (this.pool != null) {
			this.pool.dispose();
		}
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {

		// Create the connection
		Mono<Connection> connection = Mono.from(this.connectionFactory.create());

		// Return the managed object
		return new R2dbcManagedObject(connection, this.classLoader);
	}

	/**
	 * {@link ManagedObject} for the {@link Connection}.
	 */
	private static class R2dbcManagedObject implements ManagedObject, R2dbcSource {

		/**
		 * Flags complete.
		 */
		private final AtomicBoolean isComplete = new AtomicBoolean(false);

		/**
		 * {@link Mono} to close the {@link Connection}.
		 */
		private final Mono<?> close;

		/**
		 * {@link Connection}.
		 */
		private final Mono<Connection> connection;

		/**
		 * Instantiate.
		 * 
		 * @param connection  {@link Connection}.
		 * @param classLoader {@link ClassLoader}.
		 */
		private R2dbcManagedObject(Mono<Connection> connection, ClassLoader classLoader) {

			// Provide single connection for managed object
			Mono<Connection> singleConnection = connection.toProcessor();

			// Provide means to close connection
			final AtomicInteger activeConnectionCount = new AtomicInteger(0);
			this.close = Mono.defer(() -> {
				if (this.isComplete.get() && (activeConnectionCount.get() == 0)) {
					return singleConnection.flatMap(c -> Mono.from(c.close()));
				}
				return Mono.empty(); // not yet close
			});

			// Proxy the connection (disallow closing connection)
			Mono<Connection> singleProxy = singleConnection.map(c -> proxyConnection(c, classLoader)).toProcessor();

			// Ensure close connection (once done)
			this.connection = singleProxy.flatMap(c -> {
				activeConnectionCount.incrementAndGet();
				return Mono.just(c);
			}).doFinally(s -> {
				activeConnectionCount.decrementAndGet();
				this.close(); // may occur after recycle
			});
		}

		/**
		 * Attempts to close {@link Connection}.
		 */
		private void close() {
			this.close.subscribe();
		}

		/*
		 * ================== ManagedObject =====================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this;
		}

		/*
		 * =================== R2dbcSource ======================
		 */

		@Override
		public Mono<Connection> getConnection() {
			return this.connection;
		}
	}

	/**
	 * Proxy the {@link Connection} to disallow closing.
	 * 
	 * @param connection  {@link Connection} to proxy.
	 * @param classLoader {@link ClassLoader}.
	 * @return {@link Proxy} {@link Connection} disallowing close.
	 */
	private static Connection proxyConnection(Connection connection, ClassLoader classLoader) {
		return (Connection) Proxy.newProxyInstance(classLoader, new Class[] { Connection.class },
				(proxy, method, args) -> {
					if ("close".equals(method.getName())) {
						// Do not close connection
						return Mono.empty();
					} else {
						// Undertake method on actual connection
						Method actualMethod = connection.getClass().getMethod(method.getName(),
								method.getParameterTypes());
						actualMethod.setAccessible(true);
						return actualMethod.invoke(connection, args);
					}
				});
	}

}