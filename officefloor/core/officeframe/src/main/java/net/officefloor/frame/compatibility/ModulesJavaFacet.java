package net.officefloor.frame.compatibility;

/**
 * {@link JavaFacet} to determine if Jigsaw Modules available.
 * 
 * @author Daniel Sagenschneider
 */
public class ModulesJavaFacet implements JavaFacet {

	@Override
	public boolean isSupported(JavaFacetContext context) throws Exception {
		return context.getFeature() >= 9;
	}

}