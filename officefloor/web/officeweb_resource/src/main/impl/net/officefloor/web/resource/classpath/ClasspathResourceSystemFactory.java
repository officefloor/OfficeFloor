package net.officefloor.web.resource.classpath;

import java.io.IOException;

import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;
import net.officefloor.web.resource.spi.ResourceSystemFactory;

/**
 * Classpath {@link ResourceSystemFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClasspathResourceSystemFactory implements ResourceSystemFactory {

	/**
	 * Protocol name.
	 */
	public static final String PROTOCOL_NAME = "classpath";

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Instantiate.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 */
	public ClasspathResourceSystemFactory(ClassLoader classLoader) {
		this.classLoader = classLoader;
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
		return new ClasspathResourceSystem(context, this.classLoader);
	}

}