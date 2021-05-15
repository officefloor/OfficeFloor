package net.officefloor.vertx;

import java.util.concurrent.TimeoutException;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Utility functions for working with {@link Vertx} within {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorVertx {

	/**
	 * System property name to configure the {@link Vertx} time outs for starting /
	 * stopping.
	 */
	public static final String SYSTEM_PROPERTY_VERTX_TIMEOUT = "officefloor.vertx.timeout";

	/**
	 * {@link Vertx} start / stop timeout.
	 */
	private static final long VERTX_TIMEOUT = Long.getLong(SYSTEM_PROPERTY_VERTX_TIMEOUT, 10 * 1000);

	/**
	 * Singleton {@link Vertx}.
	 */
	private static Vertx vertx;

	/**
	 * Obtains the singleton {@link Vertx}.
	 * 
	 * @return Singleton {@link Vertx}.
	 */
	public synchronized static Vertx getVertx() {
		if (vertx == null) {
			vertx = Vertx.vertx();
		}
		return vertx;
	}

	/**
	 * Specifies the singleton {@link Vertx}.
	 * 
	 * @param vertx Singleton {@link Vertx}.
	 */
	public synchronized static void setVertx(Vertx vertx) {
		OfficeFloorVertx.vertx = vertx;
	}

	/**
	 * Blocks on the {@link Vertx} {@link Future}
	 * 
	 * @param <T>    Result of {@link Vertx} operation.
	 * @param future {@link Future} to block on.
	 * @return Result of {@link Vertx} {@link Future}.
	 * @throws Exception If {@link Future} fails or times out.
	 */
	public static <T> T block(Future<T> future) throws Exception {

		boolean[] isComplete = new boolean[] { false };
		@SuppressWarnings("unchecked")
		T[] returnValue = (T[]) new Object[] { null };
		Throwable[] failure = new Throwable[] { null };

		// Trigger operation
		future.onComplete((result) -> {
			synchronized (isComplete) {
				try {
					if (result.succeeded()) {
						returnValue[0] = result.result();
					} else {
						failure[0] = result.cause();
					}
				} finally {
					isComplete[0] = true;
					isComplete.notifyAll();
				}
			}
		});

		// Wait until complete, fails or times out
		long endTime = System.currentTimeMillis() + VERTX_TIMEOUT;
		synchronized (isComplete) {
			for (;;) {

				// Determine if complete
				if (isComplete[0]) {

					// Determine if failure
					Throwable cause = failure[0];
					if (cause != null) {
						if (cause instanceof Exception) {
							throw (Exception) cause;
						} else if (cause instanceof Error) {
							throw (Error) cause;
						} else {
							throw new OfficeFloorVertxException(cause);
						}
					}

					// No failure, so provide result
					return returnValue[0];
				}

				// Determine if timed out
				if (System.currentTimeMillis() > endTime) {
					throw new TimeoutException("Vertx operation took too long (" + VERTX_TIMEOUT + " milliseconds)");
				}

				// Wait some time for completion
				isComplete.wait(10);
			}
		}
	}

	/**
	 * All access via static methods.
	 */
	private OfficeFloorVertx() {
		// All access via static methods
	}

}