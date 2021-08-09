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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerRunnable;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.web.route.WebRouterBuilder;
import net.officefloor.web.template.build.AbstractWebTemplate;
import net.officefloor.web.template.build.AbstractWebTemplateFactory;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.section.WebTemplateSectionSource;

/**
 * {@link WebTemplateLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateLoaderImpl extends AbstractWebTemplateFactory
		implements OfficeFloorCompilerRunnable<WebTemplateLoader>, WebTemplateLoader, SourceIssues {

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * Instantiate for {@link OfficeFloorCompilerRunnable}.
	 */
	public WebTemplateLoaderImpl() {
		this.compiler = null;
	}

	/**
	 * Instantiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 */
	private WebTemplateLoaderImpl(OfficeFloorCompiler compiler) {
		this.compiler = compiler;
	}

	/*
	 * ===== OfficeFloorCompilerRunnable ========
	 */

	@Override
	public WebTemplateLoader run(OfficeFloorCompiler compiler, Object[] parameters) throws Exception {
		return new WebTemplateLoaderImpl(compiler);
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
	public WebTemplateType loadWebTemplateType(WebTemplate webTemplate) {

		// Ensure appropriate type
		if (!(webTemplate instanceof TypeWebTemplate)) {
			throw new IllegalStateException("Template must be created via " + WebTemplateLoader.class.getSimpleName());
		}
		TypeWebTemplate template = (TypeWebTemplate) webTemplate;

		// Determine if path parameters
		boolean isPathParameters = this.isPathParameters(template.getApplicationPath());

		// Load the properties
		PropertyList properties = template.loadProperties(isPathParameters);

		// Obtain the section loader and load section type
		SectionLoader sectionLoader = this.compiler.getSectionLoader();
		SectionType sectionType = sectionLoader.loadSectionType(template.getWebTemplateSectionSource(),
				template.getApplicationPath(), properties);

		// Load and return the section (web template) type
		return new WebTemplateTypeImpl(sectionType);
	}

	/**
	 * {@link WebTemplate} implementation for determining type.
	 */
	private class TypeWebTemplate extends AbstractWebTemplate implements OfficeFlowSinkNode, OfficeFlowSourceNode {

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
			super(new WebTemplateSectionSource(), isSecure, applicationPath, properties, sourceIssues);
		}

		/*
		 * ============== WebTemplate =================
		 */

		@Override
		protected PropertyList createPropertyList() {
			return WebTemplateLoaderImpl.this.createPropertyList();
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
