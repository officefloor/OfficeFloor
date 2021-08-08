/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct.executive;

import net.officefloor.frame.api.build.ExecutiveBuilder;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.ExecutiveConfiguration;

/**
 * Implements the {@link ExecutiveBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveBuilderImpl<XS extends ExecutiveSource>
		implements ExecutiveBuilder<XS>, ExecutiveConfiguration<XS> {

	/**
	 * {@link ExecutiveSource}.
	 */
	private final XS executiveSource;

	/**
	 * {@link Class} of the {@link ExecutiveSource}.
	 */
	private final Class<XS> executiveSourceClass;

	/**
	 * {@link SourceProperties} for initialising the {@link ExecutiveSource}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * Initiate.
	 * 
	 * @param executiveSource {@link ExecutiveSource}.
	 */
	public ExecutiveBuilderImpl(XS executiveSource) {
		this.executiveSource = executiveSource;
		this.executiveSourceClass = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param executiveSourceClass {@link Class} of the {@link ExecutiveSource}.
	 */
	public ExecutiveBuilderImpl(Class<XS> executiveSourceClass) {
		this.executiveSource = null;
		this.executiveSourceClass = executiveSourceClass;
	}

	/*
	 * ================ ExecutiveBuilder =====================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	/*
	 * ============== ExecutiveConfiguration ===================
	 */

	@Override
	public XS getExecutiveSource() {
		return this.executiveSource;
	}

	@Override
	public Class<XS> getExecutiveSourceClass() {
		return this.executiveSourceClass;
	}

	@Override
	public SourceProperties getProperties() {
		return this.properties;
	}

}
