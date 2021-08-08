/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.model.impl.section;

import net.officefloor.compile.impl.managedfunction.FunctionNamespaceTypeImpl;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionEscalationType;
import net.officefloor.compile.managedfunction.ManagedFunctionFlowType;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.configuration.WritableConfigurationItem;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.section.SectionChanges;
import net.officefloor.model.section.SectionModel;
import net.officefloor.model.test.changes.AbstractChangesTestCase;

/**
 * Abstract functionality for testing the {@link SectionChanges}.
 *
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSectionChangesTestCase extends AbstractChangesTestCase<SectionModel, SectionChanges> {

	/**
	 * Initiate.
	 */
	public AbstractSectionChangesTestCase() {
	}

	/**
	 * Initiate.
	 *
	 * @param isSpecificSetupFilePerTest Flag if specific setup file to be used.
	 */
	public AbstractSectionChangesTestCase(boolean isSpecificSetupFilePerTest) {
		super(isSpecificSetupFilePerTest);
	}

	/*
	 * ================== AbstractOperationsTestCase =========================
	 */

	@Override
	protected SectionModel retrieveModel(ConfigurationItem configurationItem) throws Exception {
		SectionModel section = new SectionModel();
		new SectionRepositoryImpl(new ModelRepositoryImpl()).retrieveSection(section, configurationItem);
		return section;
	}

	@Override
	protected void storeModel(SectionModel model, WritableConfigurationItem configurationItem) throws Exception {
		new SectionRepositoryImpl(new ModelRepositoryImpl()).storeSection(model, configurationItem);
	}

	@Override
	protected SectionChanges createModelOperations(SectionModel model) {
		return new SectionChangesImpl(model);
	}

	@Override
	protected String getModelFileExtension() {
		return ".section.xml";
	}

	/**
	 * Creates a {@link FunctionNamespaceType}.
	 * 
	 * @param constructor {@link NamespaceTypeConstructor} to construct the
	 *                    {@link FunctionNamespaceType}.
	 * @return {@link FunctionNamespaceType}.
	 */
	protected FunctionNamespaceType constructNamespaceType(NamespaceTypeConstructor constructor) {

		// Create the namespace type builder
		FunctionNamespaceTypeImpl namespaceBuilder = new FunctionNamespaceTypeImpl();

		// Build the namespace type via the constructor
		NamespaceTypeContext context = new NamespaceTypeContextImpl(namespaceBuilder);
		constructor.construct(context);

		// Return the namespace type
		return namespaceBuilder;
	}

	/**
	 * {@link NamespaceTypeConstructor} to construct the
	 * {@link FunctionNamespaceType}.
	 */
	protected interface NamespaceTypeConstructor {

		/**
		 * Constructs the {@link FunctionNamespaceType}.
		 * 
		 * @param context {@link NamespaceTypeContext}.
		 */
		void construct(NamespaceTypeContext context);
	}

	/**
	 * Context to construct the {@link FunctionNamespaceType}.
	 */
	protected interface NamespaceTypeContext {

		/**
		 * Adds a {@link ManagedFunctionType}.
		 * 
		 * @param functionName Name of the {@link ManagedFunction}.
		 * @return {@link FunctionTypeConstructor} to provide simplified
		 *         {@link ManagedFunctionType} construction.
		 */
		FunctionTypeConstructor addFunction(String functionName);

		/**
		 * Adds a {@link ManagedFunctionTypeBuilder}.
		 * 
		 * @param functionName   Name of the {@link ManagedFunction}.
		 * @param dependencyKeys Dependency keys {@link Enum}.
		 * @param flowKeys       Flow keys {@link Enum}.
		 * @return {@link ManagedFunctionTypeBuilder}.
		 */
		<M extends Enum<M>, F extends Enum<F>> ManagedFunctionTypeBuilder<M, F> addFunction(String functionName,
				Class<M> dependencyKeys, Class<F> flowKeys);
	}

	/**
	 * Provides simplified construction of a {@link ManagedFunctionType}.
	 */
	protected interface FunctionTypeConstructor {

		/**
		 * Adds a {@link ManagedFunctionObjectType}.
		 * 
		 * @param objectType {@link Object} type.
		 * @param key        Key identifying the {@link ManagedFunctionObjectType}.
		 * @return {@link ManagedFunctionObjectTypeBuilder} for the added
		 *         {@link ManagedFunctionObjectType}.
		 */
		ManagedFunctionObjectTypeBuilder<?> addObject(Class<?> objectType, Enum<?> key);

		/**
		 * Adds a {@link ManagedFunctionFlowType}.
		 * 
		 * @param argumentType Argument type.
		 * @param key          Key identifying the {@link ManagedFunctionFlowType}.
		 * @return {@link ManagedFunctionFlowTypeBuilder} for the added
		 *         {@link ManagedFunctionObjectType}.
		 */
		ManagedFunctionFlowTypeBuilder<?> addFlow(Class<?> argumentType, Enum<?> key);

		/**
		 * Adds a {@link ManagedFunctionEscalationType}.
		 * 
		 * @param escalationType Escalation type.
		 * @return {@link ManagedFunctionEscalationTypeBuilder} for the added
		 *         {@link ManagedFunctionEscalationType}.
		 */
		ManagedFunctionEscalationTypeBuilder addEscalation(Class<? extends Throwable> escalationType);

		/**
		 * Obtains the underlying {@link ManagedFunctionTypeBuilder}.
		 * 
		 * @return Underlying {@link ManagedFunctionTypeBuilder}.
		 */
		ManagedFunctionTypeBuilder<?, ?> getBuilder();
	}

	/**
	 * {@link NamespaceTypeContext} implementation.
	 */
	private class NamespaceTypeContextImpl implements NamespaceTypeContext {

		/**
		 * {@link FunctionNamespaceBuilder}.
		 */
		private final FunctionNamespaceBuilder namespaceBuilder;

		/**
		 * Initiate.
		 * 
		 * @param namespaceBuilder {@link FunctionNamespaceBuilder}.
		 */
		public NamespaceTypeContextImpl(FunctionNamespaceBuilder namespaceBuilder) {
			this.namespaceBuilder = namespaceBuilder;
		}

		/*
		 * ================== NamespaceTypeContext ============================
		 */

		@Override
		@SuppressWarnings("rawtypes")
		public FunctionTypeConstructor addFunction(String functionName) {
			// Add the function
			ManagedFunctionTypeBuilder functionTypeBuilder = this.namespaceBuilder.addManagedFunctionType(functionName,
					null, null);

			// Return the function type constructor for the function type builder
			return new FunctionTypeConstructorImpl(functionTypeBuilder);
		}

		@Override
		public <D extends Enum<D>, F extends Enum<F>> ManagedFunctionTypeBuilder<D, F> addFunction(String functionName,
				Class<D> dependencyKeys, Class<F> flowKeys) {
			return this.namespaceBuilder.addManagedFunctionType(functionName, dependencyKeys, flowKeys);
		}
	}

	/**
	 * {@link FunctionTypeConstructor} implementation.
	 */
	private class FunctionTypeConstructorImpl implements FunctionTypeConstructor {

		/**
		 * {@link ManagedFunctionTypeBuilder}.
		 */
		private final ManagedFunctionTypeBuilder<?, ?> functionTypeBuilder;

		/**
		 * Initiate.
		 * 
		 * @param functionTypeBuilder {@link ManagedFunctionTypeBuilder}.
		 */
		public FunctionTypeConstructorImpl(ManagedFunctionTypeBuilder<?, ?> functionTypeBuilder) {
			this.functionTypeBuilder = functionTypeBuilder;
		}

		/*
		 * ================= FunctionTypeConstructor ===========================
		 */

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ManagedFunctionObjectTypeBuilder<?> addObject(Class<?> objectType, Enum<?> key) {
			ManagedFunctionObjectTypeBuilder object = this.functionTypeBuilder.addObject(objectType);
			if (key != null) {
				object.setKey(key);
			}
			return object;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public ManagedFunctionFlowTypeBuilder<?> addFlow(Class<?> argumentType, Enum<?> key) {
			ManagedFunctionFlowTypeBuilder flow = this.functionTypeBuilder.addFlow();
			flow.setArgumentType(argumentType);
			if (key != null) {
				flow.setKey(key);
			}
			return flow;
		}

		@Override
		public ManagedFunctionEscalationTypeBuilder addEscalation(Class<? extends Throwable> escalationType) {
			ManagedFunctionEscalationTypeBuilder escalation = this.functionTypeBuilder.addEscalation(escalationType);
			return escalation;
		}

		@Override
		public ManagedFunctionTypeBuilder<?, ?> getBuilder() {
			return this.functionTypeBuilder;
		}
	}

	/**
	 * Constructor to construct the {@link ManagedFunctionType}.
	 */
	protected interface FunctionConstructor {

		/**
		 * Constructs the {@link ManagedFunctionType}.
		 * 
		 * @param function {@link ManagedFunctionType}.
		 */
		void construct(FunctionTypeConstructor function);
	}

	/**
	 * Constructs the {@link ManagedFunctionType}.
	 * 
	 * @param functionName Name of the {@link ManagedFunctionType}.
	 * @param constructor  {@link FunctionConstructor}.
	 * @return {@link ManagedFunctionType}.
	 */
	protected ManagedFunctionType<?, ?> constructFunctionType(final String functionName,
			final FunctionConstructor constructor) {

		// Construct the namespace
		FunctionNamespaceType namespaceType = this.constructNamespaceType(new NamespaceTypeConstructor() {
			@Override
			public void construct(NamespaceTypeContext context) {
				// Construct the function
				FunctionTypeConstructor function = context.addFunction(functionName);
				if (constructor != null) {
					constructor.construct(function);
				}
			}
		});

		// Return the function from the namespace
		return namespaceType.getManagedFunctionTypes()[0];
	}

}
