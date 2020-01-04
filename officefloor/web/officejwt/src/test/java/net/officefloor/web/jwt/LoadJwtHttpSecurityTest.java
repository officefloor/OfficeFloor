/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.jwt.role.JwtRoleCollector;
import net.officefloor.web.jwt.validate.JwtValidateKeyCollector;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;

/**
 * Tests the {@link JwtHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("unchecked")
public class LoadJwtHttpSecurityTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(JwtHttpSecuritySource.class,
				JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS, "Claims Class");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setAuthenticationClass(HttpAuthentication.class);
		type.setAccessControlClass(JwtHttpAccessControl.class);
		type.setInput(true);
		type.addFlow(JwtHttpSecuritySource.Flows.RETRIEVE_KEYS, JwtValidateKeyCollector.class);
		type.addFlow(JwtHttpSecuritySource.Flows.RETRIEVE_ROLES, JwtRoleCollector.class);
		type.addFlow(JwtHttpSecuritySource.Flows.NO_JWT, null);
		type.addFlow(JwtHttpSecuritySource.Flows.INVALID_JWT, null);
		type.addFlow(JwtHttpSecuritySource.Flows.EXPIRED_JWT, null);

		// Validate the type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, JwtHttpSecuritySource.class,
				JwtHttpSecuritySource.PROPERTY_CLAIMS_CLASS, Object.class.getName());
	}

}
