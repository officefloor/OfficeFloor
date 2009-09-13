/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

/**
 * {@link HttpResponseWriter} {@link Work}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResponseWriterWork implements
		WorkFactory<HttpResponseWriterWork>, Work {

	/**
	 * {@link HttpResponseWriter}.
	 */
	private final HttpResponseWriter writer;

	/**
	 * Initiate.
	 *
	 * @param writer
	 *            {@link HttpResponseWriter}.
	 */
	public HttpResponseWriterWork(HttpResponseWriter writer) {
		this.writer = writer;
	}

	/**
	 * Obtains the {@link HttpResponseWriter}.
	 *
	 * @return {@link HttpResponseWriter}.
	 */
	public HttpResponseWriter getHttpResponseWriter() {
		return this.writer;
	}

	/*
	 * ================= WorkFactory ============================
	 */

	@Override
	public HttpResponseWriterWork createWork() {
		return this;
	}

}