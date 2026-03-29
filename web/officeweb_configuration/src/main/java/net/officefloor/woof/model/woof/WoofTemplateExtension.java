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
 * Extension for a {@link WoofTemplateModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofTemplateExtension {

	/**
	 * Obtains the {@link WoofTemplateExtensionSource} fully qualified class
	 * name providing the extension of the {@link WoofTemplateModel}.
	 * 
	 * @return {@link WoofTemplateExtensionSource} fully qualified class name
	 *         providing the extension of the {@link WoofTemplateModel}.
	 */
	String getWoofTemplateExtensionSourceClassName();

	/**
	 * Obtains the {@link WoofTemplateExtensionProperty} instances to configure
	 * the extension of the {@link WoofTemplateModel}.
	 * 
	 * @return {@link WoofTemplateExtensionProperty} instances to configure the
	 *         extension of the {@link WoofTemplateModel}.
	 */
	WoofTemplateExtensionProperty[] getWoofTemplateExtensionProperties();

}
