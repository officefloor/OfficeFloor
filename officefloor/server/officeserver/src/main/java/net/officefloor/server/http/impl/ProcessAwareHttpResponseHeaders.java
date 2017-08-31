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
import java.util.Iterator;
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
	 * Head {@link WritableHttpHeaderImpl} instance.
	 */
	private WritableHttpHeaderImpl head = null;

	/**
	 * Tail {@link WritableHttpHeaderImpl} instance.
	 */
	private WritableHttpHeaderImpl tail = null;

	/**
	 * Count of the number of headers.
	 */
	private int headerCount = 0;

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
	public Iterator<WritableHttpHeader> getWritableHttpHeaders() {
		return new Iterator<WritableHttpHeader>() {

			WritableHttpHeaderImpl current = null;

			@Override
			public boolean hasNext() {
				return (this.current == null ? (ProcessAwareHttpResponseHeaders.this.head != null)
						: (this.current.next != null));
			}

			@Override
			public WritableHttpHeader next() {

				// Determine if first
				if (this.current == null) {
					this.current = ProcessAwareHttpResponseHeaders.this.head;
					if (this.current == null) {
						throw new NoSuchElementException();
					}
					return this.current;
				}

				// Obtain next (ensuring exists)
				if (this.current.next == null) {
					throw new NoSuchElementException();
				}
				this.current = this.current.next;
				return this.current;
			}

			@Override
			public void remove() {
				this.current = ProcessAwareHttpResponseHeaders.this.removeHttpHeader(this.current);
			}
		};
	}

	/**
	 * Removes the {@link HttpHeader}.
	 * 
	 * @param header
	 *            {@link WritableHttpHeaderImpl} to remove.
	 * @return Previous {@link WritableHttpHeaderImpl}. May be <code>null</code>
	 *         if head.
	 */
	private WritableHttpHeaderImpl removeHttpHeader(WritableHttpHeaderImpl header) {

		// Determine if first
		if (header == this.head) {
			// Drop the first
			this.head = this.head.next;
			if (this.head == null) {
				this.tail = null; // only header
			}
			this.headerCount--;
			return null; // removed first (no previous)

		} else {
			// Find previous
			WritableHttpHeaderImpl prev = this.head;
			while (prev.next != header) {
				prev = prev.next;
				if (prev == null) {
					throw new NoSuchElementException();
				}
			}

			// Drop the current (moving out of linked list)
			prev.next = header.next;
			if (prev.next == null) {
				// Removed last, so update list
				this.tail = prev;
			}
			this.headerCount--;
			return prev; // removed
		}
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
			WritableHttpHeaderImpl header = new WritableHttpHeaderImpl(name, headerName, value, headerValue);
			if (this.head == null) {
				// First header
				this.head = header;
				this.tail = header;
			} else {
				// Append the header
				this.tail.next = header;
				this.tail = header;
			}
			this.headerCount++;
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
		private final Iterator<? extends HttpHeader> unsafeIterator;

		/**
		 * Instantiate.
		 * 
		 * @param unsafeIterator
		 *            Unsafe {@link Iterator}.
		 */
		private SafeIterator(Iterator<? extends HttpHeader> unsafeIterator) {
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
		return new SafeIterator(this.getWritableHttpHeaders());
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
		return this.safe(() -> {
			try {
				this.removeHttpHeader((WritableHttpHeaderImpl) header);
				return true;
			} catch (NoSuchElementException ex) {
				return false;
			}
		});
	}

	@Override
	public boolean removeHeaders(String name) {
		return this.safe(() -> {
			Iterator<WritableHttpHeader> iterator = this.getWritableHttpHeaders();
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
			Iterator<WritableHttpHeader> iterator = this.getWritableHttpHeaders();
			while (iterator.hasNext()) {
				WritableHttpHeader header = iterator.next();
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

			WritableHttpHeaderImpl current = null;

			@Override
			public boolean hasNext() {

				// Obtain the next header
				WritableHttpHeaderImpl next;
				if (this.current == null) {
					// First header
					next = ProcessAwareHttpResponseHeaders.this.head;
				} else {
					next = this.current.next;
				}

				// Determine if further values
				while (next != null) {
					if (name.equalsIgnoreCase(next.getName())) {
						return true;
					}
					next = next.next;
				}
				return false; // no further headers by name
			}

			@Override
			public HttpHeader next() {

				// Obtain the next header
				WritableHttpHeaderImpl next;
				if (this.current == null) {
					// First header
					next = ProcessAwareHttpResponseHeaders.this.head;
				} else {
					next = this.current.next;
				}

				// Move to next position
				while (next != null) {
					if (name.equalsIgnoreCase(next.getName())) {
						// Found next value
						this.current = next;
						return this.current;
					}
					next = next.next;
				}

				// As here, no next header by name
				throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				this.current = ProcessAwareHttpResponseHeaders.this.removeHttpHeader(this.current);
			}
		});
	}

	@Override
	public HttpHeader headerAt(int index) {
		return this.safe(() -> {
			WritableHttpHeaderImpl header = this.head;
			for (int i = 0; i < index; i++) {
				header = header.next;
			}
			return header;
		});
	}

	@Override
	public int length() {
		return this.safe(() -> this.headerCount);
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
		 * Next {@link WritableHttpHeaderImpl}.
		 */
		private WritableHttpHeaderImpl next = null;

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