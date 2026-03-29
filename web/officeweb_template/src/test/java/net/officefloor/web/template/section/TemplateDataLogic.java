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

package net.officefloor.web.template.section;

import net.officefloor.plugin.section.clazz.Next;

/**
 * Logic for template with Data suffix on method name.
 * 
 * @author Daniel Sagenschneider
 */
public class TemplateDataLogic {

	/**
	 * Obtains the bean for starting template.
	 * 
	 * @return Starting template bean.
	 */
	public TemplateDataLogic getTemplateData() {
		return this;
	}

	/**
	 * Obtains the message.
	 * 
	 * @return Message.
	 */
	public String getMessage() {
		return "hello world";
	}

	/**
	 * Obtains the bean for section data.
	 * 
	 * @return Section data.
	 */
	public TemplateDataLogic getSectionData() {
		return this;
	}

	/**
	 * Obtains the description for the section.
	 * 
	 * @return Description.
	 */
	public String getDescription() {
		return "section data";
	}

	/**
	 * Required to have output flow for integration testing setup.
	 */
	@Next("doExternalFlow")
	public void requiredForIntegration() {
	}

}
