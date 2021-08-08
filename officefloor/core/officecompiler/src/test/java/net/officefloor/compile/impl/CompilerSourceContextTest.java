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

package net.officefloor.compile.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.test.MockClockFactory;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Ensure able to create root {@link SourceContext} from
 * {@link OfficeFloorCompiler}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompilerSourceContextTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to create {@link SourceContext}.
	 */
	public void testCreateSourceContext() {

		// Create the class loader
		ClassLoader classLoader = createNewClassLoader();

		// Create the compiler with class loader
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);

		// Create clock factory
		long mockCurrentTime = 2000;
		MockClockFactory clockFactory = new MockClockFactory(mockCurrentTime);

		// Load details
		compiler.setClockFactory(clockFactory);
		compiler.addResources((location) -> {
			if (!"TEST".equals(location)) {
				return null;
			}
			return new ByteArrayInputStream("RESOURCE".getBytes());
		});

		// Create the source context
		SourceContext context = compiler.createRootSourceContext();

		// Ensure correct details
		assertSame("Incorrect class loader", classLoader, context.getClassLoader());
		assertEquals("Incorrect clock", Long.valueOf(mockCurrentTime), context.getClock((time) -> time).getTime());
		assertContents(new StringReader("RESOURCE"), new InputStreamReader(context.getResource("TEST")));
	}

}
