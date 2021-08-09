/*-
 * #%L
 * OpenAPI
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
