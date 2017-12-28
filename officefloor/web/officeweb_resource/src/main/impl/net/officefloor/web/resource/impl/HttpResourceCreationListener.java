/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.web.resource.impl;

import java.io.IOException;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.resource.HttpResource;

/**
 * Listener for the creation of a {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResourceCreationListener<F extends Enum<F>> {

	/**
	 * <p>
	 * Notified on the creation of a {@link HttpResource}.
	 * <p>
	 * Note that the {@link HttpResource} may not actually exist. Therefore
	 * check the {@link HttpResource#isExist()} should you need to ensure the
	 * {@link HttpResource} actually exists.
	 * 
	 * @param httpResource
	 *            {@link HttpResource} created.
	 * @param connection
	 *            {@link ServerHttpConnection} requesting the
	 *            {@link HttpResource}.
	 * @param context
	 *            {@link ManagedFunctionContext}.
	 * @throws IOException
	 *             If fails to handle {@link HttpResource} found.
	 */
	void httpResourceCreated(HttpResource httpResource, ServerHttpConnection connection,
			ManagedFunctionContext<?, F> context) throws IOException;

}