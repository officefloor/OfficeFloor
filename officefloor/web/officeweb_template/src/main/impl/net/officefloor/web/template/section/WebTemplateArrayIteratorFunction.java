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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.web.template.section.WebTemplateSectionSource.WebTemplateArrayIteratorManagedFunctionSource;

/**
 * {@link ManagedFunction} to iterate over an array to render content.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateArrayIteratorFunction extends
		StaticManagedFunction<WebTemplateArrayIteratorFunction.DependencyKeys, WebTemplateArrayIteratorFunction.FlowKeys> {

	/**
	 * {@link WebTemplateArrayIteratorManagedFunctionSource} dependency keys.
	 */
	public static enum DependencyKeys {
		ARRAY
	}

	/**
	 * {@link WebTemplateArrayIteratorManagedFunctionSource} flow keys.
	 */
	public static enum FlowKeys {
		RENDER_ELEMENT, CONTINUE_TEMPLATE
	}

	/*
	 * ======================== ManagedFunction ========================
	 */

	@Override
	public void execute(ManagedFunctionContext<DependencyKeys, FlowKeys> context) {

		// Obtain the array
		Object[] array = (Object[]) context.getObject(DependencyKeys.ARRAY);
		if (array == null) {
			return; // no array, no rendering
		}

		// Iterate over the array rendering the elements
		for (Object element : array) {
			context.doFlow(FlowKeys.RENDER_ELEMENT, element, null);
		}

		// Continue the template
		context.doFlow(FlowKeys.CONTINUE_TEMPLATE, null, null);
	}

}
