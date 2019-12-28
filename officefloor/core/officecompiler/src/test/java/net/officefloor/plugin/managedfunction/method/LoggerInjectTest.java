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
package net.officefloor.plugin.managedfunction.method;

import java.util.logging.Logger;

import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure can inject {@link Logger}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerInjectTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to inject {@link Logger}.
	 */
	public void testLogger() {
		Closure<Logger> logger = new Closure<>();
		LoggerFunction instance = new LoggerFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", null, (context) -> {
			logger.value = ((ManagedFunctionContext<?, ?>) context).getLogger();
		});
		assertNotNull("Should have logger", logger.value);
		assertSame("Should inject the logger", logger.value, instance.logger);
	}

	public static class LoggerFunction {

		public Logger logger;

		public void method(Logger logger) {
			this.logger = logger;
		}
	}

}