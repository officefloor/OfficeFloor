package net.officefloor.web.resource.file;

import java.io.IOException;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;
import net.officefloor.web.resource.spi.ResourceSystemFactory;
import net.officefloor.web.resource.spi.ResourceSystemService;

/**
 * {@link ResourceSystemFactory} backed by files.
 * 
 * @author Daniel Sagenschneider
 */
public class FileResourceSystemService implements ResourceSystemFactory, ResourceSystemService {

	/**
	 * Protocol name.
	 */
	public static final String PROTOCOL_NAME = "file";

	/*
	 * ==================== ResourceSystemService =====================
	 */

	@Override
	public ResourceSystemFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ==================== ResourceSystemFactory =====================
	 */

	@Override
	public String getProtocolName() {
		return PROTOCOL_NAME;
	}

	@Override
	public ResourceSystem createResourceSystem(ResourceSystemContext context) throws IOException {
		return new FileResourceSystem(context);
	}

}