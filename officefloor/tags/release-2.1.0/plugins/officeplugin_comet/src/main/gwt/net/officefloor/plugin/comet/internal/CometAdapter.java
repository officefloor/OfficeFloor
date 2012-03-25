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

package net.officefloor.plugin.comet.internal;

import net.officefloor.plugin.comet.api.CometSubscriber;

/**
 * <p>
 * Adapter on the {@link CometSubscriber} interface to allow invocation of the
 * method for handling the event.
 * <p>
 * This interface is used for GWT generation and should not be implemented.
 * 
 * @author Daniel Sagenschneider
 */
public interface CometAdapter {

	/**
	 * <p>
	 * Handles the event.
	 * <p>
	 * This method is deliberately generic to enable generation of the handling.
	 * 
	 * @param handler
	 *            Handler to handle the event.
	 * @param event
	 *            Event.
	 */
	void handleEvent(Object handler, Object event);

	/**
	 * Creates the type safe publisher.
	 * 
	 * @param matchKey
	 *            Match key. May be <code>null</code>.
	 * @return Type safe publisher that implements the {@link CometSubscriber}
	 *         interface.
	 */
	Object createPublisher(Object matchKey);

}