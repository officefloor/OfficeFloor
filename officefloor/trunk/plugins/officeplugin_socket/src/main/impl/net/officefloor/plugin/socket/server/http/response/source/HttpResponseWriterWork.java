/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.http.response.source;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriter;
import net.officefloor.plugin.socket.server.http.response.HttpResponseWriterFactory;

/**
 * {@link HttpResponseWriter} {@link Work}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResponseWriterWork implements
		WorkFactory<HttpResponseWriterWork>, Work {

	/**
	 * {@link HttpResponseWriterFactory}.
	 */
	private final HttpResponseWriterFactory writerFactory;

	/**
	 * Initiate.
	 *
	 * @param writerFactory
	 *            {@link HttpResponseWriterFactory}.
	 */
	public HttpResponseWriterWork(HttpResponseWriterFactory writerFactory) {
		this.writerFactory = writerFactory;
	}

	/**
	 * Obtains the {@link HttpResponseWriterFactory}.
	 *
	 * @return {@link HttpResponseWriterFactory}.
	 */
	public HttpResponseWriterFactory getHttpResponseWriterFactory() {
		return this.writerFactory;
	}

	/*
	 * ================= WorkFactory ============================
	 */

	@Override
	public HttpResponseWriterWork createWork() {
		return this;
	}

}