package net.officefloor.app.subscription.rule;

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
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.cache.PendingFutures;
import com.googlecode.objectify.cmd.LoadType;
import com.googlecode.objectify.cmd.Query;
import com.googlecode.objectify.util.Closeable;

import net.officefloor.app.subscription.store.ObjectifyManagedObjectSource;

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
	 * {@link Entity} instances to register.
	 */
	private final Class<?>[] entities;

	/**
	 * Timeout in obtaining entities.
	 */
	private long timeout = 3 * 1000;

	/**
	 * Time to retry the obtaining the entities.
	 */
	private long retry = 10;

	/**
	 * Initialise with {@link Entity} instances to register.
	 * 
	 * @param entities {@link Entity} instances to register.
	 */
	public ObjectifyRule(Class<?>... entities) {
		this.entities = entities;
	}

	/**
	 * Obtains the {@link Objectify}.
	 * 
	 * @return {@link Objectify}.
	 */
	public Objectify ofy() {
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

	/*
	 * ================= TestRule =======================
	 */

	@Override
	public Statement apply(Statement base, Description description) {

		// Ensure start up the local data store
		boolean isStart = false;
		if (dataStore == null) {
			dataStore = LocalDatastoreHelper.create();
			isStart = true;

			// Add shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					dataStore.stop();
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

				// Start DataStore (if required)
				if (finalIsStart) {
					dataStore.start();
				}

				// Reset DataStore for test
				dataStore.reset();

				// Initialise Objectify
				ObjectifyFactory factory = new ObjectifyFactory(dataStore.getOptions().getService());
				ObjectifyService.init(factory);
				for (Class<?> entity : ObjectifyRule.this.entities) {
					ObjectifyService.register(entity);
				}

				// Run the test (with objectify session)
				Closeable closable = ObjectifyService.begin();
				try {
					ObjectifyManagedObjectSource.setObjectifyFactoryManufacturer(() -> factory);
					base.evaluate();
				} finally {
					ObjectifyManagedObjectSource.setObjectifyFactoryManufacturer(null);
					PendingFutures.completeAllPendingFutures();
					closable.close();
				}
			}
		};
	}

}