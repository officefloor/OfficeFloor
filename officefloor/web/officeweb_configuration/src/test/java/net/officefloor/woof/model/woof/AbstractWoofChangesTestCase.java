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

import junit.framework.TestCase;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoaderUtil;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.ProcedureTypeBuilder;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.test.section.SectionLoaderUtil;
import net.officefloor.compile.test.section.SectionTypeBuilder;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.ClassLoaderConfigurationContext;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.test.changes.AbstractChangesTestCase;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;
import net.officefloor.web.template.type.WebTemplateType;
import net.officefloor.web.template.type.WebTemplateTypeImpl;
import net.officefloor.woof.template.WoofTemplateExtensionLoaderUtil;

/**
 * Abstract {@link WoofChanges} {@link TestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWoofChangesTestCase extends AbstractChangesTestCase<WoofModel, WoofChanges> {

	/**
	 * Creates the {@link WoofTemplateChangeContext}.
	 * 
	 * @return {@link WoofTemplateChangeContext}.
	 */
	private static WoofTemplateChangeContext createWoofTemplateChangeContext() {

		// Create the context
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		ClassLoader classLoader = compiler.getClassLoader();
		ConfigurationContext configurationContext = new ClassLoaderConfigurationContext(classLoader, null);
		WoofChangeIssues issues = WoofTemplateExtensionLoaderUtil.getWoofChangeIssues();
		WoofTemplateChangeContext context = new WoofTemplateChangeContextImpl(false, compiler.createRootSourceContext(),
				configurationContext, issues);

		// Return the context
		return context;
	}

	/**
	 * {@link WoofTemplateChangeContext}.
	 */
	private WoofTemplateChangeContext changeContext = createWoofTemplateChangeContext();

	/**
	 * Initiate.
	 */
	public AbstractWoofChangesTestCase() {
	}

	/**
	 * Initiate.
	 * 
	 * @param isSpecificSetupFilePerTest Flags if there is a specific setup file per
	 *                                   test.
	 */
	public AbstractWoofChangesTestCase(boolean isSpecificSetupFilePerTest) {
		super(isSpecificSetupFilePerTest);
	}

	/**
	 * Obtains the {@link WoofTemplateChangeContext}.
	 * 
	 * @return {@link WoofTemplateChangeContext}.
	 */
	protected WoofTemplateChangeContext getWoofTemplateChangeContext() {
		return this.changeContext;
	}

	/*
	 * =================== AbstractOperationsTestCase ========================
	 */

	@Override
	protected String getModelFileExtension() {
		return ".woof.xml";
	}

	@Override
	protected WoofModel retrieveModel(ConfigurationItem configurationItem) throws Exception {
		WoofModel woof = new WoofModel();
		new WoofRepositoryImpl(new ModelRepositoryImpl()).retrieveWoof(woof, configurationItem);
		return woof;
	}

	@Override
	protected void storeModel(WoofModel model, WritableConfigurationItem configurationItem) throws Exception {
		new WoofRepositoryImpl(new ModelRepositoryImpl()).storeWoof(model, configurationItem);
	}

	@Override
	protected WoofChanges createModelOperations(WoofModel model) {
		return new WoofChangesImpl(model);
	}

	/**
	 * Constructs an {@link SectionType} for testing.
	 * 
	 * @param constructor {@link SectionTypeConstructor}.
	 * @return {@link SectionType}.
	 */
	protected SectionType constructSectionType(SectionTypeConstructor constructor) {

		// Construct and return the office section
		SectionTypeBuilder builder = SectionLoaderUtil.createSectionTypeBuilder();
		if (constructor != null) {
			constructor.construct(builder);
		}
		return SectionLoaderUtil.buildSectionType(builder.getSectionDesigner());
	}

	/**
	 * Constructs the {@link WebTemplateType} for testing.
	 * 
	 * @param constructor {@link SectionTypeConstructor}.
	 * @return {@link WebTemplateType}.
	 */
	protected WebTemplateType constructWebTemplateType(SectionTypeConstructor constructor) {

		// Construct the section type
		SectionType sectionType = this.constructSectionType(constructor);

		// Return the web template type
		return new WebTemplateTypeImpl(sectionType);
	}

	/**
	 * Constructor of an {@link SectionType}.
	 */
	@FunctionalInterface
	protected interface SectionTypeConstructor {

		/**
		 * Constructs the {@link SectionType}.
		 * 
		 * @param context {@link SectionType}.
		 */
		void construct(SectionTypeBuilder builder);
	}

	/**
	 * Constructs the {@link HttpSecurityType} for testing.
	 * 
	 * @param credentialsType Credentials type.
	 * @param constructor     {@link HttpSecurityTypeConstructor}.
	 * @return {@link HttpSecurityType}.
	 */
	protected <C> HttpSecurityType<HttpAuthentication<C>, HttpAccessControl, C, ?, ?> constructHttpSecurityType(
			Class<C> credentialsType, HttpSecurityTypeConstructor constructor) {

		// Construct and return the office section
		HttpSecurityTypeBuilder builder = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		builder.setCredentialsClass(credentialsType);
		if (constructor != null) {
			constructor.construct(builder);
		}
		return builder.build();
	}

	/**
	 * Constructor of an {@link HttpSecurityType}.
	 */
	@FunctionalInterface
	protected interface HttpSecurityTypeConstructor {

		/**
		 * Constructs the {@link HttpSecurityType}.
		 * 
		 * @param context {@link HttpSecurityTypeBuilder}.
		 */
		void construct(HttpSecurityTypeBuilder builder);
	}

	/**
	 * Constructs the {@link ProcedureType} for testing.
	 * 
	 * @param procedureName {@link Procedure} name.
	 * @param parameterType Parameter type.
	 * @param constructor   {@link ProcedureTypeConstructor}.
	 * @return {@link HttpSecurityType}.
	 */
	protected ProcedureType constructProcedureType(String procedureName, Class<?> parameterType,
			ProcedureTypeConstructor constructor) {

		// Construct and return the procedure
		ProcedureTypeBuilder builder = ProcedureLoaderUtil.createProcedureTypeBuilder(procedureName, parameterType);
		if (constructor != null) {
			constructor.construct(builder);
		}
		return builder.build();
	}

	/**
	 * Constructor of an {@link ProcedureType}.
	 */
	@FunctionalInterface
	protected interface ProcedureTypeConstructor {

		/**
		 * Constructs the {@link ProcedureType}.
		 * 
		 * @param context {@link ProcedureTypeBuilder}.
		 */
		void construct(ProcedureTypeBuilder context);
	}

}
