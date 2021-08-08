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
 * Context for the {@link ClassDependencies}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependenciesContext extends ClassDependenciesFlowContext {

	/**
	 * Adds a dependency.
	 * 
	 * @param dependencyName Name of the dependency.
	 * @param qualifier      Qualifier. May be <code>null</code>.
	 * @param objectType     Dependency {@link Class}.
	 * @param annotations    Annotations.
	 * @return {@link ClassItemIndex} of the dependency.
	 */
	ClassItemIndex addDependency(String dependencyName, String qualifier, Class<?> objectType, Object[] annotations);

}
