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

package net.officefloor.test.system;

/**
 * Abstract functionality for overriding the {@link System#getProperty(String)}
 * values in tests.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractSystemPropertiesOverride<I extends AbstractExternalOverride<I>>
		extends AbstractExternalOverride<I> {

	/**
	 * Instantiate.
	 * 
	 * @param nameValuePairs Initial {@link System} property name/value pairs.
	 */
	public AbstractSystemPropertiesOverride(String... nameValuePairs) {
		super(nameValuePairs);
	}

	/*
	 * ==================== AbstractExternalOverride =====================
	 */

	@Override
	protected String get(String name) {
		return System.getProperty(name);
	}

	@Override
	protected void set(String name, String value) {
		System.setProperty(name, value);
	}

	@Override
	protected void clear(String name) {
		System.clearProperty(name);
	}

}
