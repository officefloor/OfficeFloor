/*-
 * #%L
 * Objectify Persistence
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.nosql.objectify.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
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
 * Abstract JUnit functionality for running {@link Objectify} with local
 * {@link Datastore}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractObjectifyJUnit {

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
	 * Obtains the first entity.
	 * <p>
	 * Note this obtains arbitrary entity. Typically this is useful in tests when
	 * only a single instance of the entity exists. However, if more than one entity
	 * exists, this can cause indeterminate test failures.
	 * <p>
	 * Will retry to obtain the entity or timeout.
	 * 
	 * @param type Type of entity.
	 * @return First entity.
	 * @throws TimeoutException If waited too long for the entity.
	 */
	public <E> E get(Class<E> type) throws TimeoutException {
		return this.get(type, 1, (QueryLoader<E>) null).get(0);
	}

	/**
	 * <p>
	 * Obtains an entity.
	 * <p>
	 * Will retry to obtain the entity or timeout.
	 * 
	 * @param type Type of entity.
	 * @param id   Id of entity.
	 * @return Entity.
	 * @throws TimeoutException If waited too long for the entity.
	 */
	public <E> E get(Class<E> type, long id) throws TimeoutException {
		return this.get(type, (loader) -> loader.id(id));
	}

	/**
	 * Loads the entity.
	 */
	public static interface ResultLoader<E> {
		LoadResult<E> load(LoadType<E> loadType);
	}

	/**
	 * <p>
	 * Obtains an entity.
	 * <p>
	 * Will retry to obtain the entity or timeout.
	 * 
	 * @param type   Type of entity.
	 * @param loader {@link QueryLoader}.
	 * @return Entity.
	 * @throws TimeoutException If waited too long for the entity.
	 */
	public <E> E get(Class<E> type, ResultLoader<E> loader) throws TimeoutException {
		return this.get(type, 1, () -> {
			E entity = loader.load(this.ofy().load().type(type)).now();
			return ((entity == null) ? Collections.emptyList() : Arrays.asList(entity));
		}).get(0);
	}

	/**
	 * Loads the entity.
	 */
	public static interface QueryLoader<E> {
		Query<E> load(LoadType<E> loadType);
	}

	/**
	 * Obtains a list of entities.
	 * 
	 * @param type         Type of entity.
	 * @param expectedSize Expected list size.
	 * @param loader       {@link QueryLoader}.
	 * @return List of entity.
	 * @throws TimeoutException If waited too long the list of entities.
	 */
	public <E> List<E> get(Class<E> type, int expectedSize, QueryLoader<E> loader) throws TimeoutException {
		return this.get(type, expectedSize, () -> {
			LoadType<E> loadType = this.ofy().load().type(type);
			return ((loader != null) ? loader.load(loadType) : loadType).list();
		});
	}

	/**
	 * Obtains a list of entities.
	 * 
	 * @param expectedSize Expected list size.
	 * @param loader       Loads the entities.
	 * @return List of entities.
	 * @throws TimeoutException If waited too long the list of entities.
	 */
	public <E> List<E> get(Class<E> type, int expectedSize, Supplier<List<E>> loader) throws TimeoutException {

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
			entities = loader.get();

		} while (entities.size() < expectedSize);

		// Return the entities
		return entities;
	}

	/**
	 * Obtains consistent entity.
	 * 
	 * @param <E>              Type of entity.
	 * @param getEntity        Obtains the entity.
	 * @param checkConsistency Checks the consistency of the entity.
	 * @return Entity (in consistent state).
	 * @throws TimeoutException If times out waiting to become consistent.
	 */
	public <E> E consistent(Supplier<E> getEntity, Function<E, Boolean> checkConsistency) throws TimeoutException {

		// Ensure within rule
		this.ensureWithinRule("get(...)");

		// Attempt to retrieve entity in consistent state
		long endTime = System.currentTimeMillis() + this.timeout;
		for (;;) {

			// Obtain the entity
			E entity = getEntity.get();

			// Determine if consistent
			if ((entity != null) && (checkConsistency.apply(entity))) {
				return entity;
			}

			// Wait some time
			try {
				Thread.sleep(this.retry);
			} catch (InterruptedException ex) {
				throw new TimeoutException("Interrupted retrieving entity");
			}

			// Determine if timed out
			if (endTime < System.currentTimeMillis()) {
				throw new TimeoutException("Timed out retrieving "
						+ (entity != null ? entity.getClass().getSimpleName() : "entity") + " in consistent state");
			}

			// Clear cache and try again
			this.ofy().clear();
		}
	}

	/**
	 * Stores the entities.
	 * 
	 * @param entities Entities to store.
	 * @throws TimeoutException If waited too long to store the entities in
	 *                          consistent exising state.
	 */
	@SuppressWarnings("unchecked")
	public <E> void store(E... entities) throws TimeoutException {

		// Ensure within rule
		this.ensureWithinRule("store(...)");

		// Save the entities
		Map<Key<E>, E> results = this.ofy().save().entities(entities).now();

		// Ensure all entities are available
		for (Key<E> key : results.keySet()) {
			E entity = results.get(key);

			this.get(entity.getClass(), 1, (loadType) -> loadType.filterKey(key));
		}
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

	/**
	 * Sets up the local data store.
	 * 
	 * @throws Exception If fails to setup.
	 */
	protected void setupLocalDataStore() throws Exception {

		// Ensure start up the local data store
		if (dataStore == null) {
			dataStore = LocalDatastoreHelper.create();

			// Add shutdown hook
			boolean[] isStarted = new boolean[] { false };
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

			// Start DataStore
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

		// Determine if data store failure
		if (dataStoreFailure != null) {
			throw dataStoreFailure;
		}

		// Reset DataStore for test
		dataStore.reset();

		// Initialise Objectify
		this.objectifyFactory = new ObjectifyFactory(dataStore.getOptions().getService());

		// Initialise service with factory (in case of use before managed object source)
		ObjectifyService.init(this.objectifyFactory);

		// Flag to use the objectify factory
		ObjectifySupplierSource.setObjectifyFactoryManufacturer(() -> this.objectifyFactory);
	}

	/**
	 * Tears down the local data store.
	 */
	protected void tearDownLocalDataStore() {

		// Clear factory from being used
		ObjectifySupplierSource.setObjectifyFactoryManufacturer(null);

		// Complete all pending futures
		PendingFutures.completeAllPendingFutures();

		// Ensure close possible active stack
		if (this.closable != null) {
			try {
				this.closable.close();
			} catch (IllegalStateException ex) {
				// best attempt to close (as may already be closed by managed object
			}
			this.closable = null;
		}
	}

}
