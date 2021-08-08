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

package net.officefloor.compile.impl.util;

/**
 * <p>
 * Extracts a {@link String} value from the input object.
 * <p>
 * Typically the extracted {@link String} will be used as a comparable key in
 * sorting a list of objects.
 * 
 * @author Daniel Sagenschneider
 */
public interface StringExtractor<T> {

	/**
	 * Extracts the {@link String} from the {@link Object}.
	 * 
	 * @param object
	 *            {@link Object} to extract the {@link String} from.
	 * @return Extracted {@link String}.
	 */
	String toString(T object);
}
