/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
	 * Obtains the unique identifier for this {@link PublishedEvent}.
	 * <p>
	 * Identifier is unique per a single {@link CometServiceManagedObjectSource}
	 * . Identifiers may overlap if using multiple
	 * {@link CometServiceManagedObjectSource} instances.
	 * 
	 * @return Unique identifier for this {@link PublishedEvent}.
	 */
	long getEventId();

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
	 * Obtains the listener type.
	 * 
	 * @return Listener type.
	 */
	Class<?> getListenerType();

	/**
	 * Obtains the published event parameter.
	 * 
	 * @return Published event parameter. May be <code>null</code>.
	 */
	Object getEventParameter();

	/**
	 * Obtains the match key.
	 * 
	 * @return Match key. May be <code>null</code> to indicate to not filter.
	 */
	Object getMatchKey();

}