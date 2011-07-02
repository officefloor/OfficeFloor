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
package net.officefloor.plugin.comet.internal;

import java.util.Map;

import net.officefloor.plugin.comet.api.CometSubscriber;

/**
 * <p>
 * Provides the mapping of {@link CometSubscriber} interface type to its
 * {@link CometListenerAdapter}.
 * <p>
 * This interface is used for GWT generation and should not be implemented.
 * 
 * @author Daniel Sagenschneider
 */
public interface CometListenerMap {

	/**
	 * Creates the mapping of {@link CometSubscriber} interface type to its
	 * {@link CometListenerAdapter}.
	 * 
	 * @return Mapping of {@link CometSubscriber} interface type to its
	 *         {@link CometListenerAdapter}.
	 */
	Map<Class<?>, CometListenerAdapter> getMap();

}