package net.officefloor.app.subscription.rule;

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
import com.googlecode.objectify.util.Closeable;

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
				ObjectifyService.init(new ObjectifyFactory(dataStore.getOptions().getService()));
				for (Class<?> entity : ObjectifyRule.this.entities) {
					ObjectifyService.register(entity);
				}

				// Run the test (with objectify session)
				Closeable closable = ObjectifyService.begin();
				try {
					base.evaluate();
				} finally {
					PendingFutures.completeAllPendingFutures();
					closable.close();
				}
			}
		};
	}

}