package net.officefloor.maven.cloud;

/**
 * Tests no cloud configured.
 * 
 * @author Daniel Sagenschneider
 */
public class CloudIT extends AbstractCloudShadeMojoTestCase {

	public CloudIT() {
		AWS.isCreate = false;
		GOOGLE.isCreate = false;
	}

}
