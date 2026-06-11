/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.test;

import net.officefloor.frame.api.manage.UnknownObjectException;

/**
 * {@link TestDependencyService} providing an {@link Object} instance.
 */
public class ObjectTestDependencyService<T, O extends T> implements TestDependencyService {

	/**
	 * Type for dependency.
	 */
	private final Class<T> type;

	/**
	 * Object for dependency.
	 */
	private final Object object;

	/**
	 * Instantiate using {@link Object} type.
	 * 
	 * @param object {@link Object}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ObjectTestDependencyService(O object) {
		this((Class) object.getClass(), object);
	}

	/**
	 * Instantiate specifying more generic {@link Object} type.
	 * 
	 * @param type   Type for dependency.
	 * @param object Object as the dependency.
	 */
	public ObjectTestDependencyService(Class<T> type, O object) {
		this.type = type;
		this.object = object;
	}

	/*
	 * ================= TestDependencyService ==================
	 */

	@Override
	public boolean isObjectAvailable(TestDependencyServiceContext context) {
		return context.getObjectType().isAssignableFrom(this.type);
	}

	@Override
	public Object getObject(TestDependencyServiceContext context) throws UnknownObjectException, Throwable {
		return this.object;
	}

}
