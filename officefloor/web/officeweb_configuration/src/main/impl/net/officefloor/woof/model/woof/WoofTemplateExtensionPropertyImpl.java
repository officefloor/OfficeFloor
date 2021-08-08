/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.model.woof;

/**
 * {@link WoofTemplateExtensionProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionPropertyImpl implements
		WoofTemplateExtensionProperty {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Value.
	 */
	private final String value;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param value
	 *            Value.
	 */
	public WoofTemplateExtensionPropertyImpl(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/*
	 * ================== WoofTemplateExtensionProperty =====================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getValue() {
		return this.value;
	}

}
