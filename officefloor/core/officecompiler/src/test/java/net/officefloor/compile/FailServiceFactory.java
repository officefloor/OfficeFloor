/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * {@link ServiceFactory} that fails to create the service.
 * 
 * @author Daniel Sagenschneider
 */
public class FailServiceFactory implements ServiceFactory<Throwable> {

	/**
	 * Failure.
	 */
	private static final Throwable failure = new Throwable("TEST");

	/**
	 * Obtains the error issue description.
	 * 
	 * @return Error issue description.
	 */
	public static String getIssueDescription() {
		return "Failed to create service from " + FailServiceFactory.class.getName();
	}

	/**
	 * Obtains the failure thrown on creating the service.
	 * 
	 * @return Failure thrown on creating the service.
	 */
	public static Throwable getCause() {
		return failure;
	}

	/*
	 * =================== ServiceFactory ==================
	 */

	@Override
	public Throwable createService(ServiceContext context) throws Throwable {
		throw failure;
	}

}
