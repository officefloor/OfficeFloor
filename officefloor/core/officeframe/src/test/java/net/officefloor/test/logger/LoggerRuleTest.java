/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.test.logger;

import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the {@link LoggerRule}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerRuleTest extends AbstractLoggerJUnitTestCase {

	/**
	 * {@link LoggerRule} to test.
	 */
	@Rule
	public final LoggerRule rule = new LoggerRule();

	/*
	 * ================= AbstractLoggerJUnitTestCase ==================
	 */

	@Override
	protected AbstractLoggerJUnit getLoggerJUnit() {
		return this.rule;
	}

	/*
	 * ====================== Tests ===================================
	 */

	@Test
	@Override
	public void specificEvent() throws Throwable {
		super.specificEvent();
	}

	@Test
	@Override
	public void multipleEvents() throws Throwable {
		super.multipleEvents();
	}

	@Test
	@Override
	public void regularExpression() throws Throwable {
		super.regularExpression();
	}

	@Test
	@Override
	public void recordsFromLogger() throws Throwable {
		super.recordsFromLogger();
	}

	@Test
	@Override
	public void levels() throws Throwable {
		super.levels();
	}

}
