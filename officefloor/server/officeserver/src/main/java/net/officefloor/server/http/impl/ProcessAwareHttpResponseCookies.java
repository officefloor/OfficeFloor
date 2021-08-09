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
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpResponseCookie;
import net.officefloor.server.http.HttpResponseCookies;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.WritableHttpCookie;

/**
 * {@link ProcessState} aware {@link HttpResponseCookies}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareHttpResponseCookies implements HttpResponseCookies {

	/**
	 * Head {@link WritableHttpCookie} instance.
	 */
	private WritableHttpCookie head = null;

	/**
	 * Tail {@link WritableHttpCookie} instance.
	 */
	private WritableHttpCookie tail = null;

	/**
	 * {@link ManagedObjectContext}.
	 */
	private final ManagedObjectContext context;

	/**
	 * Instantiate.
	 * 
	 * @param context {@link ManagedObjectContext}.
	 */
	public ProcessAwareHttpResponseCookies(ManagedObjectContext context) {
		this.context = context;
	}

	/**
	 * Obtains the head {@link WritableHttpCookie} to the linked list of
	 * {@link WritableHttpCookie} instances for the {@link HttpResponseWriter}.
	 * 
	 * @return Head {@link WritableHttpCookie} to the linked list of
	 *         {@link WritableHttpCookie} instances for the
	 *         {@link HttpResponseWriter}. May be <code>null</code>.
	 */
	public WritableHttpCookie getWritableHttpCookie() {
		return this.head;
	}

	/**
	 * Removes the {@link WritableHttpCookie}.
	 * 
	 * @param cookie {@link WritableHttpCookie} to remove.
	 * @return Previous {@link WritableHttpCookie}. May be <code>null</code> if
	 *         head.
	 */
	private WritableHttpCookie removeHttpCookie(WritableHttpCookie cookie) {

		// Determine if first
		if (cookie == this.head) {
			// Drop the first
			this.head = this.head.next;
			if (this.head == null) {
				this.tail = null; // only header
			}
			return null; // removed first (no previous)

		} else {
			// Find previous
			WritableHttpCookie prev = this.head;
			while (prev.next != cookie) {
				prev = prev.next;
				if (prev == null) {
					throw new NoSuchElementException();
				}
			}

			// Drop the current (moving out of linked list)
			prev.next = cookie.next;
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
	 * Safely adds a {@link HttpResponseCookie}.
	 * 
	 * @param name        Name of {@link HttpResponseCookie}.
	 * @param value       Value of {@link HttpResponseCookie}.
	 * @param initialiser Optional {@link Consumer} to initialise the
	 *                    {@link HttpResponseCookie}. May be <code>null</code>.
	 * @return Added {@link HttpResponseCookie}.
	 */
	private final HttpResponseCookie safeSetCookie(String name, String value,
			Consumer<HttpResponseCookie> initialiser) {
		return this.safe(() -> {

			// Determine if cookie already exists
			WritableHttpCookie cookie = this.head;
			FOUND_COOKIE: while (cookie != null) {
				if (name.equals(cookie.getName())) {
					// Found the cookie
					cookie.setValue(value);
					break FOUND_COOKIE;
				}
				cookie = cookie.next;
			}

			// Add cookie if not already added
			if (cookie == null) {
				cookie = new WritableHttpCookie(name, value, this.context);
				if (this.head == null) {
					// First cookie
					this.head = cookie;
					this.tail = cookie;
				} else {
					// Append the cookie
					this.tail.next = cookie;
					this.tail = cookie;
				}
			}

			// Initialise the cookie
			if (initialiser != null) {
				initialiser.accept(cookie);
			}

			// Return the cookie
			return cookie;
		});
	}

	/**
	 * Obtains the {@link Iterator} to all the {@link WritableHttpCookie} instances.
	 * 
	 * @return {@link Iterator} to all the {@link WritableHttpCookie} instances.
	 */
	private Iterator<WritableHttpCookie> getHttpCookieIterator() {
		return new Iterator<WritableHttpCookie>() {

			WritableHttpCookie current = null;

			@Override
			public boolean hasNext() {
				return (this.current == null ? (ProcessAwareHttpResponseCookies.this.head != null)
						: (this.current.next != null));
			}

			@Override
			public WritableHttpCookie next() {

				// Determine if first
				if (this.current == null) {
					this.current = ProcessAwareHttpResponseCookies.this.head;
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
				this.current = ProcessAwareHttpResponseCookies.this.removeHttpCookie(this.current);
			}
		};
	}

	/**
	 * Provides {@link ProcessSafeOperation} wrapping of {@link Iterator}.
	 */
	private class SafeIterator implements Iterator<HttpResponseCookie> {

		/**
		 * Unsafe {@link Iterator}.
		 */
		private final Iterator<? extends HttpResponseCookie> unsafeIterator;

		/**
		 * Instantiate.
		 * 
		 * @param unsafeIterator Unsafe {@link Iterator}.
		 */
		private SafeIterator(Iterator<? extends HttpResponseCookie> unsafeIterator) {
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
			return ProcessAwareHttpResponseCookies.this.safe(operation);
		}

		/*
		 * =============== Iterator ===============
		 */

		@Override
		public boolean hasNext() {
			return this.safe(() -> this.unsafeIterator.hasNext());
		}

		@Override
		public HttpResponseCookie next() {
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
		public void forEachRemaining(Consumer<? super HttpResponseCookie> action) {
			this.safe(() -> {
				this.unsafeIterator.forEachRemaining(action);
				return null; // void return
			});
		}
	}

	/*
	 * ================= HttpResponseCookies =======================
	 */

	@Override
	public Iterator<HttpResponseCookie> iterator() {
		return new SafeIterator(this.getHttpCookieIterator());
	}

	@Override
	public HttpResponseCookie setCookie(String name, String value) {
		return this.safeSetCookie(name, value, null);
	}

	@Override
	public HttpResponseCookie setCookie(String name, String value, Consumer<HttpResponseCookie> initialiser) {
		return this.safeSetCookie(name, value, initialiser);
	}

	@Override
	public HttpResponseCookie setCookie(HttpRequestCookie cookie) {
		return this.safeSetCookie(cookie.getName(), cookie.getValue(), null);
	}

	@Override
	public HttpResponseCookie setCookie(HttpRequestCookie cookie, Consumer<HttpResponseCookie> initialiser) {
		return this.safeSetCookie(cookie.getName(), cookie.getValue(), initialiser);
	}

	@Override
	public boolean removeCookie(HttpResponseCookie cookie) {
		if (!(cookie instanceof WritableHttpCookie)) {
			return false; // only contains writable cookies
		}
		return this.safe(() -> {
			try {
				this.removeHttpCookie((WritableHttpCookie) cookie);
				return true;
			} catch (NoSuchElementException ex) {
				return false;
			}
		});
	}

	@Override
	public HttpResponseCookie getCookie(String name) {
		return this.safe(() -> {
			Iterator<WritableHttpCookie> iterator = this.getHttpCookieIterator();
			while (iterator.hasNext()) {
				WritableHttpCookie cookie = iterator.next();
				if (name.equalsIgnoreCase(cookie.getName())) {
					return cookie;
				}
			}
			return null; // not found
		});
	}

}
