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

import org.junit.Assert;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * Missing {@link ServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MissingServiceFactory implements ServiceFactory<Object> {

	/**
	 * Obtains the issue message.
	 * 
	 * @return Issue message.
	 */
	public static String getIssueDescription() {
		return "No services configured for " + MissingServiceFactory.class.getName();
	}

	/*
	 * =============== ServiceFactory =====================
	 */

	@Override
	public Throwable createService(ServiceContext context) throws Throwable {
		Assert.fail("Should not be creating service");
		return null;
	}

}