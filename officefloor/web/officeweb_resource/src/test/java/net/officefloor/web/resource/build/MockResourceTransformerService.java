/*-
 * #%L
 * Web resources
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.resource.build;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

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

	/**
	 * Resource path.
	 */
	public String resourcePath = null;

	/**
	 * Set of existing transformed paths.
	 */
	private Set<String> transformedPaths = new HashSet<>();

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

		// Capture the resource path
		this.resourcePath = context.getPath();

		// Ensure path only transformed once
		Assert.assertFalse("Should only transform path once: " + this.resourcePath,
				this.transformedPaths.contains(this.resourcePath));
		this.transformedPaths.add(this.resourcePath);

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
