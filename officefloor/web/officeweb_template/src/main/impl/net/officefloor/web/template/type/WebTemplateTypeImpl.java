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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.web.template.section.WebTemplateSectionSource;

/**
 * {@link WebTemplateType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateTypeImpl implements WebTemplateType {

	/**
	 * {@link WebTemplateOutputType} instances.
	 */
	private final WebTemplateOutputType[] outputs;

	/**
	 * Instantiate.
	 * 
	 * @param sectionType
	 *            {@link SectionType} loaded from the
	 *            {@link WebTemplateSectionSource}.
	 */
	public WebTemplateTypeImpl(SectionType sectionType) {

		// Create the outputs
		List<WebTemplateOutputType> outputs = new LinkedList<>();
		NEXT_OUTPUT: for (SectionOutputType output : sectionType.getSectionOutputTypes()) {

			// Determine if internal output
			if (WebTemplateSectionSource.REDIRECT_TEMPLATE_OUTPUT_NAME.equals(output.getSectionOutputName())) {
				continue NEXT_OUTPUT;
			}

			// Include the output
			outputs.add(new WebTemplateOutputTypeImpl(output));
		}
		this.outputs = outputs.toArray(new WebTemplateOutputType[outputs.size()]);
	}

	/*
	 * ================= WebTemplateType ===================
	 */

	@Override
	public WebTemplateOutputType[] getWebTemplateOutputTypes() {
		return this.outputs;
	}

	/**
	 * {@link WebTemplateOutputType} implementation.
	 */
	private static class WebTemplateOutputTypeImpl implements WebTemplateOutputType {

		/**
		 * Name.
		 */
		private final String outputName;

		/**
		 * Argument type.
		 */
		private final String argumentType;

		/**
		 * Indicates if escalation only.
		 */
		private final boolean isEscalationOnly;

		/**
		 * Annotations.
		 */
		private final Object[] annotations;

		/**
		 * Instantiate.
		 * 
		 * @param output
		 *            {@link SectionOutputType} from the
		 *            {@link WebTemplateSectionSource}.
		 */
		private WebTemplateOutputTypeImpl(SectionOutputType output) {
			this.outputName = output.getSectionOutputName();
			this.argumentType = output.getArgumentType();
			this.isEscalationOnly = output.isEscalationOnly();
			this.annotations = output.getAnnotations();
		}

		/*
		 * =============== WebTemplateOutputType ==============
		 */

		@Override
		public String getWebTemplateOutputName() {
			return this.outputName;
		}

		@Override
		public String getArgumentType() {
			return this.argumentType;
		}

		@Override
		public boolean isEscalationOnly() {
			return this.isEscalationOnly;
		}

		@Override
		public Object[] getAnnotations() {
			return this.annotations;
		}
	}

}
