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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;

/**
 * {@link Serializable} {@link HttpResponseHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponseHeaders implements HttpResponseHeaders {

	/**
	 * {@link WritableHttpHeader} instances.
	 */
	private final List<WritableHttpHeader> headers = new ArrayList<>(16);

	/**
	 * {@link ProcessAwareContext}.
	 */
	private final ProcessAwareContext context;

	/**
	 * Instantiate.
	 * 
	 * @param context
	 *            {@link ProcessAwareContext}.
	 */
	public ProcessAwareHttpResponseHeaders(ProcessAwareContext context) {
		this.context = context;
	}

	/**
	 * Obtains the {@link WritableHttpHeader} instances for the
	 * {@link HttpResponseWriter}.
	 * 
	 * @return {@link WritableHttpHeader} instances for the
	 *         {@link HttpResponseWriter}.
	 */
	public List<WritableHttpHeader> getWritableHttpHeaders() {
		return this.headers;
	}

	/**
	 * Easy access to running {@link ProcessSafeOperation}.
	 * 
	 * @param operation
	 *            {@link ProcessSafeOperation}.
	 * @return Result of {@link ProcessSafeOperation}.
	 * @throws T
	 *             Potential {@link Throwable} from
	 *             {@link ProcessSafeOperation}.
	 */
	private final <R, T extends Throwable> R safe(ProcessSafeOperation<R, T> operation) throws T {
		return ProcessAwareHttpResponseHeaders.this.context.run(operation);
	}

	/**
	 * Safely adds a {@link HttpHeader}.
	 * 
	 * @param name
	 *            Name of {@link HttpHeader}.
	 * @param headerName
	 *            Optional {@link HttpHeaderName}.
	 * @param value
	 *            Value of {@link HttpHeader}.
	 * @param headerValue
	 *            Optional {@link HttpHeaderValue}.
	 * @return Added {@link HttpHeader}.
	 */
	private final HttpHeader safeAddHeader(String name, HttpHeaderName headerName, String value,
			HttpHeaderValue headerValue) {
		return this.safe(() -> {
			WritableHttpHeader header = new WritableHttpHeaderImpl(name, headerName, value, headerValue);
			this.headers.add(header);
			return header;
		});
	}

	/**
	 * Provides {@link ProcessSafeOperation} wrapping of {@link Iterator}.
	 */
	private class SafeIterator implements Iterator<HttpHeader> {

		/**
		 * Unsafe {@link Iterator}.
		 */
		private final Iterator<HttpHeader> unsafeIterator;

		/**
		 * Instantiate.
		 * 
		 * @param unsafeIterator
		 *            Unsafe {@link Iterable}.
		 */
		private SafeIterator(Iterator<HttpHeader> unsafeIterator) {
			this.unsafeIterator = unsafeIterator;
		}

		/**
		 * Easy access to running {@link ProcessSafeOperation}.
		 * 
		 * @param operation
		 *            {@link ProcessSafeOperation}.
		 * @return Result of {@link ProcessSafeOperation}.
		 * @throws T
		 *             Potential {@link Throwable} from
		 *             {@link ProcessSafeOperation}.
		 */
		private final <R, T extends Throwable> R safe(ProcessSafeOperation<R, T> operation) throws T {
			return ProcessAwareHttpResponseHeaders.this.safe(operation);
		}

		/*
		 * =============== Iterator ===============
		 */

		@Override
		public boolean hasNext() {
			return this.safe(() -> this.unsafeIterator.hasNext());
		}

		@Override
		public HttpHeader next() {
			return this.safe(() -> this.unsafeIterator.next());
		}

		@Override
		public void remove() {
			this.safe(() -> {
				this.unsafeIterator.remove();
				return null; // void return
			});
		}

		@Override
		public void forEachRemaining(Consumer<? super HttpHeader> action) {
			this.safe(() -> {
				this.unsafeIterator.forEachRemaining(action);
				return null; // void return
			});
		}
	}

	/*
	 * ====================== HttpResponseHeaders ========================
	 */

	@Override
	public Iterator<HttpHeader> iterator() {
		return new SafeIterator(new Iterator<HttpHeader>() {

			List<? extends HttpHeader> headers = ProcessAwareHttpResponseHeaders.this.headers;

			int position = 0;

			@Override
			public boolean hasNext() {
				return this.position < this.headers.size();
			}

			@Override
			public HttpHeader next() {

				// Ensure another header
				if (this.position >= this.headers.size()) {
					throw new NoSuchElementException();
				}

				// Return the next header
				HttpHeader header = this.headers.get(this.position);
				this.position++; // increment for next
				return header;
			}

			@Override
			public void remove() {
				// Remove current header (previous to next header)
				this.headers.remove(--this.position);
			}
		});
	}

	@Override
	public HttpHeader addHeader(String name, String value) throws IllegalArgumentException {
		return this.safeAddHeader(name, null, value, null);
	}

	@Override
	public HttpHeader addHeader(HttpHeaderName name, String value) throws IllegalArgumentException {
		return this.safeAddHeader(name.getName(), name, value, null);
	}

	@Override
	public HttpHeader addHeader(String name, HttpHeaderValue value) throws IllegalArgumentException {
		return this.safeAddHeader(name, null, value.getValue(), value);
	}

	@Override
	public HttpHeader addHeader(HttpHeaderName name, HttpHeaderValue value) throws IllegalArgumentException {
		return this.safeAddHeader(name.getName(), name, value.getValue(), value);
	}

	@Override
	public boolean removeHeader(HttpHeader header) {
		return this.safe(() -> this.headers.remove(header));
	}

	@Override
	public boolean removeHeaders(String name) {
		return this.safe(() -> {
			Iterator<WritableHttpHeader> iterator = this.headers.iterator();
			boolean isRemoved = false;
			while (iterator.hasNext()) {
				if (name.equalsIgnoreCase(iterator.next().getName())) {
					iterator.remove();
					isRemoved = true;
				}
			}
			return isRemoved;
		});
	}

	@Override
	public HttpHeader getHeader(String name) {
		return this.safe(() -> {
			for (HttpHeader header : this.headers) {
				if (name.equalsIgnoreCase(header.getName())) {
					return header;
				}
			}
			return null; // not found
		});
	}

	@Override
	public Iterable<HttpHeader> getHeaders(String name) {
		return () -> new SafeIterator(new Iterator<HttpHeader>() {

			List<? extends HttpHeader> headers = ProcessAwareHttpResponseHeaders.this.headers;

			int position = 0;

			@Override
			public boolean hasNext() {

				// Determine if further values
				for (int i = this.position; i < this.headers.size(); i++) {
					if (name.equalsIgnoreCase(this.headers.get(i).getName())) {
						return true;
					}
				}
				return false; // no further headers by name
			}

			@Override
			public HttpHeader next() {

				// Move to next position
				for (; this.position < this.headers.size(); this.position++) {
					HttpHeader header = this.headers.get(this.position);
					if (name.equalsIgnoreCase(header.getName())) {
						this.position++; // increment for next
						return header; // found next header
					}
				}
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				// Remove current header (previous to next header)
				this.headers.remove(--this.position);
			}
		});
	}

	@Override
	public HttpHeader headerAt(int index) {
		return this.safe(() -> this.headers.get(index));
	}

	@Override
	public int length() {
		return this.safe(() -> this.headers.size());
	}

	/**
	 * {@link WritableHttpHeader} implementation.
	 */
	private static class WritableHttpHeaderImpl implements WritableHttpHeader {

		/**
		 * : then space encoded bytes.
		 */
		private static byte[] COLON_SPACE = ": ".getBytes(ServerHttpConnection.HTTP_CHARSET);

		/**
		 * {@link HttpHeader} end of line encoded bytes.
		 */
		private static byte[] HEADER_EOLN = "\r\n".getBytes(ServerHttpConnection.HTTP_CHARSET);

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Optional {@link HttpHeaderName}.
		 */
		private final HttpHeaderName headerName;

		/**
		 * Value.
		 */
		private final String value;

		/**
		 * Optional {@link HttpHeaderValue}.
		 */
		private final HttpHeaderValue headerValue;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            {@link HttpHeader} name.
		 * @param headerName
		 *            Optional {@link HttpHeaderName}. May be <code>null</code>.
		 * @param value
		 *            {@link HttpHeader} value.
		 * @param headerValue
		 *            Optional {@link HttpHeaderValue}. May be
		 *            <code>null</code>.
		 */
		public WritableHttpHeaderImpl(String name, HttpHeaderName headerName, String value,
				HttpHeaderValue headerValue) {
			this.name = name;
			this.headerName = headerName;
			this.value = value;
			this.headerValue = headerValue;
		}

		/*
		 * ================= WritableHttpHeader ==================
		 */

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getValue() {
			return this.value;
		}

		@Override
		public void writeHttpHeader(ServerWriter writer) throws IOException {

			// Write the header name
			if (this.headerName != null) {
				this.headerName.writeName(writer);
			} else {
				writer.write(this.name);
			}

			// Write the colon and space
			writer.write(COLON_SPACE);

			// Write the header value
			if (this.headerValue != null) {
				this.headerValue.writeValue(writer);
			} else {
				writer.write(this.value);
			}

			// Write the end of header line
			writer.write(HEADER_EOLN);
		}
	}

}