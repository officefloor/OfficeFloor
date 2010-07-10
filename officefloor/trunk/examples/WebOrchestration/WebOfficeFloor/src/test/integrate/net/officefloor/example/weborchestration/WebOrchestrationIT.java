/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.example.weborchestration;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Integration tests for Web Orchestration examples.
 * 
 * @author Daniel Sagenschneider
 */
public class WebOrchestrationIT extends AbstractSeleniumTestCase {

	/**
	 * Creates the {@link TestSuite} of web example integration tests.
	 * 
	 * @return {@link TestSuite} of web example integration tests.
	 */
	public static Test suite() {
		// TODO just return the createTestSuite
		// return
		AbstractSeleniumTestCase.createTestSuite(".task", "{", "}");

		// TODO remove
		TestSuite suite = new TestSuite();
		// suite.addTestSuite(CustomerTestCase.class);
		// suite.addTestSuite(QuoteTestCase.class);
		suite.addTestSuite(InvoiceTestCase.class);
		// suite.addTestSuite(SelectProductsTestCase.class);
		return suite;
	}

}