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
package net.officefloor.web.security.scheme;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.spi.security.HttpCredentials;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecurityContext;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.impl.AbstractHttpSecuritySource;

/**
 * Mock {@link HttpSecuritySource} that challenges with a HTML form.
 * 
 * @author Daniel Sagenschneider
 */
public class MockFormHttpSecuritySource extends
		AbstractHttpSecuritySource<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, MockFormHttpSecuritySource.MockFormFlows> {

	/**
	 * {@link Flow} keys.
	 */
	public static enum MockFormFlows {
		CHALLENGE
	}

	/*
	 * =================== HttpSecuritySource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(
			MetaDataContext<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, MockFormFlows> context)
			throws Exception {
	}

	@Override
	public HttpSecurity<HttpAuthentication<HttpCredentials>, HttpAccessControl, HttpCredentials, None, MockFormFlows> sourceHttpSecurity(
			HttpSecurityContext context) throws HttpException {
		// TODO Auto-generated method stub
		return null;
	}

}