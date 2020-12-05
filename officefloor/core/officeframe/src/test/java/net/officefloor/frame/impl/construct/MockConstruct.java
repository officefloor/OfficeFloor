/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.construct;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.junit.Assert;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecutionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectDependencyMetaDataImpl;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectExecutionMetaDataImpl;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectExtensionMetaDataImpl;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectFlowMetaDataImpl;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
import net.officefloor.frame.impl.construct.asset.AssetManagerRegistry;
import net.officefloor.frame.impl.construct.governance.GovernanceBuilderImpl;
import net.officefloor.frame.impl.construct.governance.RawGovernanceMetaData;
import net.officefloor.frame.impl.construct.managedfunction.ManagedFunctionInvocationImpl;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectBuilderImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagingOfficeBuilderImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagingOfficeMetaData;
import net.officefloor.frame.impl.construct.office.ManagedFunctionLocatorImpl;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.impl.construct.office.RawOfficeMetaData;
import net.officefloor.frame.impl.construct.officefloor.RawOfficeFloorMetaData;
import net.officefloor.frame.impl.construct.team.RawTeamMetaData;
import net.officefloor.frame.impl.execute.escalation.EscalationFlowImpl;
import net.officefloor.frame.impl.execute.execution.ManagedExecutionFactoryImpl;
import net.officefloor.frame.impl.execute.executive.DefaultExecutive;
import net.officefloor.frame.impl.execute.governance.GovernanceMetaDataImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionMetaDataImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.impl.execute.office.OfficeMetaDataImpl;
import net.officefloor.frame.impl.execute.officefloor.ThreadLocalAwareExecutorImpl;
import net.officefloor.frame.impl.execute.process.ProcessMetaDataImpl;
import net.officefloor.frame.impl.execute.team.TeamManagementImpl;
import net.officefloor.frame.impl.execute.thread.ThreadMetaDataImpl;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionInvocation;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.BackgroundScheduling;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.internal.structure.ManagedFunctionAdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ManagedObjectServiceReady;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;
import net.officefloor.frame.internal.structure.ThreadMetaData;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.MockManagedObjectSourceMetaData;

/**
 * Provides mock construction objects.
 * 
 * @author Daniel Sagenschneider
 */
public class MockConstruct {

	/**
	 * Default {@link Office} name.
	 */
	public static final String OFFICE_NAME = "OFFICE";

	/**
	 * Creates a mock {@link AssetManagerRegistry}.
	 * 
	 * @return Mock {@link AssetManagerRegistry}.
	 */
	public static AssetManagerRegistry mockAssetManagerRegistry() {
		return new AssetManagerRegistry(null, null);
	}

	/**
	 * Creates a mock {@link ManagedObjectBuilderImpl}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectBuilderImpl}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> ManagedObjectBuilderImpl<O, F, ConstructManagedObjectSource<O, F>> mockManagedObjectBuilder(
			String managedObjectSourceName) {
		return new ManagedObjectBuilderImpl<O, F, ConstructManagedObjectSource<O, F>>(managedObjectSourceName,
				new ConstructManagedObjectSource<>());
	}

	/**
	 * Creates a mock {@link RawOfficeFloorMetaData}.
	 * 
	 * @return Mock {@link RawOfficeFloorMetaData}.
	 */
	public static RawOfficeFloorMetaDataMockBuilder mockRawOfficeFloorMetaData() {
		return new RawOfficeFloorMetaDataMockBuilder();
	}

	/**
	 * Creates a mock {@link RawOfficeFloorMetaData}.
	 * 
	 * @param officeName Name of the {@link Office}.
	 * @return Mock {@link RawOfficeFloorMetaData}.
	 */
	public static RawOfficeMetaDataMockBuilder mockRawOfficeMetaData(String officeName) {
		return new RawOfficeMetaDataMockBuilder(officeName);
	}

	/**
	 * Creates a {@link OfficeMetaDataMockBuilder}.
	 * 
	 * @param officeName Name of the {@link Office}.
	 * @return {@link OfficeMetaDataMockBuilder}.
	 */
	public static OfficeMetaDataMockBuilder mockOfficeMetaData(String officeName) {
		return new OfficeMetaDataMockBuilder(officeName);
	}

	/**
	 * Creates mock {@link ManagedObjectMetaDataImpl}.
	 * 
	 * @param boundManagedObjectName Bound {@link ManagedObject} name.
	 * @param objectType             Object type of {@link ManagedObject}.
	 * @return Mock {@link ManagedObjectMetaDataImpl}.
	 */
	public static <O extends Enum<O>> ManagedObjectMetaDataImpl<O> mockManagedObjectMetaData(
			String boundManagedObjectName, Class<?> objectType) {
		AssetManagerRegistry assetManagerRegistry = mockAssetManagerRegistry();
		return new ManagedObjectMetaDataImpl<>(boundManagedObjectName, objectType, 0,
				new ConstructManagedObjectSource<>(), null, false,
				assetManagerRegistry.createAssetManager(AssetType.MANAGED_OBJECT, boundManagedObjectName, "mock", null),
				false, null, false, null, 0, null, OfficeFrame.getLogger(boundManagedObjectName));
	}

	/**
	 * Creates a {@link ManagedObjectSourceMetaDataMockBuilder} to build a
	 * {@link ManagedObjectSourceMetaData}.
	 * 
	 * @return {@link ManagedObjectSourceMetaDataMockBuilder} to build a
	 *         {@link ManagedObjectSourceMetaData}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> ManagedObjectSourceMetaDataMockBuilder<O, F> mockManagedObjectSourceMetaData() {
		return new ManagedObjectSourceMetaDataMockBuilder<>();
	}

	/**
	 * Creates a {@link RawManagedObjectMetaDataMockBuilder} to build a
	 * {@link RawManagedObjectMetaData}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @return {@link RawManagedObjectMetaDataMockBuilder}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> RawManagedObjectMetaDataMockBuilder<O, F> mockRawManagedObjectMetaData(
			String managedObjectSourceName) {
		return new RawManagedObjectMetaDataMockBuilder<>(managedObjectSourceName);
	}

	/**
	 * Creates a {@link RawManagingOfficeMetaDataMockBuilder} to build a
	 * {@link RawManagingOfficeMetaData}.
	 * 
	 * @param managingOfficeName      Name of the managing {@link Office}.
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @return {@link RawManagingOfficeMetaDataMockBuilder}.
	 */
	public static <F extends Enum<F>> RawManagingOfficeMetaDataMockBuilder<F> mockRawManagingOfficeMetaData(
			String managingOfficeName, String managedObjectSourceName) {
		return new RawManagingOfficeMetaDataMockBuilder<>(managingOfficeName, managedObjectSourceName);
	}

	/**
	 * Creates a {@link RawBoundManagedObjectMetaDataMockBuilder} to build a
	 * {@link RawBoundManagedObjectMetaData} (that is not an input).
	 * 
	 * @param boundManagedObjectName   Bound {@link ManagedObject} name.
	 * @param rawManagedObjectMetaData {@link RawManagedObjectMetaData}.
	 * @return {@link RawBoundManagedObjectMetaDataMockBuilder}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> RawBoundManagedObjectMetaDataMockBuilder<O, F> mockRawBoundManagedObjectMetaData(
			String boundManagedObjectName, RawManagedObjectMetaData<O, F> rawManagedObjectMetaData) {
		return new RawBoundManagedObjectMetaDataMockBuilder<>(boundManagedObjectName, false, rawManagedObjectMetaData);
	}

	/**
	 * Creates a {@link RawBoundManagedObjectMetaDataMockBuilder} to build a
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param boundManagedObjectName   Bound {@link ManagedObject} name.
	 * @param isInput                  Indicates if input.
	 * @param rawManagedObjectMetaData {@link RawManagedObjectMetaData}.
	 * @return {@link RawBoundManagedObjectMetaDataMockBuilder}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> RawBoundManagedObjectMetaDataMockBuilder<O, F> mockRawBoundManagedObjectMetaData(
			String boundManagedObjectName, boolean isInput, RawManagedObjectMetaData<O, F> rawManagedObjectMetaData) {
		return new RawBoundManagedObjectMetaDataMockBuilder<>(boundManagedObjectName, isInput,
				rawManagedObjectMetaData);
	}

	/**
	 * Creates a {@link RawBoundManagedObjectMetaDataMockBuilder} to build a
	 * {@link RawBoundManagedObjectMetaData} (that is not an input).
	 * 
	 * @param boundManagedObjectName   Bound {@link ManagedObject} name.
	 * @param rawManagedObjectMetaData {@link RawManagedObjectMetaDataMockBuilder}.
	 * @return {@link RawBoundManagedObjectMetaDataMockBuilder}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> RawBoundManagedObjectMetaDataMockBuilder<O, F> mockRawBoundManagedObjectMetaData(
			String boundManagedObjectName, RawManagedObjectMetaDataMockBuilder<O, F> rawManagedObjectMetaData) {
		return mockRawBoundManagedObjectMetaData(boundManagedObjectName, false, rawManagedObjectMetaData);
	}

	/**
	 * Creates a {@link RawBoundManagedObjectMetaDataMockBuilder} to build a
	 * {@link RawBoundManagedObjectMetaData} (that is not an input).
	 * 
	 * @param boundManagedObjectName   Bound {@link ManagedObject} name.
	 * @param isInput                  Indicates if input.
	 * @param rawManagedObjectMetaData {@link RawManagedObjectMetaDataMockBuilder}.
	 * @return {@link RawBoundManagedObjectMetaDataMockBuilder}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> RawBoundManagedObjectMetaDataMockBuilder<O, F> mockRawBoundManagedObjectMetaData(
			String boundManagedObjectName, boolean isInput,
			RawManagedObjectMetaDataMockBuilder<O, F> rawManagedObjectMetaData) {
		return new RawBoundManagedObjectMetaDataMockBuilder<>(boundManagedObjectName, isInput,
				rawManagedObjectMetaData);
	}

	/**
	 * Creates a {@link RawGovernanceMetaDataMockBuilder} to build a
	 * {@link RawGovernanceMetaData}.
	 * 
	 * @param governanceName Name of the {@link Governance}.
	 * @param extensionType  Extension type used by the {@link Governance}.
	 * @return {@link RawGovernanceMetaDataMockBuilder}.
	 */
	public static <E, F extends Enum<F>> RawGovernanceMetaDataMockBuilder<E, F> mockRawGovernanceMetaData(
			String governanceName, Class<E> extensionType) {
		return new RawGovernanceMetaDataMockBuilder<>(governanceName, extensionType);
	}

	/**
	 * Mock {@link ManagedObjectSource} for testing.
	 */
	@TestSource
	public static class ConstructManagedObjectSource<O extends Enum<O>, F extends Enum<F>>
			extends AbstractManagedObjectSource<O, F> {

		@Override
		protected void loadSpecification(SpecificationContext context) {
			Assert.fail("Should not be invoked");
		}

		@Override
		protected void loadMetaData(MetaDataContext<O, F> context) throws Exception {
			Assert.fail("Should not be invoked");
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			Assert.fail("Should not be invoked");
			return null;
		}
	}

	/**
	 * Builder for the {@link MockManagedObjectSourceMetaData}.
	 */
	public static class ManagedObjectSourceMetaDataMockBuilder<O extends Enum<O>, F extends Enum<F>>
			implements ManagedObjectSourceMetaData<O, F> {

		/**
		 * {@link ManagedObject} class.
		 */
		private Class<? extends ManagedObject> managedObjectClass = ManagedObject.class;

		/**
		 * {@link Object} class.
		 */
		private Class<?> objectClass = Object.class;

		/**
		 * {@link ManagedObjectDependencyMetaData} instances.
		 */
		private final List<ManagedObjectDependencyMetaData<O>> dependencies = new LinkedList<>();

		/**
		 * {@link ManagedObjectFlowMetaData} instances.
		 */
		private final List<ManagedObjectFlowMetaData<F>> flows = new LinkedList<>();

		/**
		 * {@link ManagedObjectExecutionMetaData} instances.
		 */
		private final List<ManagedObjectExecutionMetaData> executionStrategies = new LinkedList<>();

		/**
		 * {@link ManagedObjectExtensionMetaData} instances.
		 */
		private final List<ManagedObjectExtensionMetaData<?>> extensions = new LinkedList<>();

		/**
		 * Indicates whether built.
		 */
		private boolean isBuilt = false;

		/**
		 * Must be created via static methods.
		 */
		private ManagedObjectSourceMetaDataMockBuilder() {
		}

		/**
		 * Ensure not built.
		 */
		private void assertNotBuilt() {
			Assert.assertFalse("Should not alter once built", this.isBuilt);
		}

		/**
		 * Specifies the {@link ManagedObject} class.
		 * 
		 * @param managedObjectClass {@link ManagedObject} class.
		 */
		public void setManagedObjectClass(Class<? extends ManagedObject> managedObjectClass) {
			this.assertNotBuilt();
			this.managedObjectClass = managedObjectClass;
		}

		/**
		 * Specifies the {@link Object} class.
		 * 
		 * @param objectClass {@link Object} class.
		 */
		public void setObjectClass(Class<?> objectClass) {
			this.assertNotBuilt();
			this.objectClass = objectClass;
		}

		/**
		 * Adds a {@link ManagedObjectDependencyMetaData}.
		 * 
		 * @param type Dependency type.
		 * @param key  Key. May be <code>null</code>.
		 * @return {@link ManagedObjectDependencyMetaDataImpl} for further configuration
		 *         of the dependency.
		 */
		public ManagedObjectDependencyMetaDataImpl<O> addDependency(Class<?> type, O key) {
			this.assertNotBuilt();
			ManagedObjectDependencyMetaDataImpl<O> dependency = new ManagedObjectDependencyMetaDataImpl<O>(key, type);
			this.dependencies.add(dependency);
			return dependency;
		}

		/**
		 * Adds a {@link ManagedObjectFlowMetaData}.
		 * 
		 * @param argumentType Argument type to {@link Flow}. May be <code>null</code>.
		 * @param key          Key. May be <code>null</code>.
		 * @return {@link ManagedObjectFlowMetaDataImpl} for further configuration of
		 *         the {@link Flow}.
		 */
		public ManagedObjectFlowMetaDataImpl<F> addFlow(Class<?> argumentType, F key) {
			this.assertNotBuilt();
			ManagedObjectFlowMetaDataImpl<F> flow = new ManagedObjectFlowMetaDataImpl<F>(key, argumentType);
			this.flows.add(flow);
			return flow;
		}

		/**
		 * Adds a {@link ManagedObjectExecutionMetaData}.
		 * 
		 * @return {@link ManagedObjectExecutionMetaDataImpl} for further configuration
		 *         of the {@link ExecutionStrategy}.
		 */
		public ManagedObjectExecutionMetaDataImpl addExecutionStrategy() {
			this.assertNotBuilt();
			ManagedObjectExecutionMetaDataImpl execution = new ManagedObjectExecutionMetaDataImpl();
			this.executionStrategies.add(execution);
			return execution;
		}

		/**
		 * Adds an extension.
		 * 
		 * @param extensionType    Extension type.
		 * @param extensionFactory {@link ExtensionFactory}.
		 */
		public <E> void addExtension(Class<E> extensionType, ExtensionFactory<E> extensionFactory) {
			this.assertNotBuilt();
			this.extensions.add(new ManagedObjectExtensionMetaDataImpl<E>(extensionType, extensionFactory));
		}

		/*
		 * ==================== ManagedObjectSourceMetaData ====================
		 */

		@Override
		public Class<? extends ManagedObject> getManagedObjectClass() {
			this.isBuilt = true;
			return this.managedObjectClass;
		}

		@Override
		public Class<?> getObjectClass() {
			this.isBuilt = true;
			return this.objectClass;
		}

		@Override
		@SuppressWarnings("unchecked")
		public ManagedObjectDependencyMetaData<O>[] getDependencyMetaData() {
			this.isBuilt = true;
			return this.dependencies.toArray(new ManagedObjectDependencyMetaData[this.dependencies.size()]);
		}

		@Override
		@SuppressWarnings("unchecked")
		public ManagedObjectFlowMetaData<F>[] getFlowMetaData() {
			this.isBuilt = true;
			return this.flows.toArray(new ManagedObjectFlowMetaData[this.flows.size()]);
		}

		@Override
		public ManagedObjectExecutionMetaData[] getExecutionMetaData() {
			this.isBuilt = true;
			return this.executionStrategies
					.toArray(new ManagedObjectExecutionMetaData[this.executionStrategies.size()]);
		}

		@Override
		public ManagedObjectExtensionMetaData<?>[] getExtensionInterfacesMetaData() {
			this.isBuilt = true;
			return this.extensions.toArray(new ManagedObjectExtensionMetaData[this.extensions.size()]);
		}
	}

	/**
	 * Builder for the {@link RawManagingOfficeMetaData}.
	 */
	public static class RawManagingOfficeMetaDataMockBuilder<F extends Enum<F>> {

		/**
		 * Name of the managing {@link Office}.
		 */
		private final String managingOfficeName;

		/**
		 * {@link ManagingOfficeConfiguration}.
		 */
		private final ManagingOfficeBuilderImpl<F> managingOfficeConfiguration;

		/**
		 * {@link ManagedObjectSourceMetaDataMockBuilder}.
		 */
		private final ManagedObjectSourceMetaDataMockBuilder<?, F> managedObjectSourceMetDataBuilder;

		/**
		 * {@link ManagedFunctionInvocation} instances.
		 */
		private final List<ManagedFunctionInvocation> startupFunctions = new LinkedList<>();

		/**
		 * Recycle {@link ManagedFunction} name.
		 */
		private String recycleFunctionName = null;

		/**
		 * Built {@link RawManagedObjectMetaData}.
		 */
		private RawManagingOfficeMetaData<F> built = null;

		/**
		 * Instantiate.
		 * 
		 * @param managingOfficeName      Name of the managing {@link Office}.
		 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
		 */
		private RawManagingOfficeMetaDataMockBuilder(String managingOfficeName, String managedObjectSourceName) {
			this(managingOfficeName, managedObjectSourceName, new ManagedObjectSourceMetaDataMockBuilder<>());
		}

		/**
		 * Instantiate.
		 * 
		 * @param managingOfficeName                 Name of the managing
		 *                                           {@link Office}.
		 * @param managedObjectSourceName            Name of the
		 *                                           {@link ManagedObjectSource}.
		 * @param managedObjectSourceMetaDataBuilder {@link ManagedObjectSourceMetaDataMockBuilder}.
		 */
		private RawManagingOfficeMetaDataMockBuilder(String managingOfficeName, String managedObjectSourceName,
				ManagedObjectSourceMetaDataMockBuilder<?, F> managedObjectSourceMetaDataBuilder) {
			this.managingOfficeName = managingOfficeName;
			this.managingOfficeConfiguration = new ManagingOfficeBuilderImpl<>(managingOfficeName);
			this.managedObjectSourceMetDataBuilder = managedObjectSourceMetaDataBuilder;
		}

		/**
		 * Ensure not built.
		 */
		private void assetNotBuilt() {
			Assert.assertNull("Should not alter once built", this.built);
		}

		/**
		 * Obtains the {@link ManagingOfficeBuilder}.
		 * 
		 * @return {@link ManagingOfficeBuilder}.
		 */
		public ManagingOfficeBuilder<F> getBuilder() {
			this.assetNotBuilt();
			return this.managingOfficeConfiguration;
		}

		/**
		 * Specifies the recycle {@link ManagedFunction} name.
		 * 
		 * @param recycleFunctionName Recycle {@link ManagedFunction} name.
		 * @return <code>this</code>.
		 */
		public RawManagingOfficeMetaDataMockBuilder<F> recycle(String recycleFunctionName) {
			this.assetNotBuilt();
			this.recycleFunctionName = recycleFunctionName;
			return this;
		}

		/**
		 * Adds a startup {@link ManagedFunctionInvocation}.
		 * 
		 * @param functionName Name of the {@link ManagedFunction}.
		 * @param parameter    Argument to the {@link ManagedFunction}.
		 * @return <code>this</code>.
		 */
		public RawManagingOfficeMetaDataMockBuilder<F> startupFunction(String functionName, Object parameter) {
			this.assetNotBuilt();
			this.startupFunctions.add(new ManagedFunctionInvocationImpl(functionName, parameter));
			return this;
		}

		/**
		 * Obtains the {@link ManagedObjectSourceMetaDataMockBuilder} to configure the
		 * {@link Flow} instances.
		 * 
		 * @return {@link ManagedObjectSourceMetaDataMockBuilder} to configure the
		 *         {@link Flow} instances.
		 */
		public ManagedObjectSourceMetaDataMockBuilder<?, F> getFlowBuilder() {
			this.assetNotBuilt();
			return this.managedObjectSourceMetDataBuilder;
		}

		/**
		 * Builds the {@link RawManagingOfficeMetaData}.
		 * 
		 * @return {@link RawManagingOfficeMetaData}.
		 */
		public RawManagingOfficeMetaData<F> build() {
			if (this.built == null) {
				this.built = new RawManagingOfficeMetaData<>(this.managingOfficeName, this.recycleFunctionName,
						this.managingOfficeConfiguration.getInputManagedObjectConfiguration(),
						this.managedObjectSourceMetDataBuilder.getFlowMetaData(),
						this.managedObjectSourceMetDataBuilder.getExecutionMetaData(), this.managingOfficeConfiguration,
						this.startupFunctions.toArray(new ManagedFunctionInvocation[this.startupFunctions.size()]));
			}
			return this.built;
		}

		/**
		 * Obtains the {@link ManagingOfficeConfiguration}.
		 * 
		 * @return {@link ManagingOfficeConfiguration}.
		 */
		public ManagingOfficeConfiguration<F> getManagingOfficeConfiguration() {
			return this.managingOfficeConfiguration;
		}
	}

	/**
	 * Mock builder for the {@link RawManagedObjectMetaData}.
	 */
	public static class RawManagedObjectMetaDataMockBuilder<O extends Enum<O>, F extends Enum<F>> {

		/**
		 * Name of the {@link ManagedObjectSource}.
		 */
		private final String managedObjectSourceName;

		/**
		 * {@link ManagedObjectSource}.
		 */
		private ManagedObjectSource<O, F> managedObjectSource = new ConstructManagedObjectSource<>();

		/**
		 * {@link ManagedObjectConfiguration}.
		 */
		private final ManagedObjectBuilderImpl<O, F, ConstructManagedObjectSource<O, F>> managedObjectConfiguration;

		/**
		 * {@link ManagedObjectSourceMetaDataMockBuilder}.
		 */
		private final ManagedObjectSourceMetaDataMockBuilder<O, F> managedObjectSourceMetaDataBuilder = new ManagedObjectSourceMetaDataMockBuilder<>();

		/**
		 * Timeout for the {@link ManagedObjectSource}.
		 */
		private long timeout = 0;

		/**
		 * {@link ManagedObjectPool}.
		 */
		private ManagedObjectPool managedObjectPool = null;

		/**
		 * {@link ManagedObjectServiceReady} instances.
		 */
		private final List<ManagedObjectServiceReady> serviceReadiness = new LinkedList<>();

		/**
		 * {@link ThreadCompletionListener} instances.
		 */
		private final List<ThreadCompletionListener> threadCompletionListeners = new LinkedList<>();

		/**
		 * Indicates if {@link ContextAwareManagedObject}.
		 */
		private boolean isContextAware = false;

		/**
		 * Indicates if {@link AsynchronousManagedObject}.
		 */
		private boolean isAsynchronous = false;

		/**
		 * Indicates if CoordinatingManagedObject.
		 */
		private boolean isCoordinating = false;

		/**
		 * Built {@link RawManagedObjectMetaData}.
		 */
		private RawManagedObjectMetaData<O, F> built = null;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectSourceName {@link ManagedObjectSource} name.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private RawManagedObjectMetaDataMockBuilder(String managedObjectSourceName) {
			this.managedObjectSourceName = managedObjectSourceName;
			this.managedObjectConfiguration = new ManagedObjectBuilderImpl(this.managedObjectSourceName,
					ConstructManagedObjectSource.class);
		}

		/**
		 * Ensure not built.
		 */
		private void assetNotBuilt() {
			Assert.assertNull("Should not alter once built", this.built);
		}

		/**
		 * Obtains the {@link ManagedObjectBuilder}.
		 * 
		 * @return {@link ManagedObjectBuilder}.
		 */
		public ManagedObjectBuilder<F> getBuilder() {
			this.assetNotBuilt();
			return this.managedObjectConfiguration;
		}

		/**
		 * Obtains the {@link ManagedObjectSourceMetaDataMockBuilder}.
		 * 
		 * @return {@link ManagedObjectSourceMetaDataMockBuilder}.
		 */
		public ManagedObjectSourceMetaDataMockBuilder<O, F> getMetaDataBuilder() {
			this.assetNotBuilt();
			return this.managedObjectSourceMetaDataBuilder;
		}

		/**
		 * Specifies the {@link ManagedObjectSource}.
		 * 
		 * @param managedObjectSource {@link ManagedObjectSource}.
		 */
		public void setManagedObjectSource(ManagedObjectSource<O, F> managedObjectSource) {
			this.managedObjectSource = managedObjectSource;
		}

		/**
		 * Specifies the timeout.
		 * 
		 * @param timeout Timeout.
		 * @return <code>this</code>.
		 */
		public RawManagedObjectMetaDataMockBuilder<O, F> timeout(long timeout) {
			this.assetNotBuilt();
			this.timeout = timeout;
			return this;
		}

		/**
		 * Specifies the {@link ManagedObjectPool}.
		 * 
		 * @param managedObjectPool {@link ManagedObjectPool}.
		 * @return <code>this</code>.
		 */
		public RawManagedObjectMetaDataMockBuilder<O, F> pool(ManagedObjectPool managedObjectPool) {
			this.assetNotBuilt();
			this.managedObjectPool = managedObjectPool;
			return this;
		}

		/**
		 * Adds a {@link ManagedObjectServiceReady}.
		 * 
		 * @param serviceReady {@link ManagedObjectServiceReady}.
		 * @return <code>this</code>.
		 */
		public RawManagedObjectMetaDataMockBuilder<O, F> serviceReady(ManagedObjectServiceReady serviceReady) {
			this.assetNotBuilt();
			this.serviceReadiness.add(serviceReady);
			return this;
		}

		/**
		 * Adds a {@link ThreadCompletionListener}.
		 * 
		 * @param listener {@link ThreadCompletionListener}.
		 * @return <code>this</code>.
		 */
		public RawManagedObjectMetaDataMockBuilder<O, F> threadCompletionListener(ThreadCompletionListener listener) {
			this.assetNotBuilt();
			this.threadCompletionListeners.add(listener);
			return this;
		}

		/**
		 * Flags as {@link ContextAwareManagedObject}.
		 * 
		 * @return <code>this<code>.
		 */
		public RawManagedObjectMetaDataMockBuilder<O, F> contextAware() {
			this.assetNotBuilt();
			this.isContextAware = true;
			return this;
		}

		/**
		 * Flags as {@link AsynchronousManagedObject}.
		 * 
		 * @return <code>this<code>.
		 */
		public RawManagedObjectMetaDataMockBuilder<O, F> asychronous() {
			this.assetNotBuilt();
			this.isAsynchronous = true;
			return this;
		}

		/**
		 * Flags as {@link CoordinatingManagedObject}.
		 * 
		 * @return <code>this<code>.
		 */
		public RawManagedObjectMetaDataMockBuilder<O, F> coordinating() {
			this.assetNotBuilt();
			this.isCoordinating = true;
			return this;
		}

		/**
		 * Indicates if built.
		 * 
		 * @return <code>true</code> if built.
		 */
		public boolean isBuilt() {
			return this.built != null;
		}

		/**
		 * Builds the {@link RawManagedObjectMetaData}.
		 * 
		 * @return {@link RawManagedObjectMetaData}.
		 */
		public RawManagedObjectMetaData<O, F> build(RawManagingOfficeMetaData<F> rawManagingOfficeMetaData) {
			this.assetNotBuilt();
			this.built = new RawManagedObjectMetaData<>(this.managedObjectSourceName, this.managedObjectConfiguration,
					this.managedObjectSource, this.managedObjectSourceMetaDataBuilder, this.timeout,
					this.managedObjectPool,
					this.serviceReadiness.toArray(new ManagedObjectServiceReady[this.serviceReadiness.size()]),
					this.threadCompletionListeners
							.toArray(new ThreadCompletionListener[this.threadCompletionListeners.size()]),
					this.managedObjectSourceMetaDataBuilder.objectClass, this.isContextAware, this.isAsynchronous,
					this.isCoordinating, rawManagingOfficeMetaData);
			return this.built;
		}

		/**
		 * Obtains the built {@link RawManagedObjectMetaData}.
		 * 
		 * @return {@link RawManagedObjectMetaData}.
		 */
		public RawManagedObjectMetaData<O, F> getBuilt() {
			Assert.assertNotNull(this.getClass().getSimpleName() + " should be built", this.built);
			return this.built;
		}

		/**
		 * Obtains the {@link ManagedObjectSource}.
		 * 
		 * @return {@link ManagedObjectSource}.
		 */
		public ManagedObjectSource<O, F> getManagedObjectSource() {
			return this.managedObjectSource;
		}

		/**
		 * Obtains the {@link ManagedObjectSourceConfiguration}.
		 * 
		 * @return {@link ManagedObjectSourceConfiguration}.
		 */
		public ManagedObjectSourceConfiguration<F, ConstructManagedObjectSource<O, F>> getManagedObjectConfiguratation() {
			return this.managedObjectConfiguration;
		}

		/**
		 * Obtains the {@link ManagedObjectSourceMetaData}.
		 * 
		 * @return {@link ManagedObjectSourceMetaData}.
		 */
		public ManagedObjectSourceMetaData<O, F> getManagedObjectSourceMetaData() {
			return this.managedObjectSourceMetaDataBuilder;
		}
	}

	/**
	 * Builder of a {@link RawBoundManagedObjectMetaData}.
	 */
	public static class RawBoundManagedObjectMetaDataMockBuilder<O extends Enum<O>, F extends Enum<F>> {

		/**
		 * {@link RawManagedObjectMetaDataMockBuilder}.
		 */
		private final RawManagedObjectMetaData<O, F> rawManagedObjectMetaData;

		/**
		 * {@link RawManagedObjectMetaDataMockBuilder} to build the
		 * {@link RawManagedObjectMetaData}.
		 */
		private final RawManagedObjectMetaDataMockBuilder<O, F> rawManagedObjectMetaDataBuilder;

		/**
		 * {@link RawBoundManagedObjectMetaData}.
		 */
		private final RawBoundManagedObjectMetaData rawBoundManagedObjectMetaData;

		/**
		 * Listing of {@link RawBoundManagedObjectInstanceMetaDataMockBuilder}.
		 */
		private final List<RawBoundManagedObjectInstanceMetaDataMockBuilder<O, F>> instances = new LinkedList<>();

		/**
		 * {@link ManagedObjectScope}.
		 */
		private ManagedObjectScope managedOjectScope = ManagedObjectScope.PROCESS;

		/**
		 * Index of this {@link ManagedObject} within the {@link ManagedObjectScope}.
		 */
		private int indexOfManagedObjectWithinScope = 0;

		/**
		 * Indicates if built.
		 */
		private boolean isBuilt = false;

		/**
		 * Instantiate.
		 * 
		 * @param boundManagedObjectName   Bound {@link ManagedObject} name.
		 * @param isInput                  Indicates if input.
		 * @param rawManagedObjectMetaData {@link RawManagedObjectMetaData}.
		 */
		private RawBoundManagedObjectMetaDataMockBuilder(String boundManagedObjectName, boolean isInput,
				RawManagedObjectMetaData<O, F> rawManagedObjectMetaData) {
			this.rawBoundManagedObjectMetaData = new RawBoundManagedObjectMetaData(boundManagedObjectName, isInput,
					null);
			this.rawManagedObjectMetaData = rawManagedObjectMetaData;
			this.rawManagedObjectMetaDataBuilder = null;
		}

		/**
		 * Instantiate.
		 * 
		 * @param boundManagedObjectName   Bound {@link ManagedObject} name.
		 * @param isInput                  Indicates if input.
		 * @param rawManagedObjectMetaData {@link RawManagedObjectMetaDataMockBuilder}.
		 */
		private RawBoundManagedObjectMetaDataMockBuilder(String boundManagedObjectName, boolean isInput,
				RawManagedObjectMetaDataMockBuilder<O, F> rawManagedObjectMetaData) {
			this.rawBoundManagedObjectMetaData = new RawBoundManagedObjectMetaData(boundManagedObjectName, isInput,
					null);
			this.rawManagedObjectMetaData = null;
			this.rawManagedObjectMetaDataBuilder = rawManagedObjectMetaData;
		}

		/**
		 * Ensure not built.
		 */
		private void assetNotBuilt() {
			Assert.assertFalse("Should not alter once built", this.isBuilt);
		}

		/**
		 * Obtains the {@link RawManagedObjectMetaDataMockBuilder}.
		 * 
		 * @return {@link RawManagedObjectMetaDataMockBuilder}.
		 */
		public RawManagedObjectMetaDataMockBuilder<O, F> getRawManagedObjectBuilder() {
			this.assetNotBuilt();
			Assert.assertNotNull("Not mocking the " + RawManagedObjectMetaData.class.getSimpleName(),
					this.rawManagedObjectMetaDataBuilder);
			return this.rawManagedObjectMetaDataBuilder;
		}

		/**
		 * Specifies the {@link ManagedObjectIndex}.
		 * 
		 * @param managedObjectScope              {@link ManagedObjectScope}.
		 * @param indexOfManagedObjectWithinScope Index of {@link ManagedObject} within
		 *                                        the scope.
		 */
		public void setManagedObjectIndex(ManagedObjectScope managedObjectScope, int indexOfManagedObjectWithinScope) {
			this.assetNotBuilt();
			this.managedOjectScope = managedObjectScope;
			this.indexOfManagedObjectWithinScope = indexOfManagedObjectWithinScope;
		}

		/**
		 * Adds a {@link RawBoundManagedObjectInstanceMetaData}.
		 * 
		 * @return {@link RawBoundManagedObjectInstanceMetaDataMockBuilder}.
		 */
		public RawBoundManagedObjectInstanceMetaDataMockBuilder<O, F> addRawBoundManagedObjectInstanceMetaData() {
			this.assetNotBuilt();
			RawBoundManagedObjectInstanceMetaDataMockBuilder<O, F> instance = new RawBoundManagedObjectInstanceMetaDataMockBuilder<>(
					this);
			this.instances.add(instance);
			return instance;
		}

		/**
		 * Builds the {@link RawBoundManagedObjectMetaData}.
		 * 
		 * @return {@link RawBoundManagedObjectMetaData}.
		 */
		public RawBoundManagedObjectMetaData build() {
			if (!this.isBuilt) {
				// Build by specifying managed object index
				this.rawBoundManagedObjectMetaData.setManagedObjectIndex(this.managedOjectScope,
						this.indexOfManagedObjectWithinScope);

				// Build all the instances
				for (RawBoundManagedObjectInstanceMetaDataMockBuilder<O, F> instance : this.instances) {
					instance.build();
				}
				this.isBuilt = true;
			}
			return this.rawBoundManagedObjectMetaData;
		}
	}

	/**
	 * Builder of {@link RawBoundManagedObjectInstanceMetaData}.
	 */
	public static class RawBoundManagedObjectInstanceMetaDataMockBuilder<O extends Enum<O>, F extends Enum<F>> {

		/**
		 * {@link RawBoundManagedObjectMetaDataMockBuilder}.
		 */
		private final RawBoundManagedObjectMetaDataMockBuilder<O, F> rawBoundManagedObjectMetaData;

		/**
		 * Configuration.
		 */
		private final DependencyMappingBuilderImpl<O> configuration;

		/**
		 * Build {@link RawBoundManagedObjectInstanceMetaData}.
		 */
		private RawBoundManagedObjectInstanceMetaData<?> built = null;

		/**
		 * Instantiate.
		 * 
		 * @param rawBoundManagedObjectMetaData {@link RawBoundManagedObjectMetaDataMockBuilder}.
		 */
		private RawBoundManagedObjectInstanceMetaDataMockBuilder(
				RawBoundManagedObjectMetaDataMockBuilder<O, F> rawBoundManagedObjectMetaData) {
			this.rawBoundManagedObjectMetaData = rawBoundManagedObjectMetaData;
			this.configuration = new DependencyMappingBuilderImpl<>(
					rawBoundManagedObjectMetaData.rawBoundManagedObjectMetaData.getBoundManagedObjectName());
		}

		/**
		 * Obtains the {@link RawBoundManagedObjectMetaDataMockBuilder}.
		 * 
		 * @return {@link RawBoundManagedObjectMetaDataMockBuilder}.
		 */
		public RawBoundManagedObjectMetaDataMockBuilder<O, F> getRawBoundManagedObjectMetaData() {
			return this.rawBoundManagedObjectMetaData;
		}

		/**
		 * Obtains the {@link DependencyMappingBuilder}.
		 * 
		 * @return {@link DependencyMappingBuilder}.
		 */
		public DependencyMappingBuilder getBuilder() {
			return this.configuration;
		}

		/**
		 * Builds the {@link RawBoundManagedObjectInstanceMetaData}.
		 * 
		 * @return {@link RawBoundManagedObjectInstanceMetaData}.
		 */
		@SuppressWarnings("unchecked")
		public RawBoundManagedObjectInstanceMetaData<O> build() {
			if (this.built == null) {

				// Obtain the built managed object meta-data
				RawManagedObjectMetaData<O, F> rawManagedObjectMetaData;
				if (this.rawBoundManagedObjectMetaData.rawManagedObjectMetaData != null) {
					rawManagedObjectMetaData = this.rawBoundManagedObjectMetaData.rawManagedObjectMetaData;
				} else if (this.rawBoundManagedObjectMetaData.rawManagedObjectMetaDataBuilder.isBuilt()) {
					rawManagedObjectMetaData = this.rawBoundManagedObjectMetaData.rawManagedObjectMetaDataBuilder
							.getBuilt();
				} else {
					RawManagingOfficeMetaDataMockBuilder<F> managingingOffice = MockConstruct
							.mockRawManagingOfficeMetaData(OFFICE_NAME, this.configuration.getBoundManagedObjectName());
					rawManagedObjectMetaData = this.rawBoundManagedObjectMetaData.rawManagedObjectMetaDataBuilder
							.build(managingingOffice.build());
				}

				// Build the instance
				this.built = this.rawBoundManagedObjectMetaData.rawBoundManagedObjectMetaData.addInstance(
						this.rawBoundManagedObjectMetaData.rawBoundManagedObjectMetaData.getBoundManagedObjectName(),
						rawManagedObjectMetaData, this.configuration.getDependencyConfiguration(),
						this.configuration.getGovernanceConfiguration(), this.configuration.getPreLoadAdministration());
			}
			return (RawBoundManagedObjectInstanceMetaData<O>) this.built;
		}
	}

	/**
	 * Builder to build {@link RawGovernanceMetaData}.
	 */
	public static class RawGovernanceMetaDataMockBuilder<E, F extends Enum<F>> {

		/**
		 * Name of the {@link Governance}.
		 */
		private final String governanceName;

		/**
		 * Extension type.
		 */
		private final Class<E> extensionType;

		/**
		 * {@link GovernanceConfiguration}.
		 */
		private final GovernanceBuilderImpl<E, F> configuration;

		/**
		 * {@link GovernanceMetaDataImpl}.
		 */
		private final GovernanceMetaDataImpl<E, F> metaData;

		/**
		 * Index of the {@link Governance}.
		 */
		private int governanceIndex = 0;

		/**
		 * Build {@link RawGovernanceMetaData}.
		 */
		private RawGovernanceMetaData<E, F> built = null;

		/**
		 * Instantiate.
		 * 
		 * @param governanceName Name of the {@link Governance}.
		 */
		private RawGovernanceMetaDataMockBuilder(String governanceName, Class<E> extensionType) {
			this.governanceName = governanceName;
			this.extensionType = extensionType;
			this.configuration = new GovernanceBuilderImpl<>(governanceName, extensionType, () -> null);
			this.metaData = new GovernanceMetaDataImpl<>(governanceName, () -> null, null, 1, null, null);
		}

		/**
		 * Ensures not built.
		 */
		private void assetNotBuilt() {
			Assert.assertNull("Should not change after being built", this.built);
		}

		/**
		 * Specifies the {@link Governance} index.
		 * 
		 * @param governanceIndex Index of the {@link Governance}.
		 * @return <code>this</code>.
		 */
		public RawGovernanceMetaDataMockBuilder<E, F> index(int governanceIndex) {
			this.assetNotBuilt();
			this.governanceIndex = governanceIndex;
			return this;
		}

		/**
		 * Obtains the {@link GovernanceBuilder}.
		 * 
		 * @return {@link GovernanceBuilder}.
		 */
		public GovernanceBuilder<F> getBuilder() {
			return this.configuration;
		}

		/**
		 * Builds the {@link RawGovernanceMetaData}.
		 * 
		 * @return {@link RawGovernanceMetaData}.
		 */
		public RawGovernanceMetaData<E, F> build() {
			if (this.built == null) {
				this.built = new RawGovernanceMetaData<>(this.governanceName, this.governanceIndex, this.extensionType,
						this.configuration, this.metaData);
			}
			return this.built;
		}

		/**
		 * Obtains the {@link GovernanceConfiguration}.
		 * 
		 * @return {@link GovernanceConfiguration}.
		 */
		public GovernanceConfiguration<E, F> getGovernanceConfiguration() {
			return this.configuration;
		}

		/**
		 * Obtains the {@link GovernanceMetaData}.
		 * 
		 * @return {@link GovernanceMetaData}.
		 */
		public GovernanceMetaData<E, F> getGovernanceMetaData() {
			return this.metaData;
		}
	}

	/**
	 * Builder to build {@link RawOfficeFloorMetaData}.
	 */
	public static class RawOfficeMetaDataMockBuilder {

		/**
		 * Name of the {@link Office}.
		 */
		private final String officeName;

		/**
		 * {@link RawOfficeFloorMetaData}.
		 */
		private RawOfficeFloorMetaData rawOfficeFloorMetaData = null;

		/**
		 * Teams.
		 */
		private final Map<String, TeamManagement> teams = new HashMap<>();

		/**
		 * {@link RawManagedObjectMetaData} instances.
		 */
		private Map<String, RawManagedObjectMetaDataMockBuilder<?, ?>> registeredManagedObjects = new HashMap<>();

		/**
		 * {@link ProcessState} bound {@link RawBoundManagedObjectMetaData} instances.
		 */
		private final List<RawBoundManagedObjectMetaData> processManagedObjects = new LinkedList<>();

		/**
		 * {@link ThreadState} bound {@link RawBoundManagedObjectMetaData} instances.
		 */
		private final List<RawBoundManagedObjectMetaData> threadManagedObjects = new LinkedList<>();

		/**
		 * Scope bound RawBoundManagedObjectMetaData instances.
		 */
		private Map<String, RawBoundManagedObjectMetaDataMockBuilder<?, ?>> scopeManagedObjects = new HashMap<>();

		/**
		 * Indicates if manually manage {@link Governance}.
		 */
		private boolean isManuallyManageGovernance = false;

		/**
		 * {@link RawGovernanceMetaData} instances.
		 */
		private Map<String, RawGovernanceMetaDataMockBuilder<?, ?>> governanceMetaData = new HashMap<>();

		/**
		 * Built {@link RawOfficeMetaData}.
		 */
		private RawOfficeMetaData built = null;

		/**
		 * Instantiate.
		 * 
		 * @param officeName Name of the {@link Office}.
		 */
		private RawOfficeMetaDataMockBuilder(String officeName) {
			this.officeName = officeName;
		}

		/**
		 * Ensure not built.
		 */
		private void assetNotBuilt() {
			Assert.assertNull("Should not change once built", this.built);
		}

		/**
		 * Specifies the {@link RawOfficeFloorMetaData}.
		 * 
		 * @param rawOfficeFloorMetaData {@link RawOfficeFloorMetaData}.
		 */
		public void setRawOfficeFloorMetaData(RawOfficeFloorMetaData rawOfficeFloorMetaData) {
			this.assetNotBuilt();
			this.rawOfficeFloorMetaData = rawOfficeFloorMetaData;
		}

		/**
		 * Registers a {@link Team}.
		 * 
		 * @param teamName Name of the {@link Team}.
		 */
		public TeamManagement addTeam(String teamName) {
			this.assetNotBuilt();
			TeamManagement team = new TeamManagementImpl(null);
			this.teams.put(teamName, team);
			return team;
		}

		/**
		 * Adds a registered {@link RawManagedObjectMetaData}.
		 * 
		 * @param registeredManagedObjectName Name of the registered
		 *                                    {@link RawManagedObjectMetaData}.
		 * @return {@link RawManagedObjectMetaDataMockBuilder} for the
		 *         {@link RawManagedObjectMetaData}.
		 */
		public <O extends Enum<O>, F extends Enum<F>> RawManagedObjectMetaDataMockBuilder<O, F> addRegisteredManagedObject(
				String registeredManagedObjectName) {
			this.assetNotBuilt();
			RawManagedObjectMetaDataMockBuilder<O, F> mo = MockConstruct
					.mockRawManagedObjectMetaData(registeredManagedObjectName);
			this.registeredManagedObjects.put(registeredManagedObjectName, mo);
			return mo;
		}

		/**
		 * Adds a scope bound {@link RawBoundManagedObjectMetaData}.
		 * 
		 * @param scopeManagedObjectName Name of the
		 *                               {@link RawBoundManagedObjectMetaData}.
		 * @return {@link RawBoundManagedObjectMetaDataMockBuilder} for the
		 *         {@link RawBoundManagedObjectMetaData}.
		 */
		public <O extends Enum<O>, F extends Enum<F>> RawBoundManagedObjectInstanceMetaDataMockBuilder<O, F> addScopeBoundManagedObject(
				String scopeManagedObjectName) {
			this.assetNotBuilt();

			// Obtain the registered managed object
			@SuppressWarnings("unchecked")
			RawManagedObjectMetaDataMockBuilder<O, F> mo = (RawManagedObjectMetaDataMockBuilder<O, F>) this.registeredManagedObjects
					.get(scopeManagedObjectName);
			if (mo == null) {
				mo = this.addRegisteredManagedObject(scopeManagedObjectName);
			}

			// Register the bound scope managed object
			RawBoundManagedObjectMetaDataMockBuilder<O, F> bound = MockConstruct
					.mockRawBoundManagedObjectMetaData(scopeManagedObjectName, mo);
			this.scopeManagedObjects.put(scopeManagedObjectName, bound);

			// Return the instance for the bound managed object
			RawBoundManagedObjectInstanceMetaDataMockBuilder<O, F> instance = bound
					.addRawBoundManagedObjectInstanceMetaData();
			return instance;
		}

		/**
		 * Adds {@link RawGovernanceMetaData}.
		 * 
		 * @param governanceName Name of the {@link Governance}.
		 * @param extensionType  Extension type used by the {@link Governance}.
		 * @return {@link RawGovernanceMetaDataMockBuilder} for the
		 *         {@link RawGovernanceMetaData}.
		 */
		public <E, F extends Enum<F>> RawGovernanceMetaDataMockBuilder<E, F> addGovernance(String governanceName,
				Class<E> extensionType) {
			this.assetNotBuilt();
			RawGovernanceMetaDataMockBuilder<E, F> governance = MockConstruct.mockRawGovernanceMetaData(governanceName,
					extensionType);
			governance.index(this.governanceMetaData.size());
			this.governanceMetaData.put(governanceName, governance);
			return governance;
		}

		/**
		 * Flags for manual {@link Governance}.
		 * 
		 * @return <code>this</code>.
		 */
		public RawOfficeMetaDataMockBuilder manualGovernance() {
			this.assetNotBuilt();
			this.isManuallyManageGovernance = true;
			return this;
		}

		/**
		 * Builds the {@link RawOfficeMetaData}.
		 * 
		 * @return Built {@link RawOfficeMetaData}.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public RawOfficeMetaData build() {
			if (this.built == null) {

				// Build the registered managed objects
				Map<String, RawManagedObjectMetaData<?, ?>> registeredManagedObjects = new HashMap<>();
				for (String name : this.registeredManagedObjects.keySet()) {
					RawManagedObjectMetaDataMockBuilder<?, ?> rawManagedObject = this.registeredManagedObjects
							.get(name);
					if (rawManagedObject.isBuilt()) {
						registeredManagedObjects.put(name, rawManagedObject.getBuilt());
					} else {
						RawManagingOfficeMetaDataMockBuilder managingOffice = MockConstruct
								.mockRawManagingOfficeMetaData(OFFICE_NAME, name);
						registeredManagedObjects.put(name,
								this.registeredManagedObjects.get(name).build(managingOffice.build()));
					}
				}

				// Build the scope managed objects
				Map<String, RawBoundManagedObjectMetaData> scopeBoundManagedObjects = new HashMap<>();
				for (String name : this.scopeManagedObjects.keySet()) {
					scopeBoundManagedObjects.put(name, this.scopeManagedObjects.get(name).build());
				}

				// Build the governance
				Map<String, RawGovernanceMetaData<?, ?>> governanceMetaData = new HashMap<>();
				for (String name : this.governanceMetaData.keySet()) {
					governanceMetaData.put(name, this.governanceMetaData.get(name).build());
				}

				// Build the raw office meta-data
				this.built = new RawOfficeMetaData(this.officeName, this.rawOfficeFloorMetaData, this.teams,
						registeredManagedObjects,
						this.processManagedObjects
								.toArray(new RawBoundManagedObjectMetaData[this.processManagedObjects.size()]),
						this.threadManagedObjects
								.toArray(new RawBoundManagedObjectMetaData[this.threadManagedObjects.size()]),
						scopeBoundManagedObjects, this.isManuallyManageGovernance, governanceMetaData);
			}
			return this.built;
		}

		/**
		 * Obtains the
		 * 
		 * @return
		 */
		public Map<String, TeamManagement> getOfficeTeams() {
			return this.teams;
		}
	}

	/**
	 * Builder to build {@link RawOfficeFloorMetaData}.
	 */
	public static class RawOfficeFloorMetaDataMockBuilder {

		/**
		 * {@link RawTeamMetaData}.
		 */
		private final Map<String, RawTeamMetaData> teamRegistry = new HashMap<>();

		/**
		 * {@link ThreadLocalAwareExecutor}.
		 */
		private ThreadLocalAwareExecutor threadLocalAwareExecutor = new ThreadLocalAwareExecutorImpl();

		/**
		 * {@link ManagedExecutionFactory}.
		 */
		private ManagedExecutionFactory managedExecutionFactory = new ManagedExecutionFactoryImpl(null);

		/**
		 * {@link RawManagedObjectMetaData} registry.
		 */
		private Map<String, RawManagedObjectMetaDataMockBuilder<?, ?>> mosRegistry = new HashMap<>();

		/**
		 * {@link EscalationFlow}.
		 */
		private EscalationFlow officeFloorEscalation = new EscalationFlowImpl(Throwable.class,
				new ManagedFunctionMetaDataImpl<>("HANDLER", () -> null, null, null, null, null, null, null, 1, null,
						null));

		/**
		 * {@link RawOfficeFloorMetaData}.
		 */
		private RawOfficeFloorMetaData built = null;

		/**
		 * Instantiate.
		 */
		private RawOfficeFloorMetaDataMockBuilder() {
		}

		/**
		 * Ensure not built.
		 */
		private void assertNotBuilt() {
			Assert.assertNull("Should not alter once built", this.built);
		}

		/**
		 * Convenience method to register {@link Team} that is not {@link ThreadLocal}
		 * aware.
		 * 
		 * @param teamName Name of the {@link Team}.
		 * @param team     {@link Team}.
		 * @return {@link RawTeamMetaData}.
		 */
		public RawTeamMetaData registerTeam(String teamName, Team team) {
			return this.registerTeam(teamName, team, false);
		}

		/**
		 * Registers a {@link RawTeamMetaData}.
		 * 
		 * @param teamName                      Name of the {@link Team}.
		 * @param team                          {@link Team}.
		 * @param isRequireThreadLocalAwareness <code>true</code> if requires
		 *                                      {@link ThreadLocal} awareness.
		 * @return {@link RawTeamMetaData}.
		 */
		public RawTeamMetaData registerTeam(String teamName, Team team, boolean isRequireThreadLocalAwareness) {
			this.assertNotBuilt();
			RawTeamMetaData rawTeam = new RawTeamMetaData(teamName, new TeamManagementImpl(team), false);
			this.teamRegistry.put(teamName, rawTeam);
			return rawTeam;
		}

		/**
		 * Registers the {@link RawManagedObjectMetaData}.
		 * 
		 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
		 * @return {@link RawManagedObjectMetaDataMockBuilder} for the
		 *         {@link RawManagedObjectMetaData}.
		 */
		public RawManagedObjectMetaDataMockBuilder<?, ?> registerManagedObjectSource(String managedObjectSourceName) {
			RawManagedObjectMetaDataMockBuilder<?, ?> mos = mockRawManagedObjectMetaData(managedObjectSourceName);
			this.mosRegistry.put(managedObjectSourceName, mos);
			return mos;
		}

		/**
		 * Specifies the {@link EscalationFlow} for the {@link OfficeFloor}.
		 * 
		 * @param escalationFlow {@link EscalationFlow} for the {@link OfficeFloor}.
		 */
		public void setOfficeFloorEscalation(EscalationFlow escalationFlow) {
			this.officeFloorEscalation = escalationFlow;
		}

		/**
		 * Builds the {@link RawOfficeFloorMetaData}.
		 * 
		 * @return {@link RawOfficeFloorMetaData}.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public RawOfficeFloorMetaData build() {
			if (this.built == null) {

				// Build the managed object registry
				Map<String, RawManagedObjectMetaData<?, ?>> mosRegistry = new HashMap<>();
				for (String name : this.mosRegistry.keySet()) {
					RawManagedObjectMetaDataMockBuilder<?, ?> mos = this.mosRegistry.get(name);
					if (mos.isBuilt()) {
						mosRegistry.put(name, mos.getBuilt());
					} else {
						RawManagingOfficeMetaData office = MockConstruct
								.mockRawManagingOfficeMetaData(OFFICE_NAME, name).build();
						mosRegistry.put(name, mos.build(office));
					}
				}

				// Create the executive
				Executive executive = new DefaultExecutive();
				ThreadFactory[] defaultExecutionStrategy = new ThreadFactory[0];
				Map<String, ThreadFactory[]> executionStrategies = new HashMap<>();

				// Build
				this.built = new RawOfficeFloorMetaData(executive, defaultExecutionStrategy, executionStrategies,
						this.teamRegistry, new BackgroundScheduling[0], new Object(), this.threadLocalAwareExecutor,
						this.managedExecutionFactory, mosRegistry, this.officeFloorEscalation,
						new OfficeFloorListener[0]);
			}
			return this.built;
		}
	}

	/**
	 * Builder to build {@link OfficeMetaData}.
	 */
	public static class OfficeMetaDataMockBuilder {

		/**
		 * Name of the {@link Office}.
		 */
		private final String officeName;

		/**
		 * {@link OfficeConfiguration}.
		 */
		private final OfficeBuilderImpl builder;

		/**
		 * {@link ProcessMetaDataMockBuilder}.
		 */
		private final ProcessMetaDataMockBuilder processMetaData = new ProcessMetaDataMockBuilder();

		/**
		 * {@link ManagedFunctionMetaData} instances.
		 */
		private final List<ManagedFunctionMetaDataImpl<?, ?>> functions = new LinkedList<>();

		/**
		 * Built {@link OfficeMetaData}.
		 */
		private OfficeMetaData built = null;

		/**
		 * Instantiate.
		 * 
		 * @param officeName Name of the {@link Office}.
		 */
		private OfficeMetaDataMockBuilder(String officeName) {
			this.officeName = officeName;
			this.builder = new OfficeBuilderImpl(officeName);
		}

		/**
		 * Ensure not built.
		 */
		private void assertNotBuilt() {
			Assert.assertNull("Should not alter once built", this.built);
		}

		/**
		 * Obtains {@link ProcessMetaDataMockBuilder}.
		 * 
		 * @return {@link ProcessMetaDataMockBuilder}.
		 */
		public ProcessMetaDataMockBuilder getProcessMetaData() {
			this.assertNotBuilt();
			return this.processMetaData;
		}

		/**
		 * Obtains the {@link OfficeBuilder}.
		 * 
		 * @return {@link OfficeBuilder}.
		 */
		public OfficeBuilder getBuilder() {
			this.assertNotBuilt();
			return this.builder;
		}

		/**
		 * Adds a {@link ManagedFunction} to the {@link OfficeMetaData}.
		 * 
		 * @param functionName  Name of the {@link ManagedFunction}.
		 * @param parameterType to the {@link ManagedFunction}.
		 * @return {@link ManagedFunctionMetaData} for the {@link ManagedFunction}.
		 */
		public ManagedFunctionMetaData<?, ?> addManagedFunction(String functionName, Class<?> parameterType) {
			this.assertNotBuilt();
			ManagedFunctionMetaDataImpl<?, ?> function = new ManagedFunctionMetaDataImpl<>(functionName,
					() -> (context) -> {
					}, new Object[0], parameterType, null, new ManagedObjectIndex[0], new ManagedObjectMetaData[0],
					new boolean[0], 1, null, OfficeFrame.getLogger(functionName));
			this.functions.add(function);
			return function;
		}

		/**
		 * Builds the {@link OfficeMetaData}.
		 * 
		 * @return {@link OfficeMetaData}.
		 */
		public OfficeMetaData build() {
			if (this.built == null) {

				// Load the managed functions
				List<ManagedFunctionMetaDataImpl<?, ?>> functions = new LinkedList<>();

				// Obtain the builder configured managed functions
				for (ManagedFunctionConfiguration<?, ?> config : this.builder.getManagedFunctionConfiguration()) {
					String functionName = config.getFunctionName();
					Class<?> parameterType = null;
					for (ManagedFunctionObjectConfiguration<?> objectConfig : config.getObjectConfiguration()) {
						if (objectConfig.isParameter()) {
							parameterType = objectConfig.getObjectType();
						}
					}
					functions.add(new ManagedFunctionMetaDataImpl<>(functionName, null, null, parameterType, null, null,
							null, null, 1, null, null));
				}

				// Load the convenience functions
				for (ManagedFunctionMetaDataImpl<?, ?> function : this.functions) {
					functions.add(function);
				}

				// Load the convenience functions
				ManagedFunctionLocator functionLocator = new ManagedFunctionLocatorImpl(
						functions.toArray(new ManagedFunctionMetaData[functions.size()]));
				this.built = new OfficeMetaDataImpl(this.officeName, null, null, null, null, null, null, null,
						functionLocator, this.processMetaData.build(), null, null, null, null);

				// Load the office meta-data to functions
				for (ManagedFunctionMetaDataImpl<?, ?> function : functions) {
					function.loadOfficeMetaData(this.built, new FlowMetaData[0], null, null,
							new ManagedFunctionAdministrationMetaData[0], new ManagedFunctionAdministrationMetaData[0],
							new ManagedObjectIndex[0]);
				}
			}
			return this.built;
		}
	}

	/**
	 * Builder for the {@link ProcessMetaData}.
	 */
	public static class ProcessMetaDataMockBuilder {

		/**
		 * Built {@link ProcessMetaData}.
		 */
		private ProcessMetaData built = null;

		/**
		 * {@link ManagedObjectMetaData} instances.
		 */
		private List<ManagedObjectMetaData<?>> managedObjects = new LinkedList<>();

		/**
		 * {@link ThreadMetaData} for the {@link ProcessMetaData}.
		 */
		private final ThreadMetaDataMockBuilder thread = new ThreadMetaDataMockBuilder();

		/**
		 * Instantiate.
		 */
		private ProcessMetaDataMockBuilder() {
		}

		/**
		 * Ensure not built.
		 */
		private void assertNotBuilt() {
			Assert.assertNull("Should not alter once built", this.built);
		}

		/**
		 * Obtains the {@link ThreadMetaDataMockBuilder}.
		 * 
		 * @return {@link ThreadMetaDataMockBuilder}.
		 */
		public ThreadMetaDataMockBuilder getThreadMetaData() {
			this.assertNotBuilt();
			return this.thread;
		}

		/**
		 * Adds a {@link ManagedObjectMetaData}.
		 * 
		 * @param boundManagedObjectName Name of {@link ManagedObject}.
		 * @param objectType             Object type of {@link ManagedObject}.
		 * @return {@link ManagedObjectMetaData}.
		 */
		public ManagedObjectMetaData<?> addManagedObjectMetaData(String boundManagedObjectName, Class<?> objectType) {
			this.assertNotBuilt();
			ManagedObjectMetaData<?> mo = mockManagedObjectMetaData(boundManagedObjectName, objectType);
			this.managedObjects.add(mo);
			return mo;
		}

		/**
		 * Builds the {@link ProcessMetaData}.
		 * 
		 * @return {@link ProcessMetaData}.
		 */
		public ProcessMetaData build() {
			if (this.built == null) {
				this.built = new ProcessMetaDataImpl(
						this.managedObjects.toArray(new ManagedObjectMetaData[this.managedObjects.size()]),
						this.thread.built());
			}
			return this.built;
		}
	}

	/**
	 * Builder for the {@link ThreadMetaData}.
	 */
	public static class ThreadMetaDataMockBuilder {

		/**
		 * Built {@link ThreadMetaData}.
		 */
		private ThreadMetaData built = null;

		/**
		 * {@link ManagedObjectMetaData} instances.
		 */
		private List<ManagedObjectMetaData<?>> managedObjects = new LinkedList<>();

		/**
		 * {@link GovernanceMetaData} instances.
		 */
		private List<GovernanceMetaData<?, ?>> governances = new LinkedList<>();

		/**
		 * Instantiate.
		 */
		private ThreadMetaDataMockBuilder() {
		}

		/**
		 * Ensure not built.
		 */
		private void assertNotBuilt() {
			Assert.assertNull("Should not alter once built", this.built);
		}

		/**
		 * Adds a {@link ManagedObjectMetaData}.
		 * 
		 * @param boundManagedObjectName Name of {@link ManagedObject}.
		 * @param objectType             Object type of {@link ManagedObject}.
		 * @return {@link ManagedObjectMetaData}.
		 */
		public ManagedObjectMetaData<?> addManagedObjectMetaData(String boundManagedObjectName, Class<?> objectType) {
			this.assertNotBuilt();
			ManagedObjectMetaData<?> mo = mockManagedObjectMetaData(boundManagedObjectName, objectType);
			this.managedObjects.add(mo);
			return mo;
		}

		/**
		 * Adds a {@link GovernanceMetaData}.
		 * 
		 * @param governanceName Name of {@link Governance}.
		 * @return {@link GovernanceMetaData}.
		 */
		public GovernanceMetaData<?, ?> addGovernanceMetaData(String governanceName) {
			this.assertNotBuilt();
			GovernanceMetaData<?, ?> governance = mockRawGovernanceMetaData(governanceName, Object.class)
					.getGovernanceMetaData();
			this.governances.add(governance);
			return governance;
		}

		/**
		 * Builds the {@link ThreadMetaData}.
		 * 
		 * @return {@link ThreadMetaData}.
		 */
		public ThreadMetaData built() {
			if (this.built == null) {
				this.built = new ThreadMetaDataImpl(
						this.managedObjects.toArray(new ManagedObjectMetaData[this.managedObjects.size()]),
						this.governances.toArray(new GovernanceMetaData[this.governances.size()]), 1000,
						new ThreadSynchroniserFactory[0], null, null);
			}
			return this.built;
		}
	}

	/**
	 * All access via static methods.
	 */
	private MockConstruct() {
	}

}
