/*-
 * #%L
 * H2 Test
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

package net.officefloor.jdbc.h2.test;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the {@link H2Reset}.
 * 
 * @author Daniel Sagenschneider
 */
@ExtendWith(MockWoofServerExtension.class)
public class H2ResetTest extends AbstractH2ResetTestCase {

	/**
	 * {@link H2Reset} being tested on {@link Field} injection.
	 */
	private @Dependency H2Reset reset;

	/**
	 * Ensure able to inject into test.
	 */
	@Test
	public void clear() throws Exception {
		this.doTest(false, () -> this.reset.clean());
	}

	/**
	 * Ensure able to inject into test.
	 */
	@Test
	public void reset() throws Exception {
		this.doTest(true, () -> this.reset.reset());
	}

	/**
	 * Ensure able to inject into test method.
	 */
	@Test
	public void clear(H2Reset reset) throws Exception {
		this.doTest(false, () -> reset.clean());
	}

	/**
	 * Ensure able to inject into test method.
	 */
	@Test
	public void reset(H2Reset reset) throws Exception {
		this.doTest(true, () -> reset.reset());
	}
}
