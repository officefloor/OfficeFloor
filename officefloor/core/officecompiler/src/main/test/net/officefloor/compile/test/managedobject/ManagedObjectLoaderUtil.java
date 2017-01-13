/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.compile.test.managedobject;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.managedobject.ManagedObjectDependencyTypeImpl;
import net.officefloor.compile.impl.managedobject.ManagedObjectFlowTypeImpl;
import net.officefloor.compile.impl.managedobject.ManagedObjectTeamTypeImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.internal.structure.Flow;

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
	 * @param <D>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link Flow} keys type.
	 * @param <S>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> PropertyList validateSpecification(
			Class<S> managedObjectSourceClass, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler(null)
				.getManagedObjectLoader().loadSpecification(
						managedObjectSourceClass);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList,
				propertyNameLabels);

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
	 * {@link ManagedObjectTypeBuilder} against the {@link ManagedObjectType}
	 * loaded from the {@link ManagedObjectSource}.
	 * 
	 * @param <D>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link Flow} keys type.
	 * @param <S>
	 *            {@link ManagedObjectSource} type.
	 * @param expectedManagedObjectType
	 *            Expected {@link ManagedObjectType}.
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} {@link Class}.
	 * @param propertyNameValues
	 *            Property values to configure the {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectType} loaded from the
	 *         {@link ManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public static <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> ManagedObjectType<D> validateManagedObjectType(
			ManagedObjectTypeBuilder expectedManagedObjectType,
			Class<S> managedObjectSourceClass, String... propertyNameValues) {

		// Cast to obtain expected managed object type
		if (!(expectedManagedObjectType instanceof ManagedObjectType)) {
			TestCase.fail("builder must be created from createManagedObjectTypeBuilder");
		}
		ManagedObjectType<D> eType = (ManagedObjectType<D>) expectedManagedObjectType;

		// Load the managed object type
		ManagedObjectType<D> aType = loadManagedObjectType(
				managedObjectSourceClass, propertyNameValues);

		// Verify the types match
		TestCase.assertEquals("Incorrect object type", eType.getObjectClass(),
				aType.getObjectClass());

		// Verify the dependencies
		ManagedObjectDependencyType<D>[] eDependencies = eType
				.getDependencyTypes();
		ManagedObjectDependencyType<D>[] aDependencies = aType
				.getDependencyTypes();
		TestCase.assertEquals("Incorrect number of dependencies",
				eDependencies.length, aDependencies.length);
		for (int i = 0; i < eDependencies.length; i++) {
			ManagedObjectDependencyType<D> eDependency = eDependencies[i];
			ManagedObjectDependencyType<D> aDependency = aDependencies[i];
			TestCase.assertEquals("Incorrect name for dependency " + i,
					eDependency.getDependencyName(),
					aDependency.getDependencyName());
			TestCase.assertEquals("Incorrect type for dependency " + i,
					eDependency.getDependencyType(),
					aDependency.getDependencyType());
			TestCase.assertEquals("Incorrect type qualifier for dependency "
					+ i, eDependency.getTypeQualifier(),
					aDependency.getTypeQualifier());
			TestCase.assertEquals("Incorrect index for dependency " + i,
					eDependency.getIndex(), aDependency.getIndex());
			TestCase.assertEquals("Incorrect key for dependency " + i,
					eDependency.getKey(), aDependency.getKey());
		}

		// Verify the flows
		ManagedObjectFlowType<?>[] eFlows = eType.getFlowTypes();
		ManagedObjectFlowType<?>[] aFlows = aType.getFlowTypes();
		TestCase.assertEquals("Incorrect number of flows", eFlows.length,
				aFlows.length);
		for (int i = 0; i < eFlows.length; i++) {
			ManagedObjectFlowType<?> eFlow = eFlows[i];
			ManagedObjectFlowType<?> aFlow = aFlows[i];
			TestCase.assertEquals("Incorrect name for flow " + i,
					eFlow.getFlowName(), aFlow.getFlowName());
			TestCase.assertEquals("Incorrect argument type for flow " + i,
					eFlow.getArgumentType(), aFlow.getArgumentType());
			TestCase.assertEquals("Incorrect work for flow " + i,
					eFlow.getWorkName(), aFlow.getWorkName());
			TestCase.assertEquals("Incorrect task for flow " + i,
					eFlow.getTaskName(), aFlow.getTaskName());
			TestCase.assertEquals("Incorrect index for flow " + i,
					eFlow.getIndex(), aFlow.getIndex());
			TestCase.assertEquals("Incorrect key for flow " + i,
					eFlow.getKey(), aFlow.getKey());
		}

		// Verify the teams
		ManagedObjectTeamType[] eTeams = eType.getTeamTypes();
		ManagedObjectTeamType[] aTeams = aType.getTeamTypes();
		TestCase.assertEquals("Incorrect number of teams", eTeams.length,
				aTeams.length);
		for (int i = 0; i < eTeams.length; i++) {
			ManagedObjectTeamType eTeam = eTeams[i];
			ManagedObjectTeamType aTeam = aTeams[i];
			TestCase.assertEquals("Incorrect name for team " + i,
					eTeam.getTeamName(), aTeam.getTeamName());
		}

		// Verify the extension interfaces
		Class<?>[] eEis = eType.getExtensionInterfaces();
		Class<?>[] aEis = aType.getExtensionInterfaces();
		TestCase.assertEquals("Incorrect number of extension interfaces",
				eEis.length, aEis.length);
		for (int i = 0; i < eEis.length; i++) {
			TestCase.assertEquals("Incorrect extension interface " + i,
					eEis[i], aEis[i]);
		}

		// Return the loaded managed object type
		return aType;
	}

	/**
	 * Convenience method to load the {@link ManagedObjectType} from the
	 * {@link ManagedObjectSource} utilising the {@link ClassLoader} from the
	 * input {@link ManagedObjectSource} class.
	 * 
	 * @param <D>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link Flow} keys type.
	 * @param <S>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param propertyNameValues
	 *            {@link Property} name/value listing.
	 * @return {@link ManagedObjectType}.
	 */
	public static <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> ManagedObjectType<D> loadManagedObjectType(
			Class<S> managedObjectSourceClass, String... propertyNameValues) {

		// Obtain the class loader
		ClassLoader classLoader = managedObjectSourceClass.getClassLoader();

		// Return the loaded managed object type
		return loadManagedObjectType(managedObjectSourceClass, classLoader,
				propertyNameValues);
	}

	/**
	 * Loads the {@link ManagedObjectType} from the {@link ManagedObjectSource}.
	 * 
	 * @param <D>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link Flow} keys type.
	 * @param <S>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} class.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param propertyNameValues
	 *            {@link Property} name/value listing.
	 * @return {@link ManagedObjectType}.
	 */
	public static <D extends Enum<D>, F extends Enum<F>, S extends ManagedObjectSource<D, F>> ManagedObjectType<D> loadManagedObjectType(
			Class<S> managedObjectSourceClass, ClassLoader classLoader,
			String... propertyNameValues) {

		// Load and return the managed object type
		return getOfficeFloorCompiler(classLoader).getManagedObjectLoader()
				.loadManagedObjectType(managedObjectSourceClass,
						new PropertyListImpl(propertyNameValues));
	}

	/**
	 * {@link OfficeFloorCompiler} for the next operation.
	 */
	private static OfficeFloorCompiler nextOfficeFloorCompiler = null;

	/**
	 * Specifies the {@link OfficeFloorCompiler} for the next operation.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler} for the next operation.
	 */
	public static void setNextOfficeFloorCompiler(OfficeFloorCompiler compiler) {
		nextOfficeFloorCompiler = compiler;
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler(
			ClassLoader classLoader) {

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
	 * @param <D>
	 *            Dependency keys type.
	 * @param <F>
	 *            {@link Flow} keys type.
	 * @param dependenciesEnum
	 *            Dependency {@link Enum}.
	 * @param flowsEnum
	 *            Flows {@link Enum}.
	 * @param propertyNameValues
	 *            Property name values for the {@link MetaDataContext}.
	 * 
	 * @return {@link MetaDataContext}.
	 */
	@SuppressWarnings("unchecked")
	public synchronized static <D extends Enum<D>, F extends Enum<F>> MetaDataContext<D, F> createMetaDataContext(
			Class<D> dependenciesEnum, Class<F> flowsEnum,
			String... propertyNameValues) {

		// Create the meta data context
		CollectMetaDataContextManagedObjectSource.metaDataContext = null;

		// Create mock managed object source
		loadManagedObjectType(CollectMetaDataContextManagedObjectSource.class,
				propertyNameValues);

		// Return the meta data context
		return (MetaDataContext<D, F>) CollectMetaDataContextManagedObjectSource.metaDataContext;
	}

	/**
	 * {@link ManagedObjectSource} to enable obtaining the
	 * {@link MetaDataContext}.
	 */
	@TestSource
	public static class CollectMetaDataContextManagedObjectSource extends
			AbstractManagedObjectSource<None, None> {

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
		protected void loadMetaData(MetaDataContext<None, None> context)
				throws Exception {

			// Ensure have minimum configuration specified
			context.setObjectClass(Object.class);

			// Collect the context
			metaDataContext = context;
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			TestCase.fail("Should not require managed object");
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
	private static class ManagedObjectTypeBuilderImpl<D extends Enum<D>>
			implements ManagedObjectTypeBuilder, ManagedObjectType<D> {

		/**
		 * Object class.
		 */
		private Class<?> objectClass;

		/**
		 * {@link ManagedObjectDependencyType} instances.
		 */
		private final List<ManagedObjectDependencyType<?>> dependencies = new LinkedList<ManagedObjectDependencyType<?>>();

		/**
		 * {@link ManagedObjectFlowType} instances.
		 */
		private final List<ManagedObjectFlowType<?>> flows = new LinkedList<ManagedObjectFlowType<?>>();

		/**
		 * {@link ManagedObjectTeamType} instances.
		 */
		private final List<ManagedObjectTeamType> teams = new LinkedList<ManagedObjectTeamType>();

		/**
		 * Extension interfaces.
		 */
		private final List<Class<?>> extensionInterfaces = new LinkedList<Class<?>>();

		/*
		 * ====================== ManagedObjectType ==========================
		 */

		@Override
		public Class<?> getObjectClass() {
			return this.objectClass;
		}

		@Override
		@SuppressWarnings("unchecked")
		public ManagedObjectDependencyType<D>[] getDependencyTypes() {
			return (ManagedObjectDependencyType<D>[]) this.dependencies
					.toArray(new ManagedObjectDependencyType[0]);
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
		public Class<?>[] getExtensionInterfaces() {
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
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void addDependency(String name, Class<?> type,
				String typeQualifier, int index, Enum<?> key) {
			this.dependencies.add(new ManagedObjectDependencyTypeImpl(index,
					type, typeQualifier, key, name));
		}

		@Override
		public void addDependency(Enum<?> key, Class<?> type,
				String typeQualifier) {
			this.addDependency(key.name(), type, typeQualifier, key.ordinal(),
					key);
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void addFlow(String name, Class<?> argumentType, int index,
				Enum<?> key, String workName, String taskName) {
			this.flows.add(new ManagedObjectFlowTypeImpl(workName, taskName,
					index, argumentType, key, name));
		}

		@Override
		public void addFlow(Enum<?> key, Class<?> argumentType,
				String workName, String taskName) {
			this.addFlow(key.name(), argumentType, key.ordinal(), key,
					workName, taskName);
		}

		@Override
		public void addTeam(String teamName) {
			this.teams.add(new ManagedObjectTeamTypeImpl(teamName));
		}

		@Override
		public void addExtensionInterface(Class<?> extensionInterface) {
			this.extensionInterfaces.add(extensionInterface);
		}
	}

}