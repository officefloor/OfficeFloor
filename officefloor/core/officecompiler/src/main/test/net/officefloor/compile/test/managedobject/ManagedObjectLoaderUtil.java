/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.test.managedobject;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.managedobject.ManagedObjectDependencyTypeImpl;
import net.officefloor.compile.impl.managedobject.ManagedObjectExecutionStrategyTypeImpl;
import net.officefloor.compile.impl.managedobject.ManagedObjectFlowTypeImpl;
import net.officefloor.compile.impl.managedobject.ManagedObjectFunctionDependencyTypeImpl;
import net.officefloor.compile.impl.managedobject.ManagedObjectTeamTypeImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectFunctionDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.annotation.AnnotationLoaderUtil;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.test.JUnitAgnosticAssert;

/**
 * Utility class for testing the {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectLoaderUtil {

	/**
	 * Validates the {@link ManagedObjectSourceSpecification} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param <M>                 Dependency keys type.
	 * @param <F>                 {@link Flow} keys type.
	 * @param managedObjectSource {@link ManagedObjectSource} class.
	 * @param propertyNameLabels  Listing of name/label pairs for the
	 *                            {@link Property} instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <M extends Enum<M>, F extends Enum<F>> PropertyList validateSpecification(
			ManagedObjectSource<M, F> managedObjectSource, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler(null).getManagedObjectLoader()
				.loadSpecification(managedObjectSource);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Validates the {@link ManagedObjectSourceSpecification} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param <M>                      Dependency keys type.
	 * @param <F>                      {@link Flow} keys type.
	 * @param <S>                      {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass {@link ManagedObjectSource} class.
	 * @param propertyNameLabels       Listing of name/label pairs for the
	 *                                 {@link Property} instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <M extends Enum<M>, F extends Enum<F>, S extends ManagedObjectSource<M, F>> PropertyList validateSpecification(
			Class<S> managedObjectSourceClass, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler(null).getManagedObjectLoader()
				.loadSpecification(managedObjectSourceClass);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Creates the {@link ManagedObjectTypeBuilder} to create the expected
	 * {@link ManagedObjectType}.
	 * 
	 * @return {@link ManagedObjectTypeBuilder}.
	 */
	@SuppressWarnings("rawtypes")
	public static ManagedObjectTypeBuilder createManagedObjectTypeBuilder() {
		return new ManagedObjectTypeBuilderImpl();
	}

	/**
	 * Validates the {@link ManagedObjectType} contained in the
	 * {@link ManagedObjectTypeBuilder} against the {@link ManagedObjectType} loaded
	 * from the {@link ManagedObjectSource}.
	 * 
	 * @param <M>                       Dependency keys type.
	 * @param <F>                       {@link Flow} keys type.
	 * @param <S>                       {@link ManagedObjectSource} type.
	 * @param expectedManagedObjectType Expected {@link ManagedObjectType}.
	 * @param managedObjectSourceClass  {@link ManagedObjectSource} {@link Class}.
	 * @param propertyNameValues        Property values to configure the
	 *                                  {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectType} loaded from the
	 *         {@link ManagedObjectSource}.
	 */
	public static <M extends Enum<M>, F extends Enum<F>, S extends ManagedObjectSource<M, F>> ManagedObjectType<M> validateManagedObjectType(
			ManagedObjectTypeBuilder expectedManagedObjectType, Class<S> managedObjectSourceClass,
			String... propertyNameValues) {

		// Load the managed object type
		ManagedObjectType<M> aType = loadManagedObjectType(managedObjectSourceClass, propertyNameValues);

		// Validate the type
		return validateManagedObjectType(expectedManagedObjectType, aType);
	}

	/**
	 * Validates the {@link ManagedObjectType} contained in the
	 * {@link ManagedObjectTypeBuilder} against the {@link ManagedObjectType} loaded
	 * from the {@link ManagedObjectSource}.
	 * 
	 * @param <M>                       Dependency keys type.
	 * @param <F>                       {@link Flow} keys type.
	 * @param expectedManagedObjectType Expected {@link ManagedObjectType}.
	 * @param managedObjectSource       {@link ManagedObjectSource} instance.
	 * @param propertyNameValues        Property values to configure the
	 *                                  {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectType} loaded from the
	 *         {@link ManagedObjectSource}.
	 */
	public static <M extends Enum<M>, F extends Enum<F>> ManagedObjectType<M> validateManagedObjectType(
			ManagedObjectTypeBuilder expectedManagedObjectType, ManagedObjectSource<M, F> managedObjectSource,
			String... propertyNameValues) {

		// Load the managed object type
		ManagedObjectType<M> aType = loadManagedObjectType(managedObjectSource, propertyNameValues);

		// Validate the type
		return validateManagedObjectType(expectedManagedObjectType, aType);
	}

	/**
	 * Validates the {@link ManagedObjectType}.
	 * 
	 * @param expectedManagedObjectType Expected {@link ManagedObjectType}.
	 * @param aType                     Actual {@link ManagedObjectType}.
	 * @return {@link ManagedObjectType} loaded from the
	 *         {@link ManagedObjectSource}.
	 */
	private static <M extends Enum<M>, F extends Enum<F>> ManagedObjectType<M> validateManagedObjectType(
			ManagedObjectTypeBuilder expectedManagedObjectType, ManagedObjectType<M> aType) {

		// Build the expected managed object type
		ManagedObjectType<M> eType = expectedManagedObjectType.build();

		// Verify the types match
		JUnitAgnosticAssert.assertEquals(eType.getObjectType(), aType.getObjectType(), "Incorrect object type");

		// Verify input
		JUnitAgnosticAssert.assertEquals(eType.isInput(), aType.isInput(), "Incorrect flagging input");

		// Verify the dependencies
		ManagedObjectDependencyType<M>[] eDependencies = eType.getDependencyTypes();
		ManagedObjectDependencyType<M>[] aDependencies = aType.getDependencyTypes();
		JUnitAgnosticAssert.assertEquals(eDependencies.length, aDependencies.length,
				"Incorrect number of dependencies");
		for (int i = 0; i < eDependencies.length; i++) {
			ManagedObjectDependencyType<M> eDependency = eDependencies[i];
			ManagedObjectDependencyType<M> aDependency = aDependencies[i];
			JUnitAgnosticAssert.assertEquals(eDependency.getDependencyName(), aDependency.getDependencyName(),
					"Incorrect name for dependency " + i);
			JUnitAgnosticAssert.assertEquals(eDependency.getDependencyType(), aDependency.getDependencyType(),
					"Incorrect type for dependency " + i);
			JUnitAgnosticAssert.assertEquals(eDependency.getTypeQualifier(), aDependency.getTypeQualifier(),
					"Incorrect type qualifier for dependency " + i);
			JUnitAgnosticAssert.assertEquals(eDependency.getIndex(), aDependency.getIndex(),
					"Incorrect index for dependency " + i);
			JUnitAgnosticAssert.assertEquals(eDependency.getKey(), aDependency.getKey(),
					"Incorrect key for dependency " + i);

			// Verify expected annotations exist
			AnnotationLoaderUtil.validateAnnotations("for dependency " + i, eDependency.getAnnotations(),
					aDependency.getAnnotations());
		}

		// Verify the function dependencies
		ManagedObjectFunctionDependencyType[] eFunctionDependencies = eType.getFunctionDependencyTypes();
		ManagedObjectFunctionDependencyType[] aFunctionDependencies = aType.getFunctionDependencyTypes();
		JUnitAgnosticAssert.assertEquals(eFunctionDependencies.length, aFunctionDependencies.length,
				"Incorrect number of function dependencies");
		for (int i = 0; i < eFunctionDependencies.length; i++) {
			ManagedObjectFunctionDependencyType eFunctionDependency = eFunctionDependencies[i];
			ManagedObjectFunctionDependencyType aFunctionDependency = aFunctionDependencies[i];
			JUnitAgnosticAssert.assertEquals(eFunctionDependency.getFunctionObjectName(),
					aFunctionDependency.getFunctionObjectName(), "Incorrect name for function dependency " + i);
			JUnitAgnosticAssert.assertEquals(eFunctionDependency.getFunctionObjectType(),
					aFunctionDependency.getFunctionObjectType(), "Incorrect type for function dependency " + i);
		}

		// Verify the flows
		ManagedObjectFlowType<?>[] eFlows = eType.getFlowTypes();
		ManagedObjectFlowType<?>[] aFlows = aType.getFlowTypes();
		JUnitAgnosticAssert.assertEquals(eFlows.length, aFlows.length, "Incorrect number of flows");
		for (int i = 0; i < eFlows.length; i++) {
			ManagedObjectFlowType<?> eFlow = eFlows[i];
			ManagedObjectFlowType<?> aFlow = aFlows[i];
			JUnitAgnosticAssert.assertEquals(eFlow.getFlowName(), aFlow.getFlowName(), "Incorrect name for flow " + i);
			JUnitAgnosticAssert.assertEquals(eFlow.getArgumentType(), aFlow.getArgumentType(),
					"Incorrect argument type for flow " + i);
			JUnitAgnosticAssert.assertEquals(eFlow.getIndex(), aFlow.getIndex(), "Incorrect index for flow " + i);
			JUnitAgnosticAssert.assertEquals(eFlow.getKey(), aFlow.getKey(), "Incorrect key for flow " + i);
		}

		// Verify the teams
		ManagedObjectTeamType[] eTeams = eType.getTeamTypes();
		ManagedObjectTeamType[] aTeams = aType.getTeamTypes();
		JUnitAgnosticAssert.assertEquals(eTeams.length, aTeams.length, "Incorrect number of teams");
		for (int i = 0; i < eTeams.length; i++) {
			ManagedObjectTeamType eTeam = eTeams[i];
			ManagedObjectTeamType aTeam = aTeams[i];
			JUnitAgnosticAssert.assertEquals(eTeam.getTeamName(), aTeam.getTeamName(), "Incorrect name for team " + i);
		}

		// Verify the execution strategies
		ManagedObjectExecutionStrategyType[] eStrategies = eType.getExecutionStrategyTypes();
		ManagedObjectExecutionStrategyType[] aStrategies = aType.getExecutionStrategyTypes();
		JUnitAgnosticAssert.assertEquals(eStrategies.length, aStrategies.length,
				"Incorrect number of execution strategies");
		for (int i = 0; i < eStrategies.length; i++) {
			ManagedObjectExecutionStrategyType eStrategy = eStrategies[i];
			ManagedObjectExecutionStrategyType aStrategy = aStrategies[i];
			JUnitAgnosticAssert.assertEquals(eStrategy.getExecutionStrategyName(), aStrategy.getExecutionStrategyName(),
					"Incorrect name for execution strategy " + i);
		}

		// Verify the extension interfaces
		Class<?>[] eEis = eType.getExtensionTypes();
		Class<?>[] aEis = aType.getExtensionTypes();
		JUnitAgnosticAssert.assertEquals(eEis.length, aEis.length, "Incorrect number of extension interfaces");
		for (int i = 0; i < eEis.length; i++) {
			JUnitAgnosticAssert.assertEquals(eEis[i], aEis[i], "Incorrect extension interface " + i);
		}

		// Return the loaded managed object type
		return aType;
	}

	/**
	 * Convenience method to load the {@link ManagedObjectType} from the
	 * {@link ManagedObjectSource} utilising the {@link ClassLoader} from the input
	 * {@link ManagedObjectSource} class.
	 * 
	 * @param <M>                      Dependency keys type.
	 * @param <F>                      {@link Flow} keys type.
	 * @param <S>                      {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass {@link ManagedObjectSource} class.
	 * @param propertyNameValues       {@link Property} name/value listing.
	 * @return {@link ManagedObjectType}.
	 */
	public static <M extends Enum<M>, F extends Enum<F>, S extends ManagedObjectSource<M, F>> ManagedObjectType<M> loadManagedObjectType(
			Class<S> managedObjectSourceClass, String... propertyNameValues) {

		// Obtain the class loader
		ClassLoader classLoader = managedObjectSourceClass.getClassLoader();

		// Return the loaded managed object type
		return loadManagedObjectType(managedObjectSourceClass, classLoader, propertyNameValues);
	}

	/**
	 * Loads the {@link ManagedObjectType} from the {@link ManagedObjectSource}.
	 * 
	 * @param <M>                      Dependency keys type.
	 * @param <F>                      {@link Flow} keys type.
	 * @param <S>                      {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass {@link ManagedObjectSource} class.
	 * @param classLoader              {@link ClassLoader}.
	 * @param propertyNameValues       {@link Property} name/value listing.
	 * @return {@link ManagedObjectType}.
	 */
	public static <M extends Enum<M>, F extends Enum<F>, S extends ManagedObjectSource<M, F>> ManagedObjectType<M> loadManagedObjectType(
			Class<S> managedObjectSourceClass, ClassLoader classLoader, String... propertyNameValues) {

		// Load and return the managed object type
		return getOfficeFloorCompiler(classLoader).getManagedObjectLoader()
				.loadManagedObjectType(managedObjectSourceClass, new PropertyListImpl(propertyNameValues));
	}

	/**
	 * Loads the {@link ManagedObjectType} from the {@link ManagedObjectSource}.
	 * 
	 * @param <M>                 Dependency keys type.
	 * @param <F>                 {@link Flow} keys type.
	 * @param managedObjectSource {@link ManagedObjectSource} instance.
	 * @param propertyNameValues  {@link Property} name/value listing.
	 * @return {@link ManagedObjectType}.
	 */
	public static <M extends Enum<M>, F extends Enum<F>> ManagedObjectType<M> loadManagedObjectType(
			ManagedObjectSource<M, F> managedObjectSource, String... propertyNameValues) {

		// Load and return the managed object type
		return getOfficeFloorCompiler(null).getManagedObjectLoader().loadManagedObjectType(managedObjectSource,
				new PropertyListImpl(propertyNameValues));
	}

	/**
	 * {@link OfficeFloorCompiler} for the next operation.
	 */
	private static OfficeFloorCompiler nextOfficeFloorCompiler = null;

	/**
	 * Specifies the {@link OfficeFloorCompiler} for the next operation.
	 * 
	 * @param compiler {@link OfficeFloorCompiler} for the next operation.
	 */
	public static void setNextOfficeFloorCompiler(OfficeFloorCompiler compiler) {
		nextOfficeFloorCompiler = compiler;
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @param classLoader {@link ClassLoader}.
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler(ClassLoader classLoader) {

		OfficeFloorCompiler compiler;

		// Determine if OfficeFloorCompiler for this operation
		if (nextOfficeFloorCompiler != null) {
			// Use next OfficeFloorCompiler
			compiler = nextOfficeFloorCompiler;
			nextOfficeFloorCompiler = null; // clear for further operations
		} else {
			// Create the office floor compiler that fails on first issue
			compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);
			compiler.setCompilerIssues(new FailTestCompilerIssues());
		}

		// Return the OfficeFloorCompiler
		return compiler;
	}

	/**
	 * <p>
	 * Creates a {@link MetaDataContext}.
	 * <p>
	 * This is useful for testing abstract {@link ManagedObjectSource} instances
	 * that delegate configuration to sub classes.
	 * 
	 * @param <M>                Dependency keys type.
	 * @param <F>                {@link Flow} keys type.
	 * @param dependenciesEnum   Dependency {@link Enum}.
	 * @param flowsEnum          Flows {@link Enum}.
	 * @param propertyNameValues Property name values for the
	 *                           {@link MetaDataContext}.
	 * 
	 * @return {@link MetaDataContext}.
	 */
	@SuppressWarnings("unchecked")
	public synchronized static <M extends Enum<M>, F extends Enum<F>> MetaDataContext<M, F> createMetaDataContext(
			Class<M> dependenciesEnum, Class<F> flowsEnum, String... propertyNameValues) {

		// Create the meta data context
		CollectMetaDataContextManagedObjectSource.metaDataContext = null;

		// Create mock managed object source
		loadManagedObjectType(CollectMetaDataContextManagedObjectSource.class, propertyNameValues);

		// Return the meta data context
		return (MetaDataContext<M, F>) CollectMetaDataContextManagedObjectSource.metaDataContext;
	}

	/**
	 * {@link ManagedObjectSource} to enable obtaining the {@link MetaDataContext}.
	 */
	@TestSource
	public static class CollectMetaDataContextManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		/**
		 * {@link MetaDataContext}.
		 */
		public static MetaDataContext<?, ?> metaDataContext;

		/*
		 * ================== ManagedObjectSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No properties required
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

			// Ensure have minimum configuration specified
			context.setObjectClass(Object.class);

			// Collect the context
			metaDataContext = context;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			JUnitAgnosticAssert.fail("Should not require managed object");
			return null;
		}
	}

	/**
	 * All access is via static methods.
	 */
	private ManagedObjectLoaderUtil() {
	}

	/**
	 * {@link ManagedObjectTypeBuilder} implementation.
	 */
	private static class ManagedObjectTypeBuilderImpl<M extends Enum<M>>
			implements ManagedObjectTypeBuilder, ManagedObjectType<M> {

		/**
		 * Object class.
		 */
		private Class<?> objectClass;

		/**
		 * Flags if input.
		 */
		private boolean isInput = false;

		/**
		 * {@link ManagedObjectDependencyType} instances.
		 */
		private final List<ManagedObjectDependencyType<?>> dependencies = new LinkedList<>();

		/**
		 * {@link ManagedObjectFunctionDependencyType} instances.
		 */
		private final List<ManagedObjectFunctionDependencyType> functionDependencies = new LinkedList<>();

		/**
		 * {@link ManagedObjectFlowType} instances.
		 */
		private final List<ManagedObjectFlowType<?>> flows = new LinkedList<>();

		/**
		 * {@link ManagedObjectTeamType} instances.
		 */
		private final List<ManagedObjectTeamType> teams = new LinkedList<>();

		/**
		 * {@link ManagedObjectExecutionStrategyType} instances.
		 */
		private final List<ManagedObjectExecutionStrategyType> executionStrategies = new LinkedList<>();

		/**
		 * Extension interfaces.
		 */
		private final List<Class<?>> extensionInterfaces = new LinkedList<Class<?>>();

		/*
		 * ====================== ManagedObjectType ==========================
		 */

		@Override
		public Class<?> getObjectType() {
			return this.objectClass;
		}

		@Override
		public void setInput(boolean isInput) {
			this.isInput = isInput;
		}

		@Override
		@SuppressWarnings("unchecked")
		public ManagedObjectDependencyType<M>[] getDependencyTypes() {
			return (ManagedObjectDependencyType<M>[]) this.dependencies.toArray(new ManagedObjectDependencyType[0]);
		}

		@Override
		public ManagedObjectFunctionDependencyType[] getFunctionDependencyTypes() {
			return this.functionDependencies.toArray(new ManagedObjectFunctionDependencyType[0]);
		}

		@Override
		public ManagedObjectFlowType<?>[] getFlowTypes() {
			return this.flows.toArray(new ManagedObjectFlowType[0]);
		}

		@Override
		public ManagedObjectTeamType[] getTeamTypes() {
			return this.teams.toArray(new ManagedObjectTeamType[0]);
		}

		@Override
		public ManagedObjectExecutionStrategyType[] getExecutionStrategyTypes() {
			return this.executionStrategies.toArray(new ManagedObjectExecutionStrategyType[0]);
		}

		@Override
		public Class<?>[] getExtensionTypes() {
			return this.extensionInterfaces.toArray(new Class[0]);
		}

		/*
		 * ================= ManagedObjectTypeBuilder =========================
		 */

		@Override
		public void setObjectClass(Class<?> objectClass) {
			this.objectClass = objectClass;
		}

		@Override
		public boolean isInput() {
			return this.isInput;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void addDependency(String name, Class<?> type, String typeQualifier, int index, Enum<?> key,
				Class<?>... annotations) {
			this.dependencies
					.add(new ManagedObjectDependencyTypeImpl(index, type, typeQualifier, annotations, key, name));
		}

		@Override
		public void addDependency(Enum<?> key, Class<?> type, String typeQualifier) {
			this.addDependency(key.name(), type, typeQualifier, key.ordinal(), key);
		}

		@Override
		public void addFunctionDependency(String name, Class<?> type) {
			this.functionDependencies.add(new ManagedObjectFunctionDependencyTypeImpl(name, type));
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void addFlow(String name, Class<?> argumentType, int index, Enum<?> key) {
			this.flows.add(new ManagedObjectFlowTypeImpl(index, argumentType, key, name));
		}

		@Override
		public void addFlow(Enum<?> key, Class<?> argumentType) {
			this.addFlow(key.name(), argumentType, key.ordinal(), key);
		}

		@Override
		public void addTeam(String teamName) {
			this.teams.add(new ManagedObjectTeamTypeImpl(teamName));
		}

		@Override
		public void addExecutionStrategy(String executionStrategyName) {
			this.executionStrategies.add(new ManagedObjectExecutionStrategyTypeImpl(executionStrategyName));
		}

		@Override
		public void addExtensionInterface(Class<?> extensionInterface) {
			this.extensionInterfaces.add(extensionInterface);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <D extends Enum<D>> ManagedObjectType<D> build() {
			return (ManagedObjectType<D>) this;
		}
	}

}
