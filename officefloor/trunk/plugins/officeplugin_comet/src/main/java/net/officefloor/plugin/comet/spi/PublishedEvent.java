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
package net.officefloor.plugin.comet.spi;

/**
 * Published Comet event.
 * 
 * @author Daniel Sagenschneider
 */
public interface PublishedEvent {

	/**
	 * <p>
	 * Obtains the unique sequence number for this {@link PublishedEvent}.
	 * <p>
	 * Sequence numbers are used to not send the same {@link PublishedEvent} to
	 * the client again. In other words, the client provides the last sequence
	 * number received and only {@link PublishedEvent} instances after this
	 * sequence number are sent.
	 * <p>
	 * Sequence numbers are unique per a single
	 * {@link CometServiceManagedObjectSource}. Sequence numbers may overlap if
	 * using multiple {@link CometServiceManagedObjectSource} instances.
	 * 
	 * @return Unique sequence number for this {@link PublishedEvent}.
	 */
	long getEventSequenceNumber();

	/**
	 * Obtains the time this {@link PublishedEvent} was published.
	 * 
	 * @return Time in milliseconds since EPOC of when this
	 *         {@link PublishedEvent} was published.
	 */
	long getPublishTimestamp();

	/**
	 * Obtains the next {@link PublishedEvent} within the list of published
	 * events.
	 * 
	 * @return Next {@link PublishedEvent} or <code>null</code> if no further
	 *         published events.
	 */
	PublishedEvent getNextEvent();

	/**
	 * Obtains the listener type name.
	 * 
	 * @return Listener type name.
	 */
	String getListenerTypeName();

	/**
	 * Obtains the data of the event.
	 * 
	 * @return Published event data. May be <code>null</code>.
	 */
	Object getData();

	/**
	 * Obtains the match key.
	 * 
	 * @return Match key. May be <code>null</code>.
	 */
	Object getMatchKey();

}