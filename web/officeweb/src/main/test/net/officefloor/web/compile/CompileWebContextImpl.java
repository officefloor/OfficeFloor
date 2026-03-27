/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.compile;

import java.util.function.Consumer;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.test.officefloor.CompileOfficeContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.variable.Var;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.HttpUrlContinuation;
import net.officefloor.web.build.WebArchitect;

/**
 * {@link CompileWebContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileWebContextImpl implements CompileWebContext {

	/**
	 * {@link CompileOfficeContext}.
	 */
	private final CompileOfficeContext officeContext;

	/**
	 * {@link OfficeArchitect}.
	 */
	private final OfficeArchitect officeArchitect;

	/**
	 * {@link WebArchitect}.
	 */
	private final WebArchitect webArchitect;

	/**
	 * Instantiate.
	 * 
	 * @param officeContext {@link CompileOfficeContext}.
	 * @param webArchitect  {@link WebArchitect}.
	 */
	public CompileWebContextImpl(CompileOfficeContext officeContext, WebArchitect webArchitect) {
		this.officeContext = officeContext;
		this.officeArchitect = this.officeContext.getOfficeArchitect();
		this.webArchitect = webArchitect;
	}

	/*
	 * ================== CompileWebContext ==================
	 */

	@Override
	public WebArchitect getWebArchitect() {
		return this.webArchitect;
	}

	/*
	 * ================== CompileOfficeContext ==================
	 */

	@Override
	public OfficeArchitect getOfficeArchitect() {
		return this.officeArchitect;
	}

	@Override
	public OfficeSourceContext getOfficeSourceContext() {
		return this.officeContext.getOfficeSourceContext();
	}

	@Override
	public OfficeManagedObject addManagedObject(String managedObjectName, Class<?> managedObjectClass,
			ManagedObjectScope scope) {
		return this.officeContext.addManagedObject(managedObjectName, managedObjectClass, scope);
	}

	@Override
	public OfficeSection addSection(String sectionName, Class<?> sectionClass) {
		return this.officeContext.addSection(sectionName, sectionClass);
	}

	@Override
	public <T> void variable(String qualifier, Class<T> type, Consumer<Var<T>> compileVar) {
		this.officeContext.variable(qualifier, type, compileVar);
	}

	@Override
	public OfficeSection getOfficeSection() {
		return this.officeContext.getOfficeSection();
	}

	@Override
	public OfficeSection overrideSection(Class<? extends SectionSource> sectionSourceClass, String sectionLocation) {
		return this.officeContext.overrideSection(sectionSourceClass, sectionLocation);
	}

	@Override
	public HttpInput link(boolean isSecure, String httpMethodName, String applicationPath, Class<?> sectionClass) {

		// Add the section
		OfficeSection section = this.addSection(httpMethodName + "_" + applicationPath, sectionClass);

		// Create the link to the section service method
		HttpInput input = this.webArchitect.getHttpInput(isSecure, httpMethodName, applicationPath);
		this.officeArchitect.link(input.getInput(), section.getOfficeSectionInput("service"));
		return input;
	}

	@Override
	public HttpUrlContinuation link(boolean isSecure, String applicationPath, Class<?> sectionClass) {

		// Add the section
		OfficeSection section = this.addSection("GET_" + applicationPath, sectionClass);

		// Return the link to the section service method
		HttpUrlContinuation continuation = this.webArchitect.getHttpInput(isSecure, applicationPath);
		this.officeArchitect.link(continuation.getInput(), section.getOfficeSectionInput("service"));
		return continuation;
	}
}
