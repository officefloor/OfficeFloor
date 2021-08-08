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

package net.officefloor.frame.api.source;

import java.util.ServiceLoader;

/**
 * Generic factory to be loaded by the {@link ServiceLoader}. This will be
 * provided the {@link ServiceContext} to create the specific service.
 * 
 * @author Daniel Sagenschneider
 */
public interface ServiceFactory<S> {

	/**
	 * Creates the service.
	 * 
	 * @param context
	 *            {@link ServiceContext}.
	 * @return Service.
	 * @throws Throwable
	 *             If fails to create the service.
	 */
	S createService(ServiceContext context) throws Throwable;

}
