/*-
 * #%L
 * JAX-RS
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
import org.glassfish.jersey.process.internal.RequestContext;
import org.glassfish.jersey.process.internal.RequestScope;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.servlet.supply.ServletSupplierSource;

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

					try {
						// Undertake command
						factory.scope.runInScope(requestContext, command);

					} finally {
						// Ensure release context
						requestContext.release();
					}

				} catch (Throwable ex) {

					// Obtain the request / response
					HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
					HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();

					try {
						// Load exception to response
						response.reset();
						ServletSupplierSource.sendError(ex, request, response);

					} catch (Exception ignore) {
						// Ensure failure
						response.setStatus(500);
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
