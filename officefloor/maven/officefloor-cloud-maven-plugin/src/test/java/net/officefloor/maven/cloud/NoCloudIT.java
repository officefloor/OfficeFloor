package net.officefloor.maven.cloud;

/**
 * Tests no cloud configured.
 * 
 * @author Daniel Sagenschneider
 */
public class NoCloudIT extends AbstractCloudShadeMojoTestCase {

	public NoCloudIT() {
		AWS.isCreate = false;
		GOOGLE.isCreate = false;
	}
}
