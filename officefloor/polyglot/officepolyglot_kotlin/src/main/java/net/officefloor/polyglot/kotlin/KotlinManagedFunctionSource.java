/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.polyglot.kotlin;

import java.lang.reflect.Method;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.plugin.managedfunction.clazz.ClassManagedFunctionSource;
import net.officefloor.plugin.managedfunction.method.MethodFunction;
import net.officefloor.plugin.managedfunction.method.MethodManagedFunctionBuilder;
import net.officefloor.plugin.managedfunction.method.MethodObjectInstanceManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.section.clazz.SectionClassManagedFunctionSource;

/**
 * Kotlin {@link ManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class KotlinManagedFunctionSource extends SectionClassManagedFunctionSource {

	/*
	 * =================== ClassManagedFunctionSource ==========================
	 */

	@Override
	protected MethodObjectInstanceManufacturer createMethodObjectInstanceManufacturer(Class<?> clazz) throws Exception {
		return () -> null; // always static
	}

	@Override
	protected MethodManagedFunctionBuilder createMethodManagedFunctionBuilder(FunctionNamespaceBuilder namespaceBuilder,
			ManagedFunctionSourceContext context) throws Exception {
		return new KotlinMethodManagedFunctionBuilder();
	}

	/**
	 * {@link MethodManagedFunctionBuilder} for the
	 * {@link KotlinFunctionSectionSource}.
	 */
	protected class KotlinMethodManagedFunctionBuilder extends SectionMethodManagedFunctionBuilder {


		@Override
		protected ManagedFunctionTypeBuilder<Indexed, Indexed> addManagedFunctionType(
				MethodManagedFunctionTypeContext context) {
			return context.getNamespaceBuilder().addManagedFunctionType(context.getFunctionName(),
					context.getFunctionFactory(), Indexed.class, Indexed.class);
		}
	}

}