/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.type;

import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.build.WebTemplateFactory;

/**
 * Loads the type for the {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateLoader extends WebTemplateFactory {

	/**
	 * Loads the {@link WebTemplateType} for the {@link WebTemplate}.
	 * 
	 * @param template
	 *            Configured {@link WebTemplate} to provide the type information.
	 * @return {@link WebTemplateType} for the {@link WebTemplate}.
	 */
	WebTemplateType loadWebTemplateType(WebTemplate template);

}
