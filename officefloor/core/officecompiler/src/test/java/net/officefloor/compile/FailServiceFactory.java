/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceFactory} that fails to create the service.
 * 
 * @author Daniel Sagenschneider
 */
public class FailServiceFactory implements ServiceFactory<Throwable> {

	/**
	 * Failure.
	 */
	private static final Throwable failure = new Throwable("TEST");

	/**
	 * Obtains the error issue description.
	 * 
	 * @return Error issue description.
	 */
	public static String getIssueDescription() {
		return "Failed to create service from " + FailServiceFactory.class.getName();
	}

	/**
	 * Obtains the failure thrown on creating the service.
	 * 
	 * @return Failure thrown on creating the service.
	 */
	public static Throwable getCause() {
		return failure;
	}

	/*
	 * =================== ServiceFactory ==================
	 */

	@Override
	public Throwable createService(ServiceContext context) throws Throwable {
		throw failure;
	}

}