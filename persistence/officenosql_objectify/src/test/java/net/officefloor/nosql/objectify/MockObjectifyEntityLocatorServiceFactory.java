/*-
 * #%L
 * Objectify Persistence
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

package net.officefloor.nosql.objectify;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link ObjectifyEntityLocatorServiceFactory} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockObjectifyEntityLocatorServiceFactory
		implements ObjectifyEntityLocatorServiceFactory, ObjectifyEntityLocator {

	/*
	 * =================== ObjectifyEntityLocatorServiceFactory ===============
	 */

	@Override
	public ObjectifyEntityLocator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== ObjectifyEntityLocator ========================
	 */

	@Override
	public Class<?>[] locateEntities() throws Exception {
		return new Class[] { ServiceRegisteredEntity.class };
	}

}
