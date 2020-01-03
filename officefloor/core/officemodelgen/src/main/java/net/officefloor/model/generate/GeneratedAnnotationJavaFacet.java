package net.officefloor.model.generate;

import net.officefloor.frame.compatibility.JavaFacet;
import net.officefloor.frame.compatibility.JavaFacetContext;

/**
 * {@link JavaFacet} for the <code>Generated</code> annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class GeneratedAnnotationJavaFacet implements JavaFacet {

	/**
	 * Obtains the appropriate Generated annotation {@link Class} name.
	 * 
	 * @return Appropriate Generated annotation {@link Class} name.
	 */
	public static String getGeneratedClassName() {
		return new GeneratedAnnotationJavaFacet().isSupported() ? "javax.annotation.processing.Generated"
				: "javax.annotation.Generated";
	}

	/*
	 * =================== JavaFacet ====================
	 */

	@Override
	public boolean isSupported(JavaFacetContext context) throws Exception {
		return context.getFeature() >= 9;
	}

}