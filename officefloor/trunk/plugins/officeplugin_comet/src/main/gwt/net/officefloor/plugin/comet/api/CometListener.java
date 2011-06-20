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
package net.officefloor.plugin.comet.api;

/**
 * <p>
 * Marker interface that all Comet Listener interfaces should extend.
 * <p>
 * The extending interface must having the following criteria:
 * <ol>
 * <li>only one method</li>
 * <li>the method should have only one parameter (which specifies type of event)
 * </li>
 * <li>the method should not throw any exceptions</li>
 * </ol>
 * 
 * @author Daniel Sagenschneider
 */
public interface CometListener {
}