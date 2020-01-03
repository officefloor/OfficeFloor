package net.officefloor.spring.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.spring.SpringSupplierSource;

/**
 * Captures the {@link ConfigurableApplicationContext} from
 * {@link SpringSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringRule implements TestRule {

	/**
	 * {@link ConfigurableApplicationContext}.
	 */
	private ConfigurableApplicationContext applicationContext = null;

	/**
	 * Obtains the {@link ConfigurableApplicationContext}.
	 * 
	 * @return {@link ConfigurableApplicationContext}.
	 */
	public ConfigurableApplicationContext getApplicationContext() {

		// Ensure have application context
		if (this.applicationContext == null) {
			throw new IllegalStateException(
					"Must be in " + this.getClass().getSimpleName() + " context for accessing Spring beans");
		}

		// Return the application context
		return this.applicationContext;
	}

	/**
	 * Obtains the Spring bean by name.
	 * 
	 * @param name Name of bean.
	 * @return Bean.
	 */
	public Object getBean(String name) {
		return this.getApplicationContext().getBean(name);
	}

	/**
	 * Obtains the Spring bean by type.
	 * 
	 * @param requiredType Required type.
	 * @return Bean.
	 */
	public <B> B getBean(Class<B> requiredType) {
		return this.getApplicationContext().getBean(requiredType);
	}

	/*
	 * ===================== TestRule =============================
	 */

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					// Capture the Spring context
					SpringSupplierSource.captureApplicationContext(
							(context) -> SpringRule.this.applicationContext = context, () -> {

								// Evaluate the test
								base.evaluate();
								return null;
							});
				} finally {
					// Clear the application context
					SpringRule.this.applicationContext = null;
				}
			}
		};
	}

}