/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.maven.woof;

import java.io.File;
import java.net.URL;

import net.officefloor.frame.compatibility.JavaFacet;
import net.officefloor.frame.compatibility.JavaFacetContext;

/**
 * Obtains the {@link URL} instances for adding JavaFx to class path.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaFxFacet implements JavaFacet {

	/**
	 * Obtains the {@link URL}s for class path entries.
	 * 
	 * @return {@link URL}s for class path entries.
	 * @throws Exception If fails to load the class path entries.
	 */
	public static URL[] getClassPathEntries() throws Exception {

		// Obtain the java facet context
		JavaFxFacet facet = new JavaFxFacet();
		JavaFacet.isSupported(facet);

		// Handle based on version
		switch (facet.javaFacetContext.getFeature()) {
		case 8:
			String javaHome = System.getProperty("java.home");
			File javaFxJar = new File(javaHome, "lib/ext/jfxrt.jar");
			return new URL[] { javaFxJar.toURI().toURL() };
		}

		// As here, included by default
		return new URL[0];
	}

	/**
	 * {@link JavaFacetContext}.
	 */
	private JavaFacetContext javaFacetContext;

	/*
	 * ================ JavaFacet ================
	 */

	@Override
	public boolean isSupported(JavaFacetContext context) throws Exception {
		this.javaFacetContext = context;
		return true;
	}

}