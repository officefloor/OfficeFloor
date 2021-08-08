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

package net.officefloor.plugin.managedfunction.clazz;

import java.lang.reflect.Method;

import net.officefloor.compile.ManagedFunctionSourceService;
import net.officefloor.compile.ManagedFunctionSourceServiceFactory;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.method.AbstractFunctionManagedFunctionSource;

/**
 * {@link ManagedFunctionSource} for a {@link Class} having the {@link Method}
 * instances as the {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedFunctionSource extends AbstractFunctionManagedFunctionSource
		implements ManagedFunctionSourceService<ClassManagedFunctionSource>, ManagedFunctionSourceServiceFactory {

	/*
	 * =================== ManagedFunctionSourceService ===================
	 */

	@Override
	public ManagedFunctionSourceService<?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public String getManagedFunctionSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassManagedFunctionSource> getManagedFunctionSourceClass() {
		return ClassManagedFunctionSource.class;
	}

}
