/*-
 * #%L
 * JWT Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
