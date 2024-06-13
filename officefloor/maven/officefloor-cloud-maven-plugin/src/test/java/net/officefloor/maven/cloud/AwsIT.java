package net.officefloor.maven.cloud;

/**
 * Tests no cloud configured.
 * 
 * @author Daniel Sagenschneider
 */
public class AwsIT extends AbstractCloudShadeMojoTestCase {

	public AwsIT() {
		SHADE.required(OFFICEFLOOR_CLASSES, WOOF_CLASSES, CABINET_CLASSES);
		SHADE.notIncluded(CABINET_IMPL_CLASSES);
		AWS.isCreate = true;
		AWS.required(OFFICEFLOOR_CLASSES, WOOF_CLASSES, CABINET_CLASSES, CABINET_IMPL_CLASSES, AWS_CLASSES);
		GOOGLE.isCreate = false;
	}

}
