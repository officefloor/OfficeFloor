package net.officefloor.maven.cloud;

/**
 * Tests no cloud configured.
 * 
 * @author Daniel Sagenschneider
 */
public class CloudIT extends AbstractCloudShadeMojoTestCase {

	public CloudIT() {
		SHADE.required(OFFICEFLOOR_CLASSES, WOOF_CLASSES, CABINET_CLASSES);
		SHADE.notIncluded(CABINET_IMPL_CLASSES);
		AWS.isCreate = false;
		GOOGLE.isCreate = false;
	}

}
