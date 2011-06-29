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
 * {@link PublishedEvent} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class PublishedEventImpl implements PublishedEvent {

	/**
	 * Listener type.
	 */
	private final Class<?> listenerType;

	/**
	 * Event parameter.
	 */
	private final Object eventParameter;

	/**
	 * Match key to be checked against the filter key.
	 */
	private final Object matchKey;

	/**
	 * Publish time stamp.
	 */
	private final long publishTimestamp;

	/**
	 * Next {@link PublishedEvent}.
	 */
	private PublishedEvent nextEvent = null;

	/**
	 * Initiate.
	 * 
	 * @param listenerType
	 *            Listener type.
	 * @param eventParameter
	 *            Event parameter.
	 * @param matchKey
	 *            Match key.
	 * @param publishTimestamp
	 *            Publish time stamp.
	 */
	public PublishedEventImpl(Class<?> listenerType, Object eventParameter,
			Object matchKey, long publishTimestamp) {
		this.listenerType = listenerType;
		this.eventParameter = eventParameter;
		this.matchKey = matchKey;
		this.publishTimestamp = publishTimestamp;
	}

	/**
	 * Specifies the next {@link PublishedEvent}.
	 * 
	 * @param event
	 *            {@link PublishedEvent}.
	 */
	void setNextEvent(PublishedEvent event) {
		this.nextEvent = event;
	}

	/*
	 * ================= CometEvent ========================
	 */

	@Override
	public long getEventId() {
		// TODO implement CometEvent.getEventId
		throw new UnsupportedOperationException(
				"TODO implement CometEvent.getEventId");
	}

	@Override
	public long getPublishTimestamp() {
		return this.publishTimestamp;
	}

	@Override
	public PublishedEvent getNextEvent() {
		return this.nextEvent;
	}

	@Override
	public Class<?> getListenerType() {
		return this.listenerType;
	}

	@Override
	public Object getEventParameter() {
		return this.eventParameter;
	}

	@Override
	public Object getMatchKey() {
		return this.matchKey;
	}

}