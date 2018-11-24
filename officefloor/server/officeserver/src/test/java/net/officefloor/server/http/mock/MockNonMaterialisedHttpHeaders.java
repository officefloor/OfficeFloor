/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http.mock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;

/**
 * Mock {@link NonMaterialisedHttpHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockNonMaterialisedHttpHeaders implements NonMaterialisedHttpHeaders {

	/**
	 * {@link NonMaterialisedHttpHeader} instances.
	 */
	private final List<NonMaterialisedHttpHeader> headers = new LinkedList<>();

	/**
	 * Adds a {@link NonMaterialisedHttpHeader}.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public void addHttpHeader(String name, String value) {
		this.headers.add(new MockNonMaterialisedHttpHeader(name, value));
	}

	/*
	 * =================== NonMaterialisedHttpHeaders ===============
	 */

	@Override
	public Iterator<NonMaterialisedHttpHeader> iterator() {
		return this.headers.iterator();
	}

	@Override
	public int length() {
		return this.headers.size();
	}

	/**
	 * Mock {@link NonMaterialisedHttpHeader}.
	 */
	private static class MockNonMaterialisedHttpHeader implements NonMaterialisedHttpHeader, HttpHeader {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Value.
		 */
		private final String value;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            Name.
		 * @param value
		 *            Value.
		 */
		public MockNonMaterialisedHttpHeader(String name, String value) {
			this.name = name;
			this.value = value;
		}

		/*
		 * ======= NonMaterialisedHttpHeader / HttpHeader ============
		 */

		@Override
		public HttpHeader materialiseHttpHeader() {
			return this;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getValue() {
			return this.value;
		}

	}

}