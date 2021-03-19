/*-
 * #%L
 * Web resources
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
