package net.officefloor.maven.cloud;

/**
 * Tests Google configured.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleIT extends AbstractCloudShadeMojoTestCase {

	public GoogleIT() {
		AWS.isCreate = false;
		GOOGLE.isCreate = true;
		GOOGLE.required(OFFICEFLOOR_CLASSES, WOOF_CLASSES, CABINET_CLASSES, CABINET_IMPL_CLASSES, GOOGLE_CLASSES);
	}

}
