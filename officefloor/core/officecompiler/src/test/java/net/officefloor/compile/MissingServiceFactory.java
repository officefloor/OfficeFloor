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

import org.junit.Assert;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.ServiceFactory;

/**
 * Missing {@link ServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class MissingServiceFactory implements ServiceFactory<Object> {

	/**
	 * Obtains the issue message.
	 * 
	 * @return Issue message.
	 */
	public static String getIssueDescription() {
		return "No services configured for " + MissingServiceFactory.class.getName();
	}

	/*
	 * =============== ServiceFactory =====================
	 */

	@Override
	public Throwable createService(ServiceContext context) throws Throwable {
		Assert.fail("Should not be creating service");
		return null;
	}

}
