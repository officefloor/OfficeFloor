/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.compatibility;

import java.lang.reflect.Method;

/**
 * Facet that is checked to see if the current version of Java supports.
 * 
 * @author Daniel Sagenschneider
 */
public interface JavaFacet {

	/**
	 * Determines if the {@link JavaFacet} is supported.
	 * 
	 * @param javaFacet {@link JavaFacet}.
	 * @return <code>true</code> if the {@link JavaFacet} is supported.
	 */
	static boolean isSupported(JavaFacet javaFacet) {
		try {
			return javaFacet.isSupported(new JavaFacetContext() {
				@Override
				public int getFeature() {
					return getJavaFeatureVersion();
				}
			});
		} catch (Exception ex) {
			return false; // failure, so consider not supported
		}
	}

	/**
	 * Determines the feature version of current Java.
	 * 
	 * @return Feature version of current Java.
	 */
	static int getJavaFeatureVersion() {
		int javaVersion = -1;
		try {
			Method getVersion = Runtime.class.getMethod("version");
			Object version = getVersion.invoke(null);
			try {
				Method getFeature = version.getClass().getMethod("feature");
				javaVersion = (Integer) getFeature.invoke(version);
			} catch (Exception ex) {
				// No feature method
				javaVersion = 9;
			}
		} catch (Exception ex) {
			// No version method
			javaVersion = 8;
		}
		return javaVersion;
	}

	/**
	 * Allows checking directly on {@link JavaFacet} implementation if supported.
	 * 
	 * @return <code>true</code> if the {@link JavaFacet} is supported.
	 */
	default boolean isSupported() {
		return isSupported(this);
	}

	/**
	 * Indicates if the facet is supported.
	 * 
	 * @param context {@link JavaFacetContext}.
	 * @return <code>true</code> if the facet is supported.
	 * @throws Exception If fails to determine if facet is supported.
	 */
	boolean isSupported(JavaFacetContext context) throws Exception;

}
