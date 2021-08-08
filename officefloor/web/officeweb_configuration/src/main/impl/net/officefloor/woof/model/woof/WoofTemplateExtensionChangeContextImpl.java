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

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.woof.template.WoofTemplateExtensionChangeContext;
import net.officefloor.woof.template.WoofTemplateExtensionConfiguration;

/**
 * {@link WoofTemplateExtensionChangeContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateExtensionChangeContextImpl extends SourceContextImpl
		implements WoofTemplateExtensionChangeContext {

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * Old {@link WoofTemplateExtensionConfiguration}.
	 */
	private final WoofTemplateExtensionConfiguration oldConfiguration;

	/**
	 * New {@link WoofTemplateExtensionConfiguration}.
	 */
	private final WoofTemplateExtensionConfiguration newConfiguration;

	/**
	 * {@link WoofChangeIssues}.
	 */
	private final WoofChangeIssues issues;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType        Indicates if loading type.
	 * @param sourceContext        {@link SourceContext}.
	 * @param oldUri               Old URI.
	 * @param oldProperties        Old {@link SourceProperties}.
	 * @param newUri               New URI.
	 * @param newProperties        New {@link SourceProperties}.
	 * @param configurationContext {@link ConfigurationContext}.
	 * @param issues               {@link WoofChangeIssues}.
	 */
	public WoofTemplateExtensionChangeContextImpl(boolean isLoadingType, SourceContext sourceContext, String oldUri,
			SourceProperties oldProperties, String newUri, SourceProperties newProperties,
			ConfigurationContext configurationContext, WoofChangeIssues issues) {
		super(sourceContext.getName(), isLoadingType, null, sourceContext,
				(newUri == null ? null : newProperties));

		// Store state
		this.configurationContext = configurationContext;
		this.issues = issues;

		// Create the old configuration
		if (oldUri == null) {
			this.oldConfiguration = null; // no configuration
		} else {
			this.oldConfiguration = new WoofTemplateExtensionConfigurationImpl(oldUri, oldProperties);
		}

		// Create the new configuration
		if (newUri == null) {
			this.newConfiguration = null; // no configuration
		} else {
			this.newConfiguration = new WoofTemplateExtensionConfigurationImpl(newUri, newProperties);
		}
	}

	/*
	 * ================ WoofTemplateExtensionChangeContext ====================
	 */

	@Override
	public WoofTemplateExtensionConfiguration getOldConfiguration() {
		return this.oldConfiguration;
	}

	@Override
	public WoofTemplateExtensionConfiguration getNewConfiguration() {
		return this.newConfiguration;
	}

	@Override
	public ConfigurationContext getConfigurationContext() {
		return this.configurationContext;
	}

	@Override
	public WoofChangeIssues getWoofChangeIssues() {
		return this.issues;
	}

	/**
	 * {@link WoofTemplateExtensionConfiguration} implementation.
	 */
	private static class WoofTemplateExtensionConfigurationImpl extends SourcePropertiesImpl
			implements WoofTemplateExtensionConfiguration {

		/**
		 * URI.
		 */
		private final String uri;

		/**
		 * Initiate.
		 * 
		 * @param uri              URI.
		 * @param sourceProperties {@link SourceProperties}.
		 */
		public WoofTemplateExtensionConfigurationImpl(String uri, SourceProperties sourceProperties) {
			super(sourceProperties);
			this.uri = uri;
		}

		/*
		 * ================ WoofTemplateExtensionConfiguration =================
		 */

		@Override
		public String getApplicationPath() {
			return this.uri;
		}
	}

}
