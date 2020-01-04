/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceFactory} triggering failures.
 * 
 * @author Daniel Sagenschneider
 */
public class FailServiceFactory implements ServiceFactory<Class<FailServiceFactory>> {

	/**
	 * Failure to instantiate this.
	 */
	public static Throwable instantiateFailure = null;

	/**
	 * Failure to create the service.
	 */
	public static Throwable createServiceFailure = null;

	/**
	 * Resets for next test.
	 */
	public static void reset() {
		instantiateFailure = null;
		createServiceFailure = null;
	}

	/**
	 * Instantiate (with possible failure).
	 */
	public FailServiceFactory() throws Throwable {
		if (instantiateFailure != null) {
			throw instantiateFailure;
		}
	}

	/*
	 * ================= ServiceFactory ===================
	 */

	@Override
	public Class<FailServiceFactory> createService(ServiceContext context) throws Throwable {

		// Throw possible create error
		if (createServiceFailure != null) {
			throw createServiceFailure;
		}

		// Return successfully
		return FailServiceFactory.class;
	}

}
