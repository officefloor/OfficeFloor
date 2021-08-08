/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.source;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceFactory} triggering failures.
 * 
 * @author Daniel Sagenschneider
 */
public class FailServiceFactory implements ServiceFactory<Class<FailServiceFactory>> {

	/**
	 * Failure to instantiate this.
	 */
	public static Throwable instantiateFailure = null;

	/**
	 * Failure to create the service.
	 */
	public static Throwable createServiceFailure = null;

	/**
	 * Resets for next test.
	 */
	public static void reset() {
		instantiateFailure = null;
		createServiceFailure = null;
	}

	/**
	 * Instantiate (with possible failure).
	 */
	public FailServiceFactory() throws Throwable {
		if (instantiateFailure != null) {
			throw instantiateFailure;
		}
	}

	/*
	 * ================= ServiceFactory ===================
	 */

	@Override
	public Class<FailServiceFactory> createService(ServiceContext context) throws Throwable {

		// Throw possible create error
		if (createServiceFailure != null) {
			throw createServiceFailure;
		}

		// Return successfully
		return FailServiceFactory.class;
	}

}
