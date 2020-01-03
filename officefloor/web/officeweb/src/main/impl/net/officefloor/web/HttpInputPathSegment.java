/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web;

/**
 * Segment of the {@link HttpInputPath}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpInputPathSegment {

	/**
	 * Types of {@link HttpInputPathSegment}.
	 */
	public static enum HttpInputPathSegmentEnum {
		STATIC, PARAMETER
	}

	/**
	 * {@link HttpInputPathSegmentEnum}.
	 */
	public final HttpInputPathSegmentEnum type;

	/**
	 * Static path or parameter name.
	 */
	public final String value;

	/**
	 * Next {@link HttpInputPathSegment}.
	 */
	public HttpInputPathSegment next = null;

	/**
	 * Instantiate.
	 * 
	 * @param type
	 *            {@link HttpInputPathSegmentEnum}.
	 * @param value
	 *            Static path or parameter name.
	 */
	public HttpInputPathSegment(HttpInputPathSegmentEnum type, String value) {
		this.type = type;
		this.value = value;
	}
}
