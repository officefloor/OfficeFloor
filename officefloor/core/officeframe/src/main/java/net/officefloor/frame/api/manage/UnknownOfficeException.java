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
 * Indicates an unknown {@link Office} was requested.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownOfficeException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Name of the unknown {@link Office}.
	 */
	private final String unknownOfficeName;

	/**
	 * Initiate.
	 * 
	 * @param unknownOfficeName Name of the unknown {@link Office}.
	 */
	public UnknownOfficeException(String unknownOfficeName) {
		super("Unknown Office '" + unknownOfficeName + "'");
		this.unknownOfficeName = unknownOfficeName;
	}

	/**
	 * Obtains the name of the unknown {@link Office}.
	 * 
	 * @return Name of the unknown {@link Office}.
	 */
	public String getUnknownOfficeName() {
		return this.unknownOfficeName;
	}
}
