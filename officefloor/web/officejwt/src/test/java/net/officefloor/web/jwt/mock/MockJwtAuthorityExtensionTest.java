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

package net.officefloor.web.jwt.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Tests the {@link MockJwtAccessTokenExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockJwtAuthorityExtensionTest extends AbstractMockJwtAuthorityTestCase {

	/**
	 * {@link Extension} under test.
	 */
	@RegisterExtension
	public final MockJwtAccessTokenExtension mockToken = new MockJwtAccessTokenExtension();

	/*
	 * ==================== AbstractMockJwtAuthorityTestCase ====================
	 */

	@Override
	protected AbstractMockJwtAccessTokenJUnit getJwtAccessToken() {
		return this.mockToken;
	}

	@Test
	@Override
	public void overrideJwtValidateKeys() throws Throwable {
		super.overrideJwtValidateKeys();
	}

}
