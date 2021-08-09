/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.template;

import net.officefloor.web.template.extension.WebTemplateExtension;

/**
 * Flags that the {@link WebTemplateExtension} is unknown or unable to be
 * obtained.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionException extends Exception {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Initiate.
	 * 
	 * @param message Reason.
	 * @param cause   Cause.
	 */
	public WoofTemplateExtensionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Initiate.
	 * 
	 * @param message Reason.
	 */
	public WoofTemplateExtensionException(String message) {
		super(message);
	}
}
