package net.officefloor.frame.compatibility;

/**
 * Context for the {@link JavaFacet}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JavaFacetContext {

	/**
	 * Obtains the feature (eg 8, 9, 10, etc).
	 * 
	 * @return feature.
	 */
	int getFeature();

}