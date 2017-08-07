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
package net.officefloor.server.http.impl;

import java.io.IOException;
import java.nio.charset.Charset;

import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;

/**
 * {@link HttpResponse} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseImpl implements HttpResponse {

	@Override
	public HttpVersion getHttpVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHttpVersion(HttpVersion version) {
		// TODO Auto-generated method stub

	}

	@Override
	public HttpStatus getHttpStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHttpStatus(HttpStatus status) {
		// TODO Auto-generated method stub

	}

	@Override
	public HttpResponseHeaders getHttpHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setContentType(String contentType, Charset charset) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Charset getContentCharset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerOutputStream getEntity() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerWriter getEntityWriter() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void send() throws IOException {
		// TODO Auto-generated method stub

	}

}