/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.comet.internal.CometEvent;
import net.officefloor.plugin.comet.internal.CometRequest;

/**
 * Services the {@link CometRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CometService {

	/**
	 * Services the {@link CometRequest}.
	 */
	void service();

	/**
	 * Publishes an event.
	 * 
	 * @param listenerType
	 *            Listener type name.
	 * @param event
	 *            Event.
	 * @param matchKey
	 *            Match key. May be <code>null</code> to indicate to not filter.
	 * @return Sequence number for the {@link CometEvent}.
	 */
	long publishEvent(String listenerTypeName, Object event, Object matchKey);

	/**
	 * <p>
	 * Published an event allowing to provide the {@link PublishedEvent}
	 * sequence number.
	 * <p>
	 * This is made available for clustered deployments. This allows events to
	 * be published to a central queue with central sequence generation that all
	 * instances in the cluster subscribe to and publish. This then allows load
	 * balancing of {@link CometRequest} instances across the cluster.
	 * 
	 * @param eventSequenceNumber
	 *            {@link PublishedEvent} sequence number.
	 * @param listenerType
	 *            Listener type name.
	 * @param event
	 *            Event.
	 * @param matchKey
	 *            Match key. May be <code>null</code> to indicate to not filter.
	 * @return Sequence number for the {@link CometEvent}. Typically this should
	 *         be the supplied sequence number but there may be occasions for
	 *         the {@link CometService} to assign its own sequence number.
	 */
	long publishEvent(long eventSequenceNumber, String listenerTypeName,
			Object event, Object matchKey);

	/**
	 * <p>
	 * Expires waiting {@link CometRequest} instances and old
	 * {@link PublishedEvent} instances.
	 * <p>
	 * This is typically invoked by the {@link ManagedObjectSource} however is
	 * available to manually trigger.
	 */
	void expire();

}