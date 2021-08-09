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

package net.officefloor.plugin.clazz.method;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.clazz.factory.ClassObjectFactory;

/**
 * Default {@link MethodObjectFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class DefaultMethodObjectFactory implements MethodObjectFactory {

	/**
	 * {@link ClassObjectFactory}.
	 */
	private final ClassObjectFactory objectFactory;

	/**
	 * Instantiate.
	 * 
	 * @param objectFactory {@link ClassObjectFactory}.
	 */
	public DefaultMethodObjectFactory(ClassObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	/*
	 * =================== MethodObjectFactory =======================
	 */

	@Override
	public Object createInstance(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {
		return this.objectFactory.createObject(context);
	}

}
