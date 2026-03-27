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

package net.officefloor.compile.impl.administration;

import org.junit.Assert;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.plugin.administration.clazz.ClassAdministrationSource;

/**
 * Class for {@link ClassAdministrationSource} that enables validating loading a
 * {@link AdministrationType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadAdministration {

	/**
	 * Mock extension interface.
	 */
	public static class MockExtensionInterface {
	}

	/**
	 * Administration method.
	 * 
	 * @param interfaces
	 *            Extension interfaces.
	 */
	public void admin(MockExtensionInterface[] interfaces) {
	}

	/**
	 * Validates the {@link AdministrationType} is correct for this class
	 * object.
	 * 
	 * @param administrationType
	 *            {@link AdministrationType}
	 */
	public static void assertAdministrationType(AdministrationType<?, ?, ?> administrationType) {
		Assert.assertEquals("Incorrect extension interface", MockExtensionInterface.class,
				administrationType.getExtensionType());
	}

}
