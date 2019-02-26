package net.officefloor.nosql.objectify.mock;

import java.io.IOException;
import java.util.Deque;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cache.PendingFutures;
import com.googlecode.objectify.cmd.LoadType;
import com.googlecode.objectify.cmd.Query;
import com.googlecode.objectify.util.Closeable;

import net.officefloor.nosql.objectify.ObjectifySupplierSource;
import net.officefloor.nosql.objectify.ObjectifyThreadSynchroniserFactory;

/**
 * {@link Rule} for running {@link Objectify} with local {@link Datastore}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifyRule implements TestRule {

	/**
	 * {@link LocalDatastoreHelper}.
	 */
	private static LocalDatastoreHelper dataStore = null;

	/**
	 * Failure to start the {@link LocalDatastoreHelper}. Typically because emulator
	 * not available.
	 */
	private static IOException dataStoreFailure = null;

	/**
	 * Timeout in obtaining entities.
	 */
	private long timeout = 3 * 1000;

	/**
	 * Time to retry the obtaining the entities.
	 */
	private long retry = 10;

	/**
	 * {@link ObjectifyFactory}. Only available within context of rule.
	 */
	private ObjectifyFactory objectifyFactory = null;

	/**
	 * Possible {@link Closeable} for {@link Objectify}.
	 */
	private Closeable closable = null;

	/**
	 * Obtains the {@link Objectify}.
	 * 
	 * @return {@link Objectify}.
	 */
	public Objectify ofy() {

		// Ensure within context
		this.ensureWithinRule("ofy()");

		// Determine if need to start context
		Deque<Objectify> stack = ObjectifyThreadSynchroniserFactory.getStack(this.objectifyFactory);
		if (stack.isEmpty()) {

			// Start for running within rule
			this.closable = ObjectifyService.begin();
		}

		// Return context specific objectify
		return ObjectifyService.ofy();
	}

	/**
	 * Loads the entity.
	 */
	public static interface TypeLoader<E> {
		Query<E> load(LoadType<E> loadType);
	}

	/**
	 * Indicates timeout on obtaining entity.
	 */
	public static class TimeoutException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public TimeoutException(String message) {
			super(message);
		}
	}

	/**
	 * <p>
	 * Obtains an entity.
	 * <p>
	 * Will retry to obtain the entity or timeout.
	 * 
	 * @param type   Type of entity.
	 * @param loader {@link TypeLoader}.
	 * @return Entity.
	 * @throws TimeoutException If waited too long for the entity.
	 */
	public <E> E get(Class<E> type, TypeLoader<E> loader) throws TimeoutException {
		return this.get(type, 1, loader).get(0);
	}

	/**
	 * Obtains a list of entities.
	 * 
	 * @param type         Type of entity.
	 * @param expectedSize Expected list size.
	 * @param loader       {@link TypeLoader}.
	 * @return List of entity.
	 * @throws TimeoutException If waited too long the list of entities.
	 */
	public <E> List<E> get(Class<E> type, int expectedSize, TypeLoader<E> loader) throws TimeoutException {

		// Ensure within rule
		this.ensureWithinRule("get(...)");

		// Attempt to load entities
		long endTime = System.currentTimeMillis() + this.timeout;
		List<E> entities = null;
		do {

			// Determine if repeating
			if (entities != null) {

				// Determine if timed out
				if (endTime < System.currentTimeMillis()) {
					throw new TimeoutException("Timed out retrieving " + expectedSize + " " + type.getSimpleName()
							+ (expectedSize == 1 ? " entity" : " entities"));
				}

				// Wait some time
				try {
					Thread.sleep(this.retry);
				} catch (InterruptedException ex) {
					throw new TimeoutException("Interrupted retrieving entity");
				}
			}

			// Load the entity list
			entities = loader.load(this.ofy().load().type(type)).list();

		} while (entities.size() < expectedSize);

		// Return the entities
		return entities;
	}

	/**
	 * Ensures within rule.
	 * 
	 * @param methodName Name of method attempting to be invoked.
	 */
	private void ensureWithinRule(String methodName) {
		if (this.objectifyFactory == null) {
			throw new IllegalStateException("Must execute " + methodName + " within context of rule");
		}
	}

	/*
	 * ================= TestRule =======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {

		// Ensure start up the local data store
		boolean isStart = false;
		boolean[] isStarted = new boolean[] { false };
		if (dataStore == null) {
			dataStore = LocalDatastoreHelper.create();
			isStart = true;

			// Add shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					synchronized (isStarted) {
						if (isStarted[0]) {
							dataStore.stop();
						}
					}
				} catch (Exception ex) {
					System.err.println("Failed to shutdown " + LocalDatastoreHelper.class.getSimpleName());
					ex.printStackTrace();
				}
			}));
		}

		// Return statement to start application
		boolean finalIsStart = isStart;
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {

				// Easy access to rule
				ObjectifyRule rule = ObjectifyRule.this;

				// Determine if data store failure
				if (dataStoreFailure != null) {
					throw dataStoreFailure;
				}

				// Start DataStore (if required)
				if (finalIsStart) {
					try {
						dataStore.start();

						// Indicated successfully started
						synchronized (isStarted) {
							isStarted[0] = true;
						}

					} catch (IOException ex) {

						// Indicate that needs Internet connection to download emulator
						String message = "ERROR: " + ex.getMessage() + " (" + LocalDatastoreHelper.class.getSimpleName()
								+ " typically requires Internet access to download the emulator)";
						System.err.println(message);
						dataStoreFailure = new IOException(message);
						throw dataStoreFailure;
					}
				}

				// Reset DataStore for test
				dataStore.reset();

				// Initialise Objectify
				rule.objectifyFactory = new ObjectifyFactory(dataStore.getOptions().getService());

				// Run the test (with objectify session)
				try {

					// Initialise service with factory (in case of use before managed object source)
					ObjectifyService.init(rule.objectifyFactory);

					// Flag to use the objectify factory
					ObjectifySupplierSource.setObjectifyFactoryManufacturer(() -> rule.objectifyFactory);

					// Undertake test
					base.evaluate();

				} finally {

					// Clear factory from being used
					ObjectifySupplierSource.setObjectifyFactoryManufacturer(null);

					// Complete all pending futures
					PendingFutures.completeAllPendingFutures();

					// Ensure close possible active stack
					if (rule.closable != null) {
						rule.closable.close();
						rule.closable = null;
					}
				}
			}
		};
	}

}