/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.compile.impl.adapt;

import java.util.List;

/**
 * Extracts the values of a {@link List}.
 * 
 * @author Daniel Sagenschneider
 */
public class ListAdapter {

	/**
	 * Translates {@link List} to array of its values.
	 * 
	 * @param list {@link List}.
	 * @return Array of {@link List} values.
	 */
	public static Object[] toArray(Object list) {
		return ((List<?>) list).toArray();
	}

}
