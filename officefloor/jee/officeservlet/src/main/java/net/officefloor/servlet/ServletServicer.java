/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet;

import java.util.Map;
import java.util.concurrent.Executor;

import javax.servlet.AsyncContext;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Services {@link ServerHttpConnection} via {@link Servlet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServletServicer {

	/**
	 * Services the {@link ServerHttpConnection}.
	 * 
	 * @param connection                 {@link ServerHttpConnection}.
	 * @param executor                   {@link Executor}.
	 * @param asynchronousFlow           {@link AsynchronousFlow} to allow for
	 *                                   {@link AsyncContext}.
	 * @param asynchronousFlowCompletion {@link AsynchronousFlowCompletion} for
	 *                                   handling completion of servicing.
	 * @param attributes                 Optional attributes to load to the
	 *                                   {@link HttpServletRequest}. May be
	 *                                   <code>null</code>.
	 * @throws Exception If fails to service.
	 */
	void service(ServerHttpConnection connection, Executor executor, AsynchronousFlow asynchronousFlow,
			AsynchronousFlowCompletion asynchronousFlowCompletion, Map<String, ? extends Object> attributes)
			throws Exception;

}
