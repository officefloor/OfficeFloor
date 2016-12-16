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
package net.officefloor.frame.spi.managedobject.source;

/**
 * Critical section.
 *
 * @author Daniel Sagenschneider
 */
public interface CriticalSection<R, S, E extends Throwable> {

	/**
	 * Undertakes the {@link CriticalSection}.
	 * 
	 * @param state
	 *            Optional state for the {@link CriticalSection}.
	 * @return Enables a value to be returned from the {@link CriticalSection}.
	 * @throws E
	 *             Should there be a failure in the {@link CriticalSection}.
	 */
	R doCriticalSection(S state) throws E;

}