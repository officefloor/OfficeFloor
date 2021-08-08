/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.server.http.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.WritableHttpHeader;

/**
 * {@link ProcessState} aware {@link HttpResponseHeaders}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponseHeaders implements HttpResponseHeaders {

	/**
	 * Head {@link WritableHttpHeader} instance.
	 */
	private WritableHttpHeader head = null;

	/**
	 * Tail {@link WritableHttpHeader} instance.
	 */
	private WritableHttpHeader tail = null;

	/**
	 * {@link ManagedObjectContext}.
	 */
	private final ManagedObjectContext context;

	/**
	 * Instantiate.
	 * 
	 * @param context {@link ManagedObjectContext}.
	 */
	public ProcessAwareHttpResponseHeaders(ManagedObjectContext context) {
		this.context = context;
	}

	/**
	 * Obtains the head {@link WritableHttpHeader} to the linked list of
	 * {@link WritableHttpHeader} instances for the {@link HttpResponseWriter}.
	 * 
	 * @return Head {@link WritableHttpHeader} to the linked list of
	 *         {@link WritableHttpHeader} instances for the
	 *         {@link HttpResponseWriter}. May be <code>null</code>.
	 */
	public WritableHttpHeader getWritableHttpHeaders() {
		return this.head;
	}

	/**
	 * Removes the {@link HttpHeader}.
	 * 
	 * @param header {@link WritableHttpHeader} to remove.
	 * @return Previous {@link WritableHttpHeader}. May be <code>null</code> if
	 *         head.
	 */
	private WritableHttpHeader removeHttpHeader(WritableHttpHeader header) {

		// Determine if first
		if (header == this.head) {
			// Drop the first
			this.head = this.head.next;
			if (this.head == null) {
				this.tail = null; // only header
			}
			return null; // removed first (no previous)

		} else {
			// Find previous
			WritableHttpHeader prev = this.head;
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
			return prev; // removed
		}
	}

	/**
	 * Easy access to running {@link ProcessSafeOperation}.
	 * 
	 * @param operation {@link ProcessSafeOperation}.
	 * @return Result of {@link ProcessSafeOperation}.
	 * @throws T Potential {@link Throwable} from {@link ProcessSafeOperation}.
	 */
	private final <R, T extends Throwable> R safe(ProcessSafeOperation<R, T> operation) throws T {
		return this.context.run(operation);
	}

	/**
	 * Safely adds a {@link HttpHeader}.
	 * 
	 * @param headerName  {@link HttpHeaderName}.
	 * @param headerValue {@link HttpHeaderValue}.
	 * @return Added {@link HttpHeader}.
	 */
	private final HttpHeader safeAddHeader(HttpHeaderName headerName, HttpHeaderValue headerValue) {
		return this.safe(() -> {
			WritableHttpHeader header = new WritableHttpHeader(headerName, headerValue);
			if (this.head == null) {
				// First header
				this.head = header;
				this.tail = header;
			} else {
				// Append the header
				this.tail.next = header;
				this.tail = header;
			}
			return header;
		});
	}

	/**
	 * Obtains the {@link Iterator} to all the {@link WritableHttpHeader} instances.
	 * 
	 * @return {@link Iterator} to all the {@link WritableHttpHeader} instances.
	 */
	private Iterator<WritableHttpHeader> getHttpHeaderIterator() {
		return new Iterator<WritableHttpHeader>() {

			WritableHttpHeader current = null;

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
		 * @param unsafeIterator Unsafe {@link Iterator}.
		 */
		private SafeIterator(Iterator<? extends HttpHeader> unsafeIterator) {
			this.unsafeIterator = unsafeIterator;
		}

		/**
		 * Easy access to running {@link ProcessSafeOperation}.
		 * 
		 * @param operation {@link ProcessSafeOperation}.
		 * @return Result of {@link ProcessSafeOperation}.
		 * @throws T Potential {@link Throwable} from {@link ProcessSafeOperation}.
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
		return new SafeIterator(this.getHttpHeaderIterator());
	}

	@Override
	public HttpHeader addHeader(String name, String value) throws IllegalArgumentException {
		return this.safeAddHeader(new HttpHeaderName(name), new HttpHeaderValue(value));
	}

	@Override
	public HttpHeader addHeader(HttpHeaderName name, String value) throws IllegalArgumentException {
		return this.safeAddHeader(name, new HttpHeaderValue(value));
	}

	@Override
	public HttpHeader addHeader(String name, HttpHeaderValue value) throws IllegalArgumentException {
		return this.safeAddHeader(new HttpHeaderName(name), value);
	}

	@Override
	public HttpHeader addHeader(HttpHeaderName name, HttpHeaderValue value) throws IllegalArgumentException {
		return this.safeAddHeader(name, value);
	}

	@Override
	public boolean removeHeader(HttpHeader header) {
		if (!(header instanceof WritableHttpHeader)) {
			return false; // only contains writable headers
		}
		return this.safe(() -> {
			try {
				this.removeHttpHeader((WritableHttpHeader) header);
				return true;
			} catch (NoSuchElementException ex) {
				return false;
			}
		});
	}

	@Override
	public boolean removeHeaders(String name) {
		return this.safe(() -> {
			Iterator<WritableHttpHeader> iterator = this.getHttpHeaderIterator();
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
			Iterator<WritableHttpHeader> iterator = this.getHttpHeaderIterator();
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

			WritableHttpHeader current = null;

			@Override
			public boolean hasNext() {

				// Obtain the next header
				WritableHttpHeader next;
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
				WritableHttpHeader next;
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

}
