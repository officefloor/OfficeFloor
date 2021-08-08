/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor.style;

import java.util.Arrays;

/**
 * System based {@link StyleRegistry}.
 * 
 * @author Daniel Sagenschneider
 */
public class SystemStyleRegistry extends AbstractStyleRegistry {

	/**
	 * Instantiate.
	 */
	public SystemStyleRegistry() {

		// Obtain the package path for the URL protocol
		String protocolPackageFull = Handler.class.getPackage().getName();
		final String protocolPackageName = protocolPackageFull.substring(0, protocolPackageFull.lastIndexOf('.'));

		// Determine if already include the package
		final String PROPERTY_NAME = "java.protocol.handler.pkgs";
		String existingPackages = System.getProperty(PROPERTY_NAME, "");

		// Determine if already contains package
		String[] packages = existingPackages.split("|");
		if (!Arrays.stream(packages).anyMatch((packageName) -> packageName.equals(protocolPackageName))) {

			// Load in the protocol package
			String newPackages = (((existingPackages == null) || (existingPackages.trim().length() == 0)) ? ""
					: existingPackages + "|") + protocolPackageName;
			System.setProperty(PROPERTY_NAME, newPackages);
		}
	}

}
