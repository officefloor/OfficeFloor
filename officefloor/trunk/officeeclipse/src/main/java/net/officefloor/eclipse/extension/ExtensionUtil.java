/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.extension;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 * Utility class for working with extensions.
 * 
 * @author Daniel
 */
public class ExtensionUtil {

	/**
	 * Obtains the extension id for the input extension name.
	 * 
	 * @param extensionName
	 *            Name of the extension.
	 * @return Id for the extension.
	 */
	public static String getExtensionId(String name) {
		return "net.officefloor.eclipse." + name;
	}

	/**
	 * Creates the executable extensions for the particular extension.
	 * 
	 * @param extensionId
	 *            Id of the extension.
	 * @param type
	 *            Type expected for the executable extension.
	 * @return Listing of executable extensions.
	 * @throws Exception
	 *             If fails to create the listing of executable extensions.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> createExecutableExtensions(String extensionId,
			Class<T> type) throws Exception {

		// Obtain the extensions for extension point
		IConfigurationElement[] configurationElements = Platform
				.getExtensionRegistry()
				.getConfigurationElementsFor(extensionId);

		// Obtain the listing of executable extension
		List<T> typedExecutableExtensions = new LinkedList<T>();
		for (IConfigurationElement element : configurationElements) {

			// Create the executable extension
			Object executableExtension = element
					.createExecutableExtension("class");

			// Only add if of the appropriate type
			if (type.isAssignableFrom(executableExtension.getClass())) {
				// Appropriate type, therefore include
				typedExecutableExtensions.add((T) executableExtension);
			}
		}

		// Return the listing of typed executable extensions
		return typedExecutableExtensions;
	}

	/**
	 * All access via static methods.
	 */
	private ExtensionUtil() {
	}

}
