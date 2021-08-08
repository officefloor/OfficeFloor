/*-
 * #%L
 * JAX-RS
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
