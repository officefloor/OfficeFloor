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

package net.officefloor.spring;

/**
 * Qualfied bean for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class QualifiedBean {

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Instantiate.
	 * 
	 * @param value Value.
	 */
	public QualifiedBean(String value) {
		this.value = value;
	}

	/**
	 * Obtains the value.
	 * 
	 * @return Value.
	 */
	public String getValue() {
		return this.value;
	}

}
