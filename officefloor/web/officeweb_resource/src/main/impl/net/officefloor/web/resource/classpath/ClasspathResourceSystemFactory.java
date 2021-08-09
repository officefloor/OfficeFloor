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
