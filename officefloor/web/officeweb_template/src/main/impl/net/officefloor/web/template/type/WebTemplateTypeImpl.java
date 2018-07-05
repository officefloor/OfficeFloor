/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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