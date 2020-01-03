package net.officefloor.web.resource.classpath;

import java.io.IOException;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;
import net.officefloor.web.resource.spi.ResourceSystemFactory;
import net.officefloor.web.resource.spi.ResourceSystemService;

/**
 * {@link ResourceSystemFactory} to create a {@link ResourceSystem} from the
 * class path.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathResourceSystemService implements ResourceSystemFactory, ResourceSystemService {

	/**
	 * Protocol name.
	 */
	public static final String PROTOCOL_NAME = "classpath";

	/*
	 * ====================== ResourceSystemService =======================
	 */

	@Override
	public ResourceSystemFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ===================== ResourceSystemFactory =======================
	 */

	@Override
	public String getProtocolName() {
		return PROTOCOL_NAME;
	}

	@Override
	public ResourceSystem createResourceSystem(ResourceSystemContext context) throws IOException {
		return new ClasspathResourceSystem(context);
	}

}