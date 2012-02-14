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

package net.officefloor.plugin.comet;

import net.officefloor.plugin.comet.api.CometSubscriber;
import net.officefloor.plugin.comet.api.OfficeFloorComet;

/**
 * Creates the Comet publishers for {@link OfficeFloorComet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CometPublisher {

	/**
	 * Creates the publisher.
	 * 
	 * @param listenerType
	 *            Listener interface type that should be marked by extending
	 *            {@link CometSubscriber}.
	 * @param matchKey
	 *            Key to match events. The {@link Object#equals(Object)} is used
	 *            to match event meta-data to determine filtering. This may be
	 *            <code>null</code> to not be filtered.
	 * @return {@link CometSubscriber} to publish events.
	 */
	<L extends CometSubscriber> L createPublisher(Class<L> listenerType,
			Object matchKey);

}