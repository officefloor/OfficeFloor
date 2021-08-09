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

package net.officefloor.compile;

import net.officefloor.configuration.impl.configuration.XmlFileConfigurationContext;
import net.officefloor.frame.api.source.ResourceSource;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Provides abstract functionality for testing integration of the
 * {@link OfficeFloorCompiler}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractModelCompilerTestCase extends OfficeFrameTestCase {

	/**
	 * {@link ModelCompilerTestSupport}.
	 */
	public final ModelCompilerTestSupport modelTestSupport = new ModelCompilerTestSupport(this.getClass(),
			this::getName);

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Setup
		this.modelTestSupport.beforeEach(null);
	}

	@Override
	protected void tearDown() throws Exception {

		// Reset the extension services
		this.modelTestSupport.afterEach(null);

		// Complete tear down
		super.tearDown();
	}

	/**
	 * Flags to use same {@link XmlFileConfigurationContext} across all tests of the
	 * class.
	 * 
	 * @param isSame <code>true</code> to re-use same configuration file for all
	 *               tests of the class.
	 */
	protected void setSameConfigurationForAllTests(boolean isSame) {
		this.modelTestSupport.setSameConfigurationForAllTests(isSame);
	}

	/**
	 * Obtains the {@link ResourceSource} for test being run.
	 * 
	 * @return {@link ResourceSource} for test being run.
	 */
	protected ResourceSource getResourceSource() {
		return this.modelTestSupport.getResourceSource();
	}

}
