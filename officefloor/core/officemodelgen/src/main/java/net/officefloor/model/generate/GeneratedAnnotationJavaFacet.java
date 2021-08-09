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
import net.officefloor.frame.compatibility.JavaFacetContext;

/**
 * {@link JavaFacet} for the <code>Generated</code> annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class GeneratedAnnotationJavaFacet implements JavaFacet {

	/**
	 * Obtains the appropriate Generated annotation {@link Class} name.
	 * 
	 * @return Appropriate Generated annotation {@link Class} name.
	 */
	public static String getGeneratedClassName() {
		return new GeneratedAnnotationJavaFacet().isSupported() ? "javax.annotation.processing.Generated"
				: "javax.annotation.Generated";
	}

	/*
	 * =================== JavaFacet ====================
	 */

	@Override
	public boolean isSupported(JavaFacetContext context) throws Exception {
		return context.getFeature() >= 9;
	}

}
