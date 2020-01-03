package net.officefloor.web.resource.build;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.resource.spi.ResourceTransformer;
import net.officefloor.web.resource.spi.ResourceTransformerContext;
import net.officefloor.web.resource.spi.ResourceTransformerFactory;
import net.officefloor.web.resource.spi.ResourceTransformerService;

/**
 * Mock {@link ResourceTransformerFactory} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockResourceTransformerService
		implements ResourceTransformerService, ResourceTransformerFactory, ResourceTransformer {

	/*
	 * ================== ResourceTransformerService ================
	 */

	@Override
	public ResourceTransformerFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== ResourceTransformerFactory ================
	 */

	@Override
	public String getName() {
		return "mock";
	}

	@Override
	public ResourceTransformer createResourceTransformer() {
		return this;
	}

	/*
	 * ===================== ResourceTransformer =====================
	 */

	@Override
	public void transform(ResourceTransformerContext context) throws IOException {

		// Obtain the resource to transform
		Path resource = context.getResource();

		// Provide content
		Path transformed = context.createFile();
		Files.copy(resource, transformed, StandardCopyOption.REPLACE_EXISTING);
		Writer writer = Files.newBufferedWriter(transformed, StandardOpenOption.APPEND);
		writer.write(" - transformed");
		writer.close();

		// Provide transformed file
		context.setTransformedResource(transformed);
	}

}