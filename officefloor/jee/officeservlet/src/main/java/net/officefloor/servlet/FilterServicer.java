/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet;

import java.util.concurrent.Executor;

import javax.servlet.AsyncContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Services {@link ServerHttpConnection} via {@link Filter}.
 * 
 * @author Daniel Sagenschneider
 */
public interface FilterServicer {

	/**
	 * Services the {@link ServerHttpConnection}.
	 * 
	 * @param connection                 {@link ServerHttpConnection}.
	 * @param executor                   {@link Executor}.
	 * @param asynchronousFlow           {@link AsynchronousFlow} to allow for
	 *                                   {@link AsyncContext}.
	 * @param asynchronousFlowCompletion {@link AsynchronousFlowCompletion}.
	 * @param chain                      {@link FilterChain}.
	 * @throws Exception If fails to service.
	 */
	void service(ServerHttpConnection connection, Executor executor, AsynchronousFlow asynchronousFlow,
			AsynchronousFlowCompletion asynchronousFlowCompletion, FilterChain chain) throws Exception;

}
