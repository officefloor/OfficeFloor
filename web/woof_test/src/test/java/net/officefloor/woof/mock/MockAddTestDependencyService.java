/*-
 * #%L
 * Web on OfficeFloor Testing
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
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

package net.officefloor.woof.mock;

import net.officefloor.frame.api.manage.UnknownObjectException;
import net.officefloor.test.TestDependencyService;
import net.officefloor.test.TestDependencyServiceContext;

/**
 * {@link TestDependencyService} for testing adding.
 */
public class MockAddTestDependencyService implements TestDependencyService {

	/*
	 * ==================== TestDependencyService ====================
	 */

	@Override
	public boolean isObjectAvailable(TestDependencyServiceContext context) {
		return this.getClass().isAssignableFrom(context.getObjectType());
	}

	@Override
	public Object getObject(TestDependencyServiceContext context) throws UnknownObjectException, Throwable {
		return this;
	}

}
