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
package net.officefloor.server.http.parse.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.conversation.HttpEntity;
import net.officefloor.server.http.conversation.impl.HttpEntityImpl;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.parse.HttpRequestParser;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ServerInputStreamImpl;

/**
 * {@link HttpRequestParser} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestParserImpl implements HttpRequestParser {

	/*
	 * ================= HttpRequestParser ======================
	 */

	@Override
	public boolean parse(ByteBuffer data) throws IOException, HttpRequestParseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFinishedParsingBuffer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public Supplier<HttpMethod> getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Supplier<String> getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpVersion getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NonMaterialisedHttpHeaders getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ByteSequence getEntity() {
		// TODO Auto-generated method stub
		return null;
	}

}