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

package net.officefloor.frame.compatibility;

import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link JavaCompatibility}.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaFacetTest extends OfficeFrameTestCase {

	/**
	 * Current Java feature.
	 */
	private static final int javaFeature = JavaFacet.getJavaFeatureVersion();

	/**
	 * Ensure correct feature.
	 */
	public void testCorrectFeature() {
		Closure<Integer> version = new Closure<>();
		assertTrue("Should be supported", JavaFacet.isSupported((context) -> {
			version.value = context.getFeature();
			return true;
		}));
		assertEquals("Incorrect version", javaFeature, version.value.intValue());
	}

	/**
	 * Ensure appropriately supports modules.
	 */
	public void testSupportModules() {
		boolean isSupported = JavaFacet.isSupported(new ModulesJavaFacet());
		assertEquals("Incorrect modules supported", (javaFeature >= 9), isSupported);
	}

}
