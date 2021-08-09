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
