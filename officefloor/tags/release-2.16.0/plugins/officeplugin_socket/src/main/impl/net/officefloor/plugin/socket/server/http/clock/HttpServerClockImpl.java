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
package net.officefloor.plugin.socket.server.http.clock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;

/**
 * {@link HttpServerClock} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class HttpServerClockImpl implements HttpServerClockSource,
		HttpServerClock {

	/**
	 * Formatter for the date header value.
	 */
	private final SimpleDateFormat dateFormat;

	/**
	 * {@link CachedDate} to reduce calculations of date header value.
	 */
	private CachedDate cachedDate;

	/**
	 * Initiate.
	 */
	public HttpServerClockImpl() {
		// Setup the date formatter at startup for performance
		this.dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
				Locale.US);
		this.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/*
	 * ======================== HttpServerClockSource ========================
	 */

	@Override
	public HttpServerClock createHttpServerClock(
			MetaDataContext<None, Indexed> configurationContext)
			throws Exception {
		return this;
	}

	/*
	 * =========================== HttpServerClock ===========================
	 */

	@Override
	public String getDateHeaderValue() {

		// Obtain the time
		long time = System.currentTimeMillis();

		// Obtain the seconds
		long seconds = (long) (time / 1000);

		// Determine if have cached
		CachedDate cachedDate = this.cachedDate;
		if ((cachedDate == null) || (cachedDate.seconds != seconds)) {
			// Generate new date (as no/invalid cached date)
			String dateHeaderValue = this.dateFormat.format(new Date(time));

			// Cache the date header value
			cachedDate = new CachedDate(seconds, dateHeaderValue);
			this.cachedDate = cachedDate;

		}

		// Provide the date header value
		return cachedDate.dateHeaderValue;
	}

	/**
	 * Cached date value.
	 */
	private static class CachedDate {

		/**
		 * Time in seconds from epoch for date header value.
		 */
		private final long seconds;

		/**
		 * Cached date header value.
		 */
		private final String dateHeaderValue;

		/**
		 * Initiate.
		 * 
		 * @param seconds
		 *            Time in seconds from epoch for date header value.
		 * @param dateHeaderValue
		 *            Cached date header value.
		 */
		public CachedDate(long seconds, String dateHeaderValue) {
			this.seconds = seconds;
			this.dateHeaderValue = dateHeaderValue;
		}
	}

}