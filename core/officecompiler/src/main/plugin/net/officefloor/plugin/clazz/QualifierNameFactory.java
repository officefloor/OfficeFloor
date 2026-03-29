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

package net.officefloor.plugin.clazz;

import java.lang.annotation.Annotation;

/**
 * Determines the {@link Qualifier} name from the {@link Qualifier} attributes.
 * 
 * @author Daniel Sagenschneider
 */
public interface QualifierNameFactory<A extends Annotation> {

	/**
	 * Obtains the {@link Qualifier} name from the {@link Annotation}.
	 * 
	 * @param annotation
	 *            {@link Annotation} containing attributes to aid determining
	 *            the name.
	 * @return Qualified name.
	 */
	String getQualifierName(A annotation);

}
