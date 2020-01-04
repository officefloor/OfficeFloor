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

package net.officefloor.compile.impl.classes;

import java.io.IOException;

import net.officefloor.compile.classes.ClassPathScanner;
import net.officefloor.compile.classes.ClassPathScannerContext;
import net.officefloor.compile.classes.ClassPathScannerServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Mock {@link ClassPathScanner} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockClassPathScanner implements ClassPathScannerServiceFactory, ClassPathScanner {

	/**
	 * Mock package path. Also, invalid path to confirm handled gracefully.
	 */
	public static final String MOCK_PACKAGE_PATH = "mock package path";

	/**
	 * Mock entry path added.
	 */
	public static final String MOCK_ENTRY_PATH = "MOCK";

	/*
	 * ====================== ClassPathScannerServiceFactory =====================
	 */

	@Override
	public ClassPathScanner createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ============================ ClassPathScanner ==============================
	 */

	@Override
	public void scan(ClassPathScannerContext context) throws IOException {
		if (MOCK_PACKAGE_PATH.equals(context.getPackageName())) {

			// Add mock entry
			context.addEntry(MOCK_ENTRY_PATH);
		}
	}

}
