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
package net.officefloor.woof.model.woof;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.configuration.impl.configuration.ClassLoaderConfigurationContext;
import net.officefloor.configuration.impl.configuration.MemoryConfigurationContext;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.test.changes.AbstractChangesTestCase;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.type.HttpSecurityDependencyType;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecuritySupportingManagedObjectType;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpAuthenticationFactory;
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
	protected WoofChanges createModelOperations(WoofModel model) {
		return new WoofChangesImpl(model);
	}

	@Override
	protected void assertModels(WoofModel expected, WoofModel actual) throws Exception {

		// Determine if output XML of actual
		if (this.isPrintMessages()) {

			// Provide details of the model compare
			this.printMessage("=============== MODEL COMPARE ================");

			// Provide details of expected model
			this.printMessage("------------------ EXPECTED ------------------");
			WritableConfigurationItem expectedConfig = MemoryConfigurationContext
					.createWritableConfigurationItem("location");
			new WoofRepositoryImpl(new ModelRepositoryImpl()).storeWoof(expected, expectedConfig);
			this.printMessage(expectedConfig.getReader());

			// Provide details of actual model
			this.printMessage("------------------- ACTUAL -------------------");
			WritableConfigurationItem actualConfig = MemoryConfigurationContext
					.createWritableConfigurationItem("location");
			new WoofRepositoryImpl(new ModelRepositoryImpl()).storeWoof(actual, actualConfig);
			this.printMessage(actualConfig.getReader());
			this.printMessage("================ END COMPARE =================");
		}

		// Under take the compare
		super.assertModels(expected, actual);
	}

	/**
	 * Constructs an {@link SectionType} for testing.
	 * 
	 * @param constructor {@link SectionTypeConstructor}.
	 * @return {@link SectionType}.
	 */
	protected SectionType constructSectionType(SectionTypeConstructor constructor) {

		// Construct and return the office section
		SectionTypeContextImpl context = new SectionTypeContextImpl();
		constructor.construct(context);
		return context;
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
	protected interface SectionTypeConstructor {

		/**
		 * Constructs the {@link SectionType}.
		 * 
		 * @param context {@link SectionType}.
		 */
		void construct(SectionTypeContext context);
	}

	/**
	 * Context to construct the {@link SectionType}.
	 */
	protected interface SectionTypeContext {

		/**
		 * Adds an {@link SectionInputType}.
		 * 
		 * @param name          Name.
		 * @param parameterType Parameter type.
		 */
		void addSectionInput(String name, Class<?> parameterType);

		/**
		 * Adds an {@link SectionOutputType}.
		 * 
		 * @param name             Name.
		 * @param argumentType     Argument type.
		 * @param isEscalationOnly Flag indicating if escalation only.
		 */
		void addSectionOutput(String name, Class<?> argumentType, boolean isEscalationOnly);

		/**
		 * Adds an {@link SectionObjectType}.
		 * 
		 * @param name          Name.
		 * @param objectType    Object type.
		 * @param typeQualifier Type qualifier.
		 */
		void addSectionObject(String name, Class<?> objectType, String typeQualifier);
	}

	/**
	 * {@link SectionTypeContext} implementation.
	 */
	private class SectionTypeContextImpl implements SectionTypeContext, SectionType {

		/**
		 * {@link SectionInputType} instances.
		 */
		private final List<SectionInputType> inputs = new LinkedList<SectionInputType>();

		/**
		 * {@link SectionOutputType} instances.
		 */
		private final List<SectionOutputType> outputs = new LinkedList<SectionOutputType>();

		/**
		 * {@link SectionObjectType} instances.
		 */
		private final List<SectionObjectType> objects = new LinkedList<SectionObjectType>();

		/*
		 * ===================== SectionTypeContext =====================
		 */

		@Override
		public void addSectionInput(String name, Class<?> parameterType) {
			this.inputs.add(new SectionTypeItem(name, (parameterType == null ? null : parameterType.getName())));
		}

		@Override
		public void addSectionOutput(String name, Class<?> argumentType, boolean isEscalationOnly) {
			this.outputs.add(new SectionTypeItem(name, (argumentType == null ? null : argumentType.getName()),
					isEscalationOnly));
		}

		@Override
		public void addSectionObject(String name, Class<?> objectType, String typeQualifier) {
			this.objects.add(new SectionTypeItem(name, objectType.getName(), typeQualifier));
		}

		/*
		 * ===================== SectionType ===========================
		 */

		@Override
		public SectionInputType[] getSectionInputTypes() {
			return this.inputs.toArray(new SectionInputType[0]);
		}

		@Override
		public SectionOutputType[] getSectionOutputTypes() {
			return this.outputs.toArray(new SectionOutputType[0]);
		}

		@Override
		public SectionObjectType[] getSectionObjectTypes() {
			return this.objects.toArray(new SectionObjectType[0]);
		}
	}

	/**
	 * Item from {@link SectionType}.
	 */
	private class SectionTypeItem implements SectionInputType, SectionOutputType, SectionObjectType {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Type.
		 */
		private final String type;

		/**
		 * Type qualifier.
		 */
		private final String typeQualifier;

		/**
		 * Flag indicating if escalation only.
		 */
		private final boolean isEscalation;

		/**
		 * Initialise for {@link SectionInputType}.
		 * 
		 * @param name Name.
		 * @param type Type.
		 */
		public SectionTypeItem(String name, String type) {
			this(name, type, false);
		}

		/**
		 * Initialise for {@link SectionOutputType}.
		 * 
		 * @param name         Name.
		 * @param type         Type.
		 * @param isEscalation Flag indicating if escalation only.
		 */
		public SectionTypeItem(String name, String type, boolean isEscalation) {
			this.name = name;
			this.type = type;
			this.typeQualifier = null;
			this.isEscalation = isEscalation;
		}

		/**
		 * Initialise for {@link SectionObjectType}.
		 * 
		 * @param name          Name.
		 * @param type          Type.
		 * @param typeQualifier Type qualifier.
		 */
		public SectionTypeItem(String name, String type, String typeQualifier) {
			this.name = name;
			this.type = type;
			this.typeQualifier = typeQualifier;
			this.isEscalation = false;
		}

		/*
		 * ================ SectionInputType ======================
		 */

		@Override
		public String getSectionInputName() {
			return this.name;
		}

		@Override
		public String getParameterType() {
			return this.type;
		}

		@Override
		public Object[] getAnnotations() {
			return new Object[0];
		}

		/*
		 * ================ SectionOutputType ======================
		 */

		@Override
		public String getSectionOutputName() {
			return this.name;
		}

		@Override
		public String getArgumentType() {
			return this.type;
		}

		@Override
		public boolean isEscalationOnly() {
			return this.isEscalation;
		}

		/*
		 * ================ SectionObjectType ======================
		 */

		@Override
		public String getSectionObjectName() {
			return this.name;
		}

		@Override
		public String getObjectType() {
			return this.type;
		}

		@Override
		public String getTypeQualifier() {
			return this.typeQualifier;
		}
	}

	/**
	 * Constructs the {@link HttpSecurityType} for testing.
	 * 
	 * @param credentialsType Credentials type.
	 * @param constructor     {@link HttpSecurityTypeConstructor}.
	 * @return {@link HttpSecurityType}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <C> HttpSecurityType<HttpAuthentication<C>, HttpAccessControl, C, ?, ?> constructHttpSecurityType(
			Class<C> credentialsType, HttpSecurityTypeConstructor constructor) {

		// Construct and return the office section
		HttpSecurityTypeContextImpl<C, ?, ?> context = new HttpSecurityTypeContextImpl(credentialsType);
		constructor.construct(context);
		return context;
	}

	/**
	 * Constructor of an {@link HttpSecurityType}.
	 */
	protected interface HttpSecurityTypeConstructor {

		/**
		 * Constructs the {@link HttpSecurityType}.
		 * 
		 * @param context {@link HttpSecurityTypeContext}.
		 */
		void construct(HttpSecurityTypeContext context);
	}

	/**
	 * Context to construct the {@link HttpSecurityType}.
	 */
	protected interface HttpSecurityTypeContext {

		/**
		 * Adds a {@link HttpSecurityDependencyType}.
		 * 
		 * @param dependencyName Name of dependency.
		 * @param dependencyType Dependency type.
		 * @param typeQualifier  Qualifier for dependency type.
		 * @param key            Dependency key.
		 */
		void addDependency(String dependencyName, Class<?> dependencyType, String typeQualifier, Enum<?> key);

		/**
		 * Adds a {@link HttpSecurityFlowType}.
		 * 
		 * @param flowName     Name of flow.
		 * @param argumentType Argument to flow.
		 * @param key          Flow key.
		 */
		void addFlow(String flowName, Class<?> argumentType, Enum<?> key);
	}

	/**
	 * {@link HttpSecurityTypeContext} implementation.
	 */
	private class HttpSecurityTypeContextImpl<C, O extends Enum<O>, F extends Enum<F>>
			implements HttpSecurityTypeContext, HttpSecurityType<HttpAuthentication<C>, HttpAccessControl, C, O, F> {

		/**
		 * Type of credentials.
		 */
		private final Class<C> credentialsType;

		/**
		 * {@link HttpSecurityDependencyType} instances.
		 */
		private final List<HttpSecurityDependencyType<?>> dependencies = new LinkedList<HttpSecurityDependencyType<?>>();

		/**
		 * {@link HttpSecurityFlowType} instances.
		 */
		private final List<HttpSecurityFlowType<?>> flows = new LinkedList<HttpSecurityFlowType<?>>();

		/**
		 * Initiate.
		 * 
		 * @param credentialsType Type of credentials.
		 */
		public HttpSecurityTypeContextImpl(Class<C> credentialsType) {
			this.credentialsType = credentialsType;
		}

		/*
		 * ================== HttpSecurityTypeContext ===================
		 */

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void addDependency(String dependencyName, Class<?> dependencyType, String typeQualifier, Enum<?> key) {
			// Create and register the dependency (using next index)
			this.dependencies.add(new HttpSecurityTypeItem(dependencyName, dependencyType, typeQualifier, key,
					this.dependencies.size()));
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void addFlow(String flowName, Class<?> argumentType, Enum<?> key) {
			// Create and register the flow (using next index)
			this.flows.add(new HttpSecurityTypeItem(flowName, argumentType, null, key, this.flows.size()));
		}

		/*
		 * ================== HttpSecurityType ============================
		 */

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class<HttpAuthentication<C>> getAuthenticationType() {
			return (Class) HttpAuthentication.class;
		}

		@Override
		public HttpAuthenticationFactory<HttpAuthentication<C>, C> getHttpAuthenticationFactory() {
			return null;
		}

		@Override
		public Class<HttpAccessControl> getAccessControlType() {
			return HttpAccessControl.class;
		}

		@Override
		public HttpAccessControlFactory<HttpAccessControl> getHttpAccessControlFactory() {
			return null;
		}

		@Override
		public Class<C> getCredentialsType() {
			return this.credentialsType;
		}

		@Override
		@SuppressWarnings("unchecked")
		public HttpSecurityDependencyType<O>[] getDependencyTypes() {
			return this.dependencies.toArray(new HttpSecurityDependencyType[this.dependencies.size()]);
		}

		@Override
		@SuppressWarnings("unchecked")
		public HttpSecurityFlowType<F>[] getFlowTypes() {
			return this.flows.toArray(new HttpSecurityFlowType[this.flows.size()]);
		}

		@Override
		public HttpSecuritySupportingManagedObjectType<?>[] getSupportingManagedObjectTypes() {
			return new HttpSecuritySupportingManagedObjectType[0];
		}
	}

	/**
	 * Item for {@link HttpSecurityDependencyType} and {@link HttpSecurityFlowType}.
	 */
	private class HttpSecurityTypeItem<E extends Enum<E>>
			implements HttpSecurityDependencyType<E>, HttpSecurityFlowType<E> {

		/**
		 * Name of item.
		 */
		private final String itemName;

		/**
		 * Type for item.
		 */
		private final Class<?> itemType;

		/**
		 * Qualifier for item type.
		 */
		private final String typeQualifier;

		/**
		 * Key.
		 */
		private final E key;

		/**
		 * Index.
		 */
		private final int index;

		/**
		 * Initiate.
		 * 
		 * @param itemName      Name of item.
		 * @param itemType      Type for item.
		 * @param typeQualifier Qualifier for item type.
		 * @param key           Key.
		 * @param index         Index.
		 */
		public HttpSecurityTypeItem(String itemName, Class<?> itemType, String typeQualifier, E key, int index) {
			this.itemName = itemName;
			this.itemType = itemType;
			this.typeQualifier = typeQualifier;
			this.key = key;
			this.index = index;
		}

		/*
		 * ============== Item common methods ==============
		 */

		@Override
		public int getIndex() {
			return this.index;
		}

		@Override
		public E getKey() {
			return this.key;
		}

		/*
		 * ============== HttpSecurityDependencyType ==============
		 */

		@Override
		public String getDependencyName() {
			return this.itemName;
		}

		@Override
		public Class<?> getDependencyType() {
			return this.itemType;
		}

		@Override
		public String getTypeQualifier() {
			return this.typeQualifier;
		}

		/*
		 * ============== HttpSecurityFlowType ==============
		 */

		@Override
		public String getFlowName() {
			return this.itemName;
		}

		@Override
		public Class<?> getArgumentType() {
			return this.itemType;
		}
	}

}