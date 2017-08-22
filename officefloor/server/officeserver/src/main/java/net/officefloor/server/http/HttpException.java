/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http;

/**
 * <p>
 * HTTP {@link Exception}.
 * <p>
 * This is a {@link RuntimeException} as typically this is handled directly by
 * the {@link HttpServerImplementation} to send a {@link HttpResponse}. It is
 * typically not for application logic to handle.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpException extends RuntimeException {

	/**
	 * No {@link HttpHeader} instances value.
	 */
	private static final HttpHeader[] NO_HEADERS = new HttpHeader[0];

	/**
	 * {@link HttpStatus}.
	 */
	private final HttpStatus status;

	/**
	 * Possible {@link HttpHeader} instances for the {@link HttpResponse}.
	 */
	private final HttpHeader[] headers;

	/**
	 * {@link HttpResponse} entity content.
	 */
	private final String entity;

	/**
	 * Instantiate.
	 * 
	 * @param status
	 *            {@link HttpStatus}.
	 */
	public HttpException(HttpStatus status) {
		super(status.getStatusMessage());
		this.status = status;
		this.headers = NO_HEADERS;
		this.entity = null;
	}

	/**
	 * Instantiate.
	 * 
	 * @param status
	 *            {@link HttpStatus}.
	 * @param headers
	 *            {@link HttpHeader} instances. May be <code>null</code>.
	 * @param entity
	 *            Entity for the {@link HttpResponse}. May be <code>null</code>.
	 */
	public HttpException(HttpStatus status, HttpHeader[] headers, String entity) {
		super(status.getStatusMessage());
		this.status = status;
		this.headers = (headers == null ? NO_HEADERS : headers);
		this.entity = entity;
	}

	/**
	 * Obtains the {@link HttpStatus} for the {@link HttpResponse}.
	 * 
	 * @return {@link HttpStatus} for the {@link HttpResponse}.
	 */
	public HttpStatus getHttpStatus() {
		return this.status;
	}

	/**
	 * Obtains the {@link HttpHeader} instances.
	 * 
	 * @return {@link HttpHeader} instances.
	 */
	public HttpHeader[] getHttpHeaders() {
		return this.headers;
	}

	/**
	 * Obtains the entity for the {@link HttpResponse}.
	 * 
	 * @return Entity for the {@link HttpResponse}. May be <code>null</code> for
	 *         no entity.
	 */
	public String getEntity() {
		return this.entity;
	}

}