package net.officefloor.jaxrs;

import java.io.Writer;
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
import org.glassfish.jersey.process.internal.RequestContext;
import org.glassfish.jersey.process.internal.RequestScope;

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
	 * {@link RequestScope}.
	 */
	private final RequestScope scope;

	/**
	 * Instantiate.
	 * 
	 * @param request {@link HttpServletRequest}.
	 * @param scope   {@link RequestScope}.
	 */
	@Inject
	public OfficeFloorExecutorServiceFactory(HttpServletRequest request, RequestScope scope) {
		this.request = request;
		this.scope = scope;
	}

	/*
	 * =============== Factory ===================
	 */

	@Override
	public ManagedExecutorService provide() {
		return new OfficeFloorExecutorService();
	}

	@Override
	public void dispose(ManagedExecutorService instance) {
		// Nothing to dispose
	}

	/**
	 * {@link OfficeFloor} {@link ExecutorService}.
	 */
	public class OfficeFloorExecutorService extends AbstractExecutorService implements ManagedExecutorService {

		/*
		 * ================== ExecutorService ==================
		 */

		@Override
		public void execute(Runnable command) {

			// Easy access to factory
			OfficeFloorExecutorServiceFactory factory = OfficeFloorExecutorServiceFactory.this;

			// Suspend the request
			RequestContext requestContext = factory.scope.suspendCurrent();

			// Obtain the async context
			AsyncContext asyncContext = factory.request.isAsyncStarted() ? factory.request.getAsyncContext()
					: request.startAsync();

			// Execute the command
			asyncContext.start(() -> {
				try {

					// Undertake command
					factory.scope.runInScope(requestContext, command);
					requestContext.release();

				} catch (Throwable ex) {

					// Failure not handled, so fail request
					HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
					try {

						// Load exception to response
						response.reset();
						response.setStatus(500);
						Writer writer = response.getWriter();
						writer.write("Command failed running in " + ExecutorService.class.getSimpleName() + " with "
								+ ex.getClass().getName() + ": " + ex.getMessage());

					} catch (Exception ignore) {
						// Best attempt to provide content of response
					} finally {
						// Ensure complete response
						asyncContext.complete();
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