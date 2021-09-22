package net.officefloor.nosql.cosmosdb;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.NotFoundException;

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