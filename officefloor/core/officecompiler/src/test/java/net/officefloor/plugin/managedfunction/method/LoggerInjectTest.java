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