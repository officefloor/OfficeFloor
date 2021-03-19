/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.test.logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Tests the {@link LoggerExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoggerExtensionTest extends AbstractLoggerJUnitTestCase {

	/**
	 * {@link LoggerExtension} to test.
	 */
	@RegisterExtension
	public final LoggerExtension extension = new LoggerExtension();

	/*
	 * ================= AbstractLoggerJUnitTestCase ==================
	 */

	@Override
	protected AbstractLoggerJUnit getLoggerJUnit() {
		return this.extension;
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
