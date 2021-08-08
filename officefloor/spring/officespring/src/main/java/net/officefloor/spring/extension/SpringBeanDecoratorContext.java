/*-
 * #%L
 * Spring Integration
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

package net.officefloor.spring.extension;

/**
 * Spring Bean decorator context.
 * 
 * @author Daniel Sagenschneider
 */
public interface SpringBeanDecoratorContext {

	/**
	 * Obtains the name of the Spring Bean.
	 * 
	 * @return Name of the Spring Bean.
	 */
	String getBeanName();

	/**
	 * Obtains the type of the Spring Bean.
	 * 
	 * @return Type of the Spring Bean.
	 */
	Class<?> getBeanType();

	/**
	 * Adds a further dependency for the Spring Bean.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type for dependency.
	 */
	void addDependency(String qualifier, Class<?> type);

}
