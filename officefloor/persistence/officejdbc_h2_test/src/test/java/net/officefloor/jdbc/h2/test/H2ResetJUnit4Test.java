/*-
 * #%L
 * H2 Test
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

package net.officefloor.jdbc.h2.test;

import java.lang.reflect.Field;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the {@link H2Reset} with {@link TestRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class H2ResetJUnit4Test extends AbstractH2ResetTestCase {

	/**
	 * {@link MockWoofServerRule}.
	 */
	@Rule
	public final MockWoofServerRule server = new MockWoofServerRule(this);

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

}
