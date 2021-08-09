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

package net.officefloor.plugin.clazz.dependency;

/**
 * Index for {@link Class} item.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassItemIndex {

	/**
	 * Obtains the index of the item.
	 * 
	 * @return Index of the item.
	 */
	int getIndex();

	/**
	 * Allows adding further annotations to the item.
	 * 
	 * @param annotation Further annotation for the item.
	 */
	void addAnnotation(Object annotation);

}
