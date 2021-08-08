/*-
 * #%L
 * Model Generator
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

package net.officefloor.model.generate;

import net.officefloor.frame.compatibility.JavaFacet;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * {@link JavaFacet} for the <code>Generated</code> annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class GeneratedAnnotationJavaFacetTest extends OfficeFrameTestCase {

	/**
	 * Ensure the correct annotation.
	 */
	public void testCorrectAnnotation() {
		String expected = JavaFacet.getJavaFeatureVersion() >= 9 ? "javax.annotation.processing.Generated"
				: "javax.annotation.Generated";
		assertEquals("Incorrect annotation", expected, GeneratedAnnotationJavaFacet.getGeneratedClassName());
	}

}
