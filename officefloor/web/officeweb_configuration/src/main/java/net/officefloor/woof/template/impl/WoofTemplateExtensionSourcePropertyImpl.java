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

package net.officefloor.woof.template.impl;

import net.officefloor.woof.template.WoofTemplateExtensionSourceProperty;

/**
 * Implementation of the {@link WoofTemplateExtensionSourceProperty}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionSourcePropertyImpl implements
		WoofTemplateExtensionSourceProperty {

	/**
	 * Name of property.
	 */
	protected final String name;

	/**
	 * Label of property.
	 */
	protected final String label;

	/**
	 * Initiate with name and label of property.
	 * 
	 * @param name
	 *            Name of property.
	 * @param label
	 *            Label of property.
	 */
	public WoofTemplateExtensionSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = label;
	}

	/*
	 * =============== WoofTemplateExtensionSourceProperty =================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}
