package net.officefloor.spring.test;

import org.junit.runners.model.Statement;
import org.springframework.context.ConfigurableApplicationContext;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.spring.SpringSupplierSource;

/**
 * Tests the {@link SpringRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringRuleTest extends OfficeFrameTestCase {

	/**
	 * Ensure {@link ConfigurableApplicationContext} only available within context.
	 */
	public void testEnsureInContext() {
		try {
			new SpringRule().getApplicationContext();
			fail("Should not be successful");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause", "Must be in SpringRule context for accessing Spring beans",
					ex.getMessage());
		}
	}

	/**
	 * Ensure able to get Spring beans.
	 */
	public void testSpringBeans() throws Throwable {

		// Capture the spring bean
		Closure<SimpleBean> beanByName = new Closure<>();
		Closure<SimpleBean> beanByType = new Closure<>();

		// Ensure can use spring
		SpringRule spring = new SpringRule();
		spring.apply(new Statement() {
			@Override
			public void evaluate() throws Throwable {
				// Configure OfficeFloor to auto-wire in Spring beans
				CompileOfficeFloor compile = new CompileOfficeFloor();
				compile.office((context) -> {
					OfficeArchitect office = context.getOfficeArchitect();
					office.addSupplier("SPRING", SpringSupplierSource.class.getName()).addProperty(
							SpringSupplierSource.CONFIGURATION_CLASS_NAME, MockSpringBootConfiguration.class.getName());
				});
				try (OfficeFloor officeFloor = compile.compileAndOpenOfficeFloor()) {

					// Ensure can capture the spring bean
					beanByName.value = (SimpleBean) spring.getBean("simpleBean");
					beanByType.value = spring.getBean(SimpleBean.class);
				}
			}
		}, null).evaluate();

		// Ensure obtained the spring beans
		assertNotNull("Should obtain bean by name", beanByName.value);
		assertNotNull("Should obtain bean by type", beanByType.value);
	}

}