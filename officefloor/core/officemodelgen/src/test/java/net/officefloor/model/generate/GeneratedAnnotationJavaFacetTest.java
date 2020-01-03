package net.officefloor.model.generate;

import net.officefloor.frame.compatibility.JavaFacet;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * {@link JavaFacet} for the <code>Generated</code> annotation.
 * 
 * @author Daniel Sagenschneider
 */
public class GeneratedAnnotationJavaFacetTest extends OfficeFrameTestCase {

	/**
	 * Ensure the correct annotation.
	 */
	public void testCorrectAnnotation() {
		String expected = JavaFacet.getJavaFeatureVersion() >= 9 ? "javax.annotation.processing.Generated"
				: "javax.annotation.Generated";
		assertEquals("Incorrect annotation", expected, GeneratedAnnotationJavaFacet.getGeneratedClassName());
	}

}