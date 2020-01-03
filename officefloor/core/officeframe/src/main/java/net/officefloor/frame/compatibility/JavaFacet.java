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