/*-
 * #%L
 * OpenAPI
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

package net.officefloor.web.openapi.security;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.jwt.JwtHttpSecuritySource;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySource;

/**
 * Tests the default {@link OpenApiSecurityExtension} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultOpenApiSecurityExtensionTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct {@link HttpSecuritySource} name for basic.
	 */
	public void testBasic() {
		assertEquals("Must match class", BasicHttpSecuritySource.class.getName(),
				BasicOpenApiSecurityExtension.BASIC_HTTP_SECURITY_SOURCE_CLASS_NAME);
	}

	/**
	 * Ensure correct {@link HttpSecuritySource} name for basic.
	 */
	public void testJwt() {
		assertEquals("Must match class", JwtHttpSecuritySource.class.getName(),
				JwtOpenApiSecurityExtension.JWT_HTTP_SECURITY_SOURCE_CLASS_NAME);
	}

}
