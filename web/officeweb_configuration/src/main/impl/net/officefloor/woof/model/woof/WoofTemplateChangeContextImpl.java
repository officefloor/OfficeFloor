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
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;

/**
 * {@link WoofTemplateChangeContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateChangeContextImpl extends SourceContextImpl implements WoofTemplateChangeContext {

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * {@link WoofChangeIssues}.
	 */
	private final WoofChangeIssues issues;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType        Indicates if loading type.
	 * @param sourceContext        {@link SourceContext}.
	 * @param configurationContext {@link ConfigurationContext}.
	 * @param issues               {@link WoofChangeIssues}.
	 */
	public WoofTemplateChangeContextImpl(boolean isLoadingType, SourceContext sourceContext,
			ConfigurationContext configurationContext, WoofChangeIssues issues) {
		super(sourceContext.getName(), isLoadingType, null, sourceContext, new SourcePropertiesImpl());
		this.configurationContext = configurationContext;
		this.issues = issues;
	}

	/*
	 * =============== WoofTemplateChangeContext =======================
	 */

	@Override
	public ConfigurationContext getConfigurationContext() {
		return this.configurationContext;
	}

	@Override
	public WoofChangeIssues getWoofChangeIssues() {
		return this.issues;
	}

}
