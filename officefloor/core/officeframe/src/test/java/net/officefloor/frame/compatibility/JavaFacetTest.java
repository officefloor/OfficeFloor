package net.officefloor.frame.compatibility;

import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link JavaCompatibility}.
 * 
 * @author Daniel Sagenschneider
 */
public class JavaFacetTest extends OfficeFrameTestCase {

	/**
	 * Current Java feature.
	 */
	private static final int javaFeature = JavaFacet.getJavaFeatureVersion();

	/**
	 * Ensure correct feature.
	 */
	public void testCorrectFeature() {
		Closure<Integer> version = new Closure<>();
		assertTrue("Should be supported", JavaFacet.isSupported((context) -> {
			version.value = context.getFeature();
			return true;
		}));
		assertEquals("Incorrect version", javaFeature, version.value.intValue());
	}

	/**
	 * Ensure appropriately supports modules.
	 */
	public void testSupportModules() {
		boolean isSupported = JavaFacet.isSupported(new ModulesJavaFacet());
		assertEquals("Incorrect modules supported", (javaFeature >= 9), isSupported);
	}

}