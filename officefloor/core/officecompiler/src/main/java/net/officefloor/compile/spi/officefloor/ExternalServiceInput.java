/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.spi.officefloor;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * External service input to {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExternalServiceInput<O> {

	/**
	 * Invoked by an external service to use {@link OfficeFloor}.
	 * 
	 * @param object
	 *            Object made available as a {@link ManagedObject} for
	 *            dependency injection into {@link ManagedFunction} instances.
	 * @param callback
	 *            {@link FlowCallback} to indicate servicing complete.
	 */
	void service(O object, FlowCallback callback);

}