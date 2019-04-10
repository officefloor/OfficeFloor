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