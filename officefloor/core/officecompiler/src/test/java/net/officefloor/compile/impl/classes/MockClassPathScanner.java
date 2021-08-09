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
