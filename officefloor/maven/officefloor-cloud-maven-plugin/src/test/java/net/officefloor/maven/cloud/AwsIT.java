package net.officefloor.maven.cloud;

/**
 * Tests AWS configured.
 * 
 * @author Daniel Sagenschneider
 */
public class AwsIT extends AbstractCloudShadeMojoTestCase {

	public AwsIT() {
		AWS.isCreate = true;
		AWS.required(OFFICEFLOOR_CLASSES, WOOF_CLASSES, CABINET_CLASSES, CABINET_IMPL_CLASSES, AWS_CLASSES);
		GOOGLE.isCreate = false;
	}

}
