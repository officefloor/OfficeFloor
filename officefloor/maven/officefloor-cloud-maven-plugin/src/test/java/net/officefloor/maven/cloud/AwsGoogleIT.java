package net.officefloor.maven.cloud;

/**
 * Tests AWS and Google configured.
 * 
 * @author Daniel Sagenschneider
 */
public class AwsGoogleIT extends AbstractCloudShadeMojoTestCase {

	public AwsGoogleIT() {
		AWS.isCreate = true;
		AWS.required(OFFICEFLOOR_CLASSES, WOOF_CLASSES, CABINET_CLASSES, CABINET_IMPL_CLASSES, AWS_CLASSES);
		GOOGLE.isCreate = true;
		GOOGLE.required(OFFICEFLOOR_CLASSES, WOOF_CLASSES, CABINET_CLASSES, CABINET_IMPL_CLASSES, GOOGLE_CLASSES);
	}

}
