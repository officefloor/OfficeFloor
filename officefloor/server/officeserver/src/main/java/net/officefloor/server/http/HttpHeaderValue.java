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

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import net.officefloor.server.stream.ServerWriter;

/**
 * <p>
 * Provides formatting of values for {@link HttpHeader} values.
 * <p>
 * Also provides means for common {@link HttpHeader} values in already encoded
 * HTTP bytes for faster writing.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHeaderValue {

	/**
	 * {@link HttpHeader} value formatter of a {@link Date}.
	 */
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
			.withZone(ZoneOffset.UTC);

	/**
	 * Obtains the HTTP value for an Integer.
	 * 
	 * @param value
	 *            Integer value.
	 * @return HTTP value for the Integer.
	 */
	public static String getIntegerValue(int value) {
		return String.valueOf(value);
	}

	/**
	 * Obtains the HTTP value for a {@link Date}.
	 * 
	 * @param value
	 *            {@link Date} value.
	 * @return HTTP value for the {@link Date}.
	 */
	public static String getDateValue(Date value) {
		return dateFormatter.format(value.toInstant());
	}

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Pre-encoded bytes of value ready for HTTP output.
	 */
	private final byte[] encodedValue;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            {@link HttpHeaderValue}.
	 */
	public HttpHeaderValue(String value) {
		this.value = value;
		this.encodedValue = this.value.getBytes(ServerHttpConnection.HTTP_CHARSET);
	}

	/**
	 * Instantiate with integer.
	 * 
	 * @param value
	 *            Integer.
	 */
	public HttpHeaderValue(int value) {
		this(getIntegerValue(value));
	}

	/**
	 * Instantiate with date.
	 * 
	 * @param value
	 *            Date.
	 */
	public HttpHeaderValue(Date value) {
		this(getDateValue(value));
	}

	/**
	 * Obtains the value.
	 * 
	 * @return value.
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Writes the value to the {@link ServerWriter}.
	 * 
	 * @param writer
	 *            {@link ServerWriter}.
	 * @throws IOException
	 *             If fails to write the name.
	 */
	public void writeValue(ServerWriter writer) throws IOException {
		writer.write(this.encodedValue);
	}

}