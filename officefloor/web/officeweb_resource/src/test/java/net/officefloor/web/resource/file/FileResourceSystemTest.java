package net.officefloor.web.resource.file;

import net.officefloor.web.resource.impl.AbstractHttpResourceStoreTestCase;

/**
 * Tests the {@link FileResourceSystem}.
 * 
 * @author Daniel Sagenschneider
 */
public class FileResourceSystemTest extends AbstractHttpResourceStoreTestCase {

	/*
	 * ============== AbstractHttpResourceStoreTestCase ==================
	 */

	@Override
	protected String getLocation() {
		return this.getStoreFilePath();
	}

	@Override
	protected Class<FileResourceSystemService> getResourceSystemService() {
		return FileResourceSystemService.class;
	}

}