package net.officefloor.nosql.cosmosdb;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.ConflictResolutionPolicy;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosDatabaseProperties;
import com.azure.cosmos.models.CosmosResponse;
import com.azure.cosmos.models.ThroughputProperties;

import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;

/**
 * Utility function for Cosmos DB.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosDbUtil {

	/**
	 * Maximum number of retries.
	 */
	private static int MAX_RETRIES = 3;

	/**
	 * Operation that can be retried.
	 */
	@FunctionalInterface
	public static interface CosmosOperation<R, E extends Throwable> {
		R run() throws E;
	}

	/**
	 * Adapter to create structure.
	 */
	private static interface StructureCreationAdapter<P, S, R> {

		/**
		 * Creates the structure.
		 * 
		 * @param parent  Parent.
		 * @param request Request.
		 * @return {@link CosmosResponse}.
		 */
		CosmosResponse<?> createStructureIfNotExists(P parent, R request);

		/**
		 * Initialises the structure.
		 * 
		 * @param structure Structure.
		 */
		void init(S structure);

		/**
		 * Obtains the structure's Id.
		 * 
		 * @param request Request.
		 * @return Structure's Id.
		 */
		String getId(R request);

		/**
		 * Obtains the structure.
		 * 
		 * @param parent      Parent.
		 * @param structureId Id of the structure.
		 * @return Structure.
		 */
		S getStructure(P parent, String structureId);

		/**
		 * Reads the structure.
		 * 
		 * @param structure Structure.
		 * @return {@link CosmosResponse} for read..
		 */
		CosmosResponse<?> read(S structure);
	}

	/**
	 * Creates the {@link CosmosDatabase} instances ensuring they are available.
	 * 
	 * @param client            {@link CosmosClient}.
	 * @param databases         {@link CosmosDatabaseProperties} instances of the
	 *                          {@link CosmosDatabase} instances to crate.
	 * @param waitTimeInSeconds Time to wait in seconds for {@link CosmosDatabase}
	 *                          creation.
	 * @param logger            {@link Logger} to log progress.
	 * @param logLevel          {@link Level} for logging.
	 * @throws Exception If fails to create the {@link CosmosDatabase} instances.
	 */
	public static void createDatabases(CosmosClient client, List<CosmosDatabaseProperties> databases,
			int waitTimeInSeconds, Logger logger, Level logLevel) throws Exception {
		createStructures("database", client, databases,
				new StructureCreationAdapter<CosmosClient, CosmosDatabase, CosmosDatabaseProperties>() {

					@Override
					public CosmosResponse<?> createStructureIfNotExists(CosmosClient parent,
							CosmosDatabaseProperties request) {
						return parent.createDatabaseIfNotExists(request.getId());
					}

					@Override
					public void init(CosmosDatabase structure) {
						// Nothing to initialise
					}

					@Override
					public String getId(CosmosDatabaseProperties request) {
						return request.getId();
					}

					@Override
					public CosmosDatabase getStructure(CosmosClient parent, String structureId) {
						return parent.getDatabase(structureId);
					}

					@Override
					public CosmosResponse<?> read(CosmosDatabase structure) {
						return structure.read();
					}
				}, waitTimeInSeconds, logger, logLevel);
	}

	/**
	 * Creates the {@link CosmosAsyncDatabase} instances ensuring they are
	 * available.
	 * 
	 * @param client            {@link CosmosAsyncClient}.
	 * @param databases         {@link CosmosDatabaseProperties} instances of the
	 *                          {@link CosmosAsyncDatabase} instances to crate.
	 * @param waitTimeInSeconds Time to wait in seconds for
	 *                          {@link CosmosAsyncDatabase} creation.
	 * @param logger            {@link Logger} to log progress.
	 * @param logLevel          {@link Level} for logging.
	 * @throws Exception If fails to create the {@link CosmosAsyncDatabase}
	 *                   instances.
	 */
	public static void createAsyncDatabases(CosmosAsyncClient client, List<CosmosDatabaseProperties> databases,
			int waitTimeInSeconds, Logger logger, Level logLevel) throws Exception {
		createStructures("database", client, databases,
				new StructureCreationAdapter<CosmosAsyncClient, CosmosAsyncDatabase, CosmosDatabaseProperties>() {

					@Override
					public CosmosResponse<?> createStructureIfNotExists(CosmosAsyncClient parent,
							CosmosDatabaseProperties request) {
						return parent.createDatabaseIfNotExists(request.getId()).block();
					}

					@Override
					public void init(CosmosAsyncDatabase structure) {
						// Nothing to initialise
					}

					@Override
					public String getId(CosmosDatabaseProperties request) {
						return request.getId();
					}

					@Override
					public CosmosAsyncDatabase getStructure(CosmosAsyncClient parent, String structureId) {
						return parent.getDatabase(structureId);
					}

					@Override
					public CosmosResponse<?> read(CosmosAsyncDatabase structure) {
						return structure.read().block();
					}
				}, waitTimeInSeconds, logger, logLevel);
	}

	/**
	 * Creates the {@link CosmosContainer} instances ensuring they are available.
	 * 
	 * @param database          {@link CosmosDatabase}.
	 * @param containers        {@link CosmosContainerProperties} instances of the
	 *                          {@link CosmosContainer} instances to create.
	 * @param waitTimeInSeconds Time to wait in seconds for {@link CosmosContainer}
	 *                          creation.
	 * @param logger            {@link Logger} to log progress.
	 * @param logLevel          {@link Level} for logging.
	 * @throws Exception If fails to create the {@link CosmosContainer} instances.
	 */
	public static void createContainers(CosmosDatabase database, List<CosmosContainerProperties> containers,
			int waitTimeInSeconds, Logger logger, Level logLevel) throws Exception {
		createStructures("container", database, containers,
				new StructureCreationAdapter<CosmosDatabase, CosmosContainer, CosmosContainerProperties>() {

					@Override
					public CosmosResponse<?> createStructureIfNotExists(CosmosDatabase parent,
							CosmosContainerProperties request) {
						loadContainerDefaults(request);
						return parent.createContainerIfNotExists(request,
								ThroughputProperties.createManualThroughput(400));
					}

					@Override
					public void init(CosmosContainer structure) {
						structure.openConnectionsAndInitCaches();
					}

					@Override
					public String getId(CosmosContainerProperties request) {
						return request.getId();
					}

					@Override
					public CosmosContainer getStructure(CosmosDatabase parent, String structureId) {
						return parent.getContainer(structureId);
					}

					@Override
					public CosmosResponse<?> read(CosmosContainer structure) {
						return structure.read();
					}
				}, waitTimeInSeconds, logger, logLevel);
	}

	/**
	 * Creates the {@link CosmosAsyncContainer} instances ensuring they are
	 * available.
	 * 
	 * @param database          {@link CosmosAsyncDatabase}.
	 * @param containers        {@link CosmosContainerProperties} instances of the
	 *                          {@link CosmosAsyncContainer} instances to create.
	 * @param waitTimeInSeconds Time to wait in seconds for
	 *                          {@link CosmosAsyncContainer} creation.
	 * @param logger            {@link Logger} to log progress.
	 * @param logLevel          {@link Level} for logging.
	 * @throws Exception If fails to create the {@link CosmosAsyncContainer}
	 *                   instances.
	 */
	public static void createAsyncContainers(CosmosAsyncDatabase database, List<CosmosContainerProperties> containers,
			int waitTimeInSeconds, Logger logger, Level logLevel) throws Exception {
		createStructures("container", database, containers,
				new StructureCreationAdapter<CosmosAsyncDatabase, CosmosAsyncContainer, CosmosContainerProperties>() {

					@Override
					public CosmosResponse<?> createStructureIfNotExists(CosmosAsyncDatabase parent,
							CosmosContainerProperties request) {
						loadContainerDefaults(request);
						return parent
								.createContainerIfNotExists(request, ThroughputProperties.createManualThroughput(400))
								.block();
					}

					@Override
					public void init(CosmosAsyncContainer structure) {
						structure.openConnectionsAndInitCaches().block();
					}

					@Override
					public String getId(CosmosContainerProperties request) {
						return request.getId();
					}

					@Override
					public CosmosAsyncContainer getStructure(CosmosAsyncDatabase parent, String structureId) {
						return parent.getContainer(structureId);
					}

					@Override
					public CosmosResponse<?> read(CosmosAsyncContainer structure) {
						return structure.read().block();
					}
				}, waitTimeInSeconds, logger, logLevel);
	}

	private static void loadContainerDefaults(CosmosContainerProperties container) {
		if (container.getConflictResolutionPolicy() == null) {
			container.setConflictResolutionPolicy(ConflictResolutionPolicy.createLastWriterWinsPolicy());
		}
	}

	/**
	 * Generic creation of {@link CosmosContainer} / {@link CosmosAsyncContainer}
	 * instances.
	 * 
	 * @param <D>               {@link CosmosDatabase} /
	 *                          {@link CosmosAsyncDatabase}.
	 * @param <C>               {@link CosmosContainer} /
	 *                          {@link CosmosAsyncContainer}.
	 * @param database          {@link CosmosDatabase} /
	 *                          {@link CosmosAsyncDatabase}.
	 * @param creation          {@link ContainerCreationAdapter}.
	 * @param waitTimeInSeconds Time to wait in seconds for {@link CosmosContainer}
	 *                          / {@link CosmosAsyncContainer} creation.
	 * @param logger            {@link Logger} to log progress.
	 * @param logLevel          {@link Level} for logging.
	 * @param containers        {@link CosmosContainerProperties} instances of the
	 *                          {@link CosmosContainer} /
	 *                          {@link CosmosAsyncContainer} instances to create.
	 * @throws Exception If fails to create the {@link CosmosContainer} /
	 *                   {@link CosmosAsyncContainer} instances.
	 */
	private static <P, S, R> void createStructures(String structureTypeName, P parent, List<R> structureRequests,
			StructureCreationAdapter<P, S, R> creation, int waitTimeInSeconds, Logger logger, Level logLevel)
			throws Exception {

		// Trigger creating the structures
		for (R request : structureRequests) {
			String structureId = creation.getId(request);

			// Create the structure
			AtomicInteger attempt = new AtomicInteger(0);
			ignoreConflict(() -> retry(() -> {
				int attemptIndex = attempt.incrementAndGet();
				if (logger != null) {
					logger.log(logLevel, "Ensuring " + structureTypeName + " " + structureId + " created"
							+ (attemptIndex == 1 ? "" : " (attempt " + attemptIndex + ")"));
				}
				return creation.createStructureIfNotExists(parent, request);
			}));
		}

		// Wait until each structure is available
		NEXT_STRUCTURE: for (R request : structureRequests) {
			String structureId = creation.getId(request);
			S structure = creation.getStructure(parent, structureId);
			CosmosException lastException = null;
			long endTime = System.currentTimeMillis() + (waitTimeInSeconds * 1000);
			do {
				// Check if container is ready
				int status;
				int subStatus = -1;
				try {
					CosmosResponse<?> response = creation.read(structure);
					status = response.getStatusCode();
				} catch (CosmosException ex) {
					status = ex.getStatusCode();
					subStatus = ex.getSubStatusCode();
					lastException = ex;
				}
				if (logger != null) {
					logger.log(logLevel, "Checking " + structureTypeName + " " + structureId + " has status " + status
							+ (subStatus < 0 ? "" : ", subStatus " + subStatus));
				}

				// Determine if available
				if ((status == 200) || (status == 201)) {
					// Structure available
					if (logger != null) {
						logger.log(logLevel, "Created " + structureTypeName + " " + structureId + " available");
					}
					continue NEXT_STRUCTURE;

				} else if ((status == 404) && (subStatus == 1013)) {
					// Structure still being created, so wait some time
					if (logger != null) {
						logger.log(logLevel, "Created " + structureTypeName + " " + structureId + " still creating");
					}
					Thread.sleep(100);

				} else {
					// Propagate the failure
					String message = "Failed creating " + structureTypeName + " " + structureId;
					Exception failure = new Exception(message, lastException);
					if (logger != null) {
						logger.log(logLevel, failure.getMessage(), failure);
					}
					throw failure;
				}

			} while (endTime < System.currentTimeMillis());
			throw new Exception("Took too long (" + waitTimeInSeconds + " seconds) waiting for " + structureTypeName
					+ " " + structureId + " to be available", lastException);
		}

		// Initialise the structures
		for (R request : structureRequests) {
			String structureId = creation.getId(request);

			// Initialise structure
			S structure = creation.getStructure(parent, structureId);
			try {
				creation.init(structure);
			} catch (Exception ex) {
				// Log and carry on
				if (logger != null) {
					logger.log(logLevel, "Failed initilising " + structureTypeName + " " + structureId, ex);
				}
			}
		}
	}

	/**
	 * Ignores {@link NotFoundException} (when deleting).
	 * 
	 * @param <R>       Result.
	 * @param <E>       Possible {@link Exception} thrown.
	 * @param operation {@link CosmosOperation}.
	 * @return Result of {@link CosmosException}.
	 * @throws E Failure of {@link CosmosOperation}.
	 */
	public static <R, E extends Throwable> R ignoreNotFound(CosmosOperation<R, E> operation) throws E {
		try {
			return operation.run();
		} catch (NotFoundException ex) {
			return null; // ignore not found
		} catch (CosmosException ex) {
			if (ex.getStatusCode() == 404) {
				return null; // ignore not found
			} else {
				throw ex; // propagate
			}
		}
	}

	/**
	 * <p>
	 * Ignores conflict.
	 * <p>
	 * Typically on create to ignore if already container by name.
	 * 
	 * @param <R>       Result.
	 * @param <E>       Possible {@link Exception} thrown.
	 * @param operation {@link CosmosOperation}.
	 * @return Result of {@link CosmosOperation}.
	 * @throws E Failure of {@link CosmosOperation}.
	 */
	public static <R, E extends Throwable> R ignoreConflict(CosmosOperation<R, E> operation) throws E {
		try {
			return operation.run();
		} catch (CosmosException ex) {
			if (ex.getStatusCode() == 409) {
				return null; // ignore conflict
			}
			throw ex; // propagate
		}
	}

	/**
	 * Undertakes the {@link CosmosOperation} with appropriate retrying.
	 * 
	 * @param <R>       Result.
	 * @param <E>       Possible {@link Exception} thrown.
	 * @param operation {@link CosmosOperation}.
	 * @return Result of {@link CosmosOperation}.
	 * @throws E Failure of {@link CosmosOperation}.
	 */
	@SuppressWarnings("unchecked")
	public static <R, E extends Throwable> R retry(CosmosOperation<R, E> operation) throws E {
		Throwable failure = null;
		for (int i = 0; i < MAX_RETRIES; i++) {
			try {
				return operation.run();
			} catch (CosmosException ex) {
				failure = ex;

				// Determine if can retry
				if (!isRetriable(ex)) {
					throw (E) ex;
				}
			}
		}

		// No further retrying
		throw (E) failure;
	}

	/**
	 * Undertakes retry for asynchronous.
	 * 
	 * @return {@link Retry}.
	 */
	public static Retry retry() {
		return Retry.from((fluxRetrySignal) -> fluxRetrySignal.map((retrySignal) -> {

			// Determine if further retries
			if ((retrySignal.totalRetries() < MAX_RETRIES) && (isRetriable(retrySignal.failure()))) {

				// Retry
				return retrySignal.totalRetries();
			}

			// Not retriable
			throw Exceptions.propagate(retrySignal.failure());
		}));
	}

	/**
	 * Determines if failure is retriable.
	 * 
	 * @param failure Failure.
	 * @return <code>true</code> if retriable.
	 */
	private static boolean isRetriable(Throwable failure) {
		if (failure instanceof CosmosException) {
			CosmosException cosmosEx = (CosmosException) failure;

			// Handle status that can be retried
			switch (cosmosEx.getStatusCode()) {
			case 408:
			case 503:
				// Can retry status
				return true;

			default:
				// Not a status to retry
				break;
			}

			// Determine if timeout
			Throwable cause = cosmosEx.getCause();
			if ((cause != null) && (cause instanceof ReadTimeoutException)) {
				return true;
			}
		}

		// Not retriable
		return false;
	}

	/**
	 * All access via static methods.
	 */
	private CosmosDbUtil() {
	}

}