package net.officefloor.jaxrs;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.hk2.api.Factory;
import org.jvnet.hk2.annotations.Optional;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link ExecutorService} {@link Factory}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExecutorServiceFactory implements Factory<ManagedExecutorService> {

	/**
	 * {@link HttpServletRequest}.
	 */
	private final HttpServletRequest request;

	/**
	 * Instantiate.
	 * 
	 * @param request {@link HttpServletRequest}.
	 */
	@Inject
	public OfficeFloorExecutorServiceFactory(@Optional HttpServletRequest request) {
		this.request = request;
	}

	/*
	 * =============== Factory ===================
	 */

	@Override
	public ManagedExecutorService provide() {
		return new OfficeFloorExecutorService(this.request);
	}

	@Override
	public void dispose(ManagedExecutorService instance) {
		// Nothing to dispose
	}

	/**
	 * {@link OfficeFloor} {@link ExecutorService}.
	 */
	public static class OfficeFloorExecutorService extends AbstractExecutorService implements ManagedExecutorService {

		/**
		 * {@link HttpServletRequest}.
		 */
		private final HttpServletRequest request;

		/**
		 * Instantiate.
		 * 
		 * @param request {@link HttpServletRequest}.
		 */
		public OfficeFloorExecutorService(HttpServletRequest request) {
			this.request = request;
		}

		/*
		 * ================== ExecutorService ==================
		 */

		@Override
		public void execute(Runnable command) {

			// Determine if have request
			if (this.request == null) {
				command.run(); // no async context, so execute immediately
			}

			// Obtain the async context
			AsyncContext asyncContext = this.request.isAsyncStarted() ? this.request.getAsyncContext()
					: request.startAsync();

			// Execute the command
			asyncContext.start(() -> {
				try {
					command.run();
				} catch (Throwable ex) {
					try {
						// Provide error and complete response
						HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
						response.sendError(500);
						asyncContext.complete();
					} catch (Exception ignore) {
						// Best attempt to send response
					}
				}
			});
		}

		@Override
		public void shutdown() {
			// Nothing to shutdown
		}

		@Override
		public List<Runnable> shutdownNow() {
			return Collections.emptyList();
		}

		@Override
		public boolean isShutdown() {
			return false;
		}

		@Override
		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			return true;
		}

		@Override
		public boolean isTerminated() {
			return false;
		}
	}

}