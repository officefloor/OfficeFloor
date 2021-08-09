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

package net.officefloor.frame.api.manage;

/**
 * Indicates timed out waiting on the object.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectTimeoutException extends Exception {

	/**
	 * Default serialisation.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Bound object name.
	 */
	private final String boundObjectName;

	/**
	 * Instantiate.
	 * 
	 * @param boundObjectName     Bound object name.
	 * @param timeoutMilliseconds Time out in milliseconds.
	 */
	public ObjectTimeoutException(String boundObjectName, long timeoutMilliseconds) {
		super("Timed out waiting on object " + boundObjectName + " after " + timeoutMilliseconds + " milliseconds");
		this.boundObjectName = boundObjectName;
	}

	/**
	 * Obtains the bound object name.
	 * 
	 * @return Bound object name.
	 */
	public String getBoundObjectName() {
		return this.boundObjectName;
	}
}
