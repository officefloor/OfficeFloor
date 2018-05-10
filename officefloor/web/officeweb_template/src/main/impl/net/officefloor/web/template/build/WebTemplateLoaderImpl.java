/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.web.template.build;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.web.route.WebRouterBuilder;
import net.officefloor.web.template.extension.WebTemplateExtension;
import net.officefloor.web.template.section.WebTemplateSectionSource;
import net.officefloor.web.template.section.WebTemplateSectionSource.WebTemplateEnhancements;

/**
 * {@link WebTemplateLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateLoaderImpl extends AbstractWebTemplateFactory implements WebTemplateLoader, SourceIssues {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * Instantiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 */
	public WebTemplateLoaderImpl(OfficeFloorCompiler compiler) {
		this.compiler = compiler;
	}

	/*
	 * ============ SourceIssues ================
	 */

	@Override
	public CompileError addIssue(String issueDescription) {
		return this.compiler.getCompilerIssues().addIssue(this.compiler, issueDescription);
	}

	@Override
	public CompileError addIssue(String issueDescription, Throwable cause) {
		return this.compiler.getCompilerIssues().addIssue(this.compiler, issueDescription, cause);
	}

	/*
	 * ============ WebTemplateFactory ===============
	 */

	@Override
	protected PropertyList createPropertyList() {
		return this.compiler.createPropertyList();
	}

	@Override
	protected SourceIssues getSourceIssues() {
		return this;
	}

	@Override
	protected boolean isPathParameters(String applicationPath) {
		return WebRouterBuilder.isPathParameters(applicationPath);
	}

	@Override
	protected WebTemplate addTemplate(boolean isSecure, String applicationPath, PropertyList properties) {
		return new TypeWebTemplate(isSecure, applicationPath, properties, this);
	}

	/*
	 * ============ WebTemplateLoader ================
	 */

	@Override
	public SectionType loadWebTemplateType(WebTemplate webTemplate) {

		// Ensure appropriate type
		if (!(webTemplate instanceof TypeWebTemplate)) {
			throw new IllegalStateException("Template must be created via " + WebTemplateLoader.class.getSimpleName());
		}
		TypeWebTemplate template = (TypeWebTemplate) webTemplate;

		// Determine if path parameters
		boolean isPathParameters = this.isPathParameters(template.applicationPath);

		// Load the properties
		PropertyList properties = template.loadProperties(isPathParameters);

		// Obtain the section loader to load the type
		SectionLoader sectionLoader = this.compiler.getSectionLoader();

		// Load and return the section (web template) type
		return WebTemplateSectionSource.executeWithEnhancements(template.enhancements, () -> sectionLoader
				.loadSectionType(WebTemplateSectionSource.class, template.applicationPath, properties));
	}

	/**
	 * {@link WebTemplate} implementation for determining type.
	 */
	private class TypeWebTemplate extends AbstractWebTemplate implements OfficeFlowSinkNode, OfficeFlowSourceNode {

		/**
		 * {@link WebTemplateEnhancements}.
		 */
		private final WebTemplateEnhancements enhancements = new WebTemplateEnhancements();

		/**
		 * Instantiate.
		 * 
		 * @param isSecure
		 *            Indicates if secure.
		 * @param applicationPath
		 *            Application path.
		 * @param properties
		 *            {@link PropertyList}.
		 * @param sourceIssues
		 *            {@link SourceIssues}.
		 */
		private TypeWebTemplate(boolean isSecure, String applicationPath, PropertyList properties,
				SourceIssues sourceIssues) {
			super(isSecure, applicationPath, properties, sourceIssues);
		}

		/*
		 * ============== WebTemplate =================
		 */

		@Override
		public WebTemplateExtensionBuilder addExtension(WebTemplateExtension extension) {
			return this.enhancements.createWebTempalteExtensionBuilder(extension,
					WebTemplateLoaderImpl.this.createPropertyList());
		}

		@Override
		public OfficeFlowSinkNode getRender(String valuesTypeName) {
			return this;
		}

		@Override
		public OfficeFlowSourceNode getOutput(String outputName) {
			return this;
		}

		@Override
		public WebTemplate addGovernance(OfficeGovernance governance) {
			return this;
		}
	}

}