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
