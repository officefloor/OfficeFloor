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

import net.officefloor.woof.template.WoofTemplateExtensionSource;

/**
 * {@link WoofTemplateExtension} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionImpl implements WoofTemplateExtension {

	/**
	 * {@link WoofTemplateExtensionSource} class name.
	 */
	private final String sourceClassName;

	/**
	 * {@link WoofTemplateExtensionProperty} instances.
	 */
	private final WoofTemplateExtensionProperty[] properties;

	/**
	 * Initiate.
	 * 
	 * @param sourceClassName
	 *            {@link WoofTemplateExtensionSource} class name.
	 * @param properties
	 *            {@link WoofTemplateExtensionProperty} instances.
	 */
	public WoofTemplateExtensionImpl(String sourceClassName,
			WoofTemplateExtensionProperty... properties) {
		this.sourceClassName = sourceClassName;
		this.properties = properties;
	}

	/*
	 * ====================== WoofTemplateExtension ============================
	 */

	@Override
	public String getWoofTemplateExtensionSourceClassName() {
		return this.sourceClassName;
	}

	@Override
	public WoofTemplateExtensionProperty[] getWoofTemplateExtensionProperties() {
		return this.properties;
	}

}
