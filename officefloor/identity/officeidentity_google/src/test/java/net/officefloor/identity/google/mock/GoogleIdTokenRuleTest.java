/*-
 * #%L
 * Identity for Google Logins
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

package net.officefloor.identity.google.mock;

import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the {@link GoogleIdTokenRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleIdTokenRuleTest extends AbstractGoogleIdTokenRuleTestCase {

	/**
	 * {@link Rule} under test.
	 */
	@Rule
	public final GoogleIdTokenRule rule = new GoogleIdTokenRule();

	/**
	 * ==================== AbstractGoogleIdTokenRuleTestCase ====================
	 */

	@Override
	protected AbstractGoogleIdTokenJUnit getGoogleIdToken() {
		return this.rule;
	}

	@Test
	@Override
	public void mockHttpServer() throws Throwable {
		super.mockHttpServer();
	}

}
