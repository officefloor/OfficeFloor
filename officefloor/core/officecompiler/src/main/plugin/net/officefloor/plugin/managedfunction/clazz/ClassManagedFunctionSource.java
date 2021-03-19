/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
