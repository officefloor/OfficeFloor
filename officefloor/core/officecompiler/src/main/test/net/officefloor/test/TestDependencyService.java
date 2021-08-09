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

package net.officefloor.test;

import net.officefloor.frame.api.manage.UnknownObjectException;

/**
 * Service providing additional test dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public interface TestDependencyService {

	/**
	 * Indicates if able to provide object.
	 * 
	 * @param context {@link TestDependencyServiceContext}.
	 * @return <code>true</code> if able to provide object.
	 */
	boolean isObjectAvailable(TestDependencyServiceContext context);

	/**
	 * Obtains the dependency object.
	 * 
	 * @param context {@link TestDependencyServiceContext}.
	 * @return Object.
	 * @throws UnknownObjectException If unknown bound object name.
	 * @throws Throwable              If failure in obtaining the bound object.
	 */
	Object getObject(TestDependencyServiceContext context) throws UnknownObjectException, Throwable;

}
