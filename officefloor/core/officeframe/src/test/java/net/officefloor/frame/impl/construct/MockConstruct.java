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
package net.officefloor.frame.impl.construct;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;

import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ThreadCompletionListener;
import net.officefloor.frame.api.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExtensionMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFlowMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectDependencyMetaDataImpl;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectExtensionMetaDataImpl;
import net.officefloor.frame.api.managedobject.source.impl.ManagedObjectFlowMetaDataImpl;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.construct.asset.AssetManagerFactory;
import net.officefloor.frame.impl.construct.managedobject.DependencyMappingBuilderImpl;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectInstanceMetaData;
import net.officefloor.frame.impl.construct.managedobject.RawBoundManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagedObjectBuilderImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.ManagingOfficeBuilderImpl;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagedObjectMetaData;
import net.officefloor.frame.impl.construct.managedobjectsource.RawManagingOfficeMetaData;
import net.officefloor.frame.impl.construct.office.ManagedFunctionLocatorImpl;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionMetaDataImpl;
import net.officefloor.frame.impl.execute.managedobject.ManagedObjectMetaDataImpl;
import net.officefloor.frame.impl.execute.office.OfficeMetaDataImpl;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectConfiguration;
import net.officefloor.frame.internal.configuration.ManagedObjectSourceConfiguration;
import net.officefloor.frame.internal.configuration.ManagingOfficeConfiguration;
import net.officefloor.frame.internal.configuration.OfficeConfiguration;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.OfficeMetaData;
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
	 * Creates a mock {@link ManagedObjectBuilderImpl}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectBuilderImpl}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> ManagedObjectBuilderImpl<O, F, MockConstructManagedObjectSource<O, F>> mockManagedObjectBuilder(
			String managedObjectSourceName) {
		return new ManagedObjectBuilderImpl<O, F, MockConstructManagedObjectSource<O, F>>(managedObjectSourceName,
				new MockConstructManagedObjectSource<>());
	}

	/**
	 * Creates a {@link MockOfficeMetaDataBuilder}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @return {@link MockOfficeMetaDataBuilder}.
	 */
	public static MockOfficeMetaDataBuilder mockOfficeMetaData(String officeName) {
		return new MockOfficeMetaDataBuilder(officeName);
	}

	/**
	 * Creates mock {@link ManagedObjectMetaDataImpl}.
	 * 
	 * @param boundManagedObjectName
	 *            Bound {@link ManagedObject} name.
	 * @param objectType
	 *            Object type of {@link ManagedObject}.
	 * @return Mock {@link ManagedObjectMetaDataImpl}.
	 */
	public static <O extends Enum<O>> ManagedObjectMetaDataImpl<O> mockManagedObjectMetaData(
			String boundManagedObjectName, Class<?> objectType) {
		AssetManagerFactory assetManagerFactory = mockAssetManagerFactory();
		return new ManagedObjectMetaDataImpl<>(boundManagedObjectName, objectType, 0,
				new MockConstructManagedObjectSource<>(), null, false, false,
				assetManagerFactory.createAssetManager(AssetType.MANAGED_OBJECT, boundManagedObjectName, "mock", null),
				false, null, false, null, 0, null);
	}

	/**
	 * Creates a {@link MockManagedObjectSourceMetaDataBuilder} to build a
	 * {@link ManagedObjectSourceMetaData}.
	 * 
	 * @return {@link MockManagedObjectSourceMetaDataBuilder} to build a
	 *         {@link ManagedObjectSourceMetaData}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> MockManagedObjectSourceMetaDataBuilder<O, F> mockManagedObjectSourceMetaData() {
		return new MockManagedObjectSourceMetaDataBuilder<>();
	}

	/**
	 * Creates a {@link MockRawManagedObjectMetaDataBuilder} to build a
	 * {@link RawManagedObjectMetaData}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @return {@link MockRawManagedObjectMetaDataBuilder}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> MockRawManagedObjectMetaDataBuilder<O, F> mockRawManagedObjectMetaData(
			String managedObjectSourceName) {
		return new MockRawManagedObjectMetaDataBuilder<>(managedObjectSourceName);
	}

	/**
	 * Creates a {@link MockRawManagingOfficeMetaDataBuilder} to build a
	 * {@link RawManagingOfficeMetaData}.
	 * 
	 * @param managingOfficeName
	 *            Name of the managing {@link Office}.
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @return {@link MockRawManagingOfficeMetaDataBuilder}.
	 */
	public static <F extends Enum<F>> MockRawManagingOfficeMetaDataBuilder<F> mockRawManagingOfficeMetaData(
			String managingOfficeName, String managedObjectSourceName) {
		return new MockRawManagingOfficeMetaDataBuilder<>(managingOfficeName, managedObjectSourceName);
	}

	/**
	 * Creates a {@link MockRawBoundManagedObjectMetaDataBuilder} to build a
	 * {@link RawBoundManagedObjectMetaData} (that is not an input).
	 * 
	 * @param boundManagedObjectName
	 *            Bound {@link ManagedObject} name.
	 * @param rawManagedObjectMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @return {@link MockRawBoundManagedObjectMetaDataBuilder}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> MockRawBoundManagedObjectMetaDataBuilder<O, F> mockRawBoundManagedObjectMetaData(
			String boundManagedObjectName, RawManagedObjectMetaData<O, F> rawManagedObjectMetaData) {
		return new MockRawBoundManagedObjectMetaDataBuilder<>(boundManagedObjectName, false, rawManagedObjectMetaData);
	}

	/**
	 * Creates a {@link MockRawBoundManagedObjectMetaDataBuilder} to build a
	 * {@link RawBoundManagedObjectMetaData}.
	 * 
	 * @param boundManagedObjectName
	 *            Bound {@link ManagedObject} name.
	 * @param isInput
	 *            Indicates if input.
	 * @param rawManagedObjectMetaData
	 *            {@link RawManagedObjectMetaData}.
	 * @return {@link MockRawBoundManagedObjectMetaDataBuilder}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> MockRawBoundManagedObjectMetaDataBuilder<O, F> mockRawBoundManagedObjectMetaData(
			String boundManagedObjectName, boolean isInput, RawManagedObjectMetaData<O, F> rawManagedObjectMetaData) {
		return new MockRawBoundManagedObjectMetaDataBuilder<>(boundManagedObjectName, isInput,
				rawManagedObjectMetaData);
	}

	/**
	 * Creates a {@link MockRawBoundManagedObjectMetaDataBuilder} to build a
	 * {@link RawBoundManagedObjectMetaData} (that is not an input).
	 * 
	 * @param boundManagedObjectName
	 *            Bound {@link ManagedObject} name.
	 * @param rawManagedObjectMetaData
	 *            {@link MockRawManagedObjectMetaDataBuilder}.
	 * @return {@link MockRawBoundManagedObjectMetaDataBuilder}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> MockRawBoundManagedObjectMetaDataBuilder<O, F> mockRawBoundManagedObjectMetaData(
			String boundManagedObjectName, MockRawManagedObjectMetaDataBuilder<O, F> rawManagedObjectMetaData) {
		return mockRawBoundManagedObjectMetaData(boundManagedObjectName, false, rawManagedObjectMetaData);
	}

	/**
	 * Creates a {@link MockRawBoundManagedObjectMetaDataBuilder} to build a
	 * {@link RawBoundManagedObjectMetaData} (that is not an input).
	 * 
	 * @param boundManagedObjectName
	 *            Bound {@link ManagedObject} name.
	 * @param isInput
	 *            Indicates if input.
	 * @param rawManagedObjectMetaData
	 *            {@link MockRawManagedObjectMetaDataBuilder}.
	 * @return {@link MockRawBoundManagedObjectMetaDataBuilder}.
	 */
	public static <O extends Enum<O>, F extends Enum<F>> MockRawBoundManagedObjectMetaDataBuilder<O, F> mockRawBoundManagedObjectMetaData(
			String boundManagedObjectName, boolean isInput,
			MockRawManagedObjectMetaDataBuilder<O, F> rawManagedObjectMetaData) {
		return new MockRawBoundManagedObjectMetaDataBuilder<>(boundManagedObjectName, isInput,
				rawManagedObjectMetaData);
	}

	/**
	 * Creates a mock {@link AssetManagerFactory}.
	 * 
	 * @return Mock {@link AssetManagerFactory}.
	 */
	public static AssetManagerFactory mockAssetManagerFactory() {
		return new AssetManagerFactory(null, null, null);
	}

	/**
	 * Mock {@link ManagedObjectSource} for testing.
	 */
	@TestSource
	public static class MockConstructManagedObjectSource<O extends Enum<O>, F extends Enum<F>>
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
	public static class MockManagedObjectSourceMetaDataBuilder<O extends Enum<O>, F extends Enum<F>>
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
		private MockManagedObjectSourceMetaDataBuilder() {
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
		 * @param managedObjectClass
		 *            {@link ManagedObject} class.
		 */
		public void setManagedObjectClass(Class<? extends ManagedObject> managedObjectClass) {
			this.assertNotBuilt();
			this.managedObjectClass = managedObjectClass;
		}

		/**
		 * Specifies the {@link Object} class.
		 * 
		 * @param objectClass
		 *            {@link Object} class.
		 */
		public void setObjectClass(Class<?> objectClass) {
			this.assertNotBuilt();
			this.objectClass = objectClass;
		}

		/**
		 * Adds a {@link ManagedObjectDependencyMetaData}.
		 * 
		 * @param type
		 *            Dependency type.
		 * @param key
		 *            Key. May be <code>null</code>.
		 * @return {@link ManagedObjectDependencyMetaDataImpl} for further
		 *         configuration of the dependency.
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
		 * @param argumentType
		 *            Argument type to {@link Flow}. May be <code>null</code>.
		 * @param key
		 *            Key. May be <code>null</code>.
		 * @return {@link ManagedObjectFlowMetaDataImpl} for further
		 *         configuration of the {@link Flow}.
		 */
		public ManagedObjectFlowMetaDataImpl<F> addFlow(Class<?> argumentType, F key) {
			this.assertNotBuilt();
			ManagedObjectFlowMetaDataImpl<F> flow = new ManagedObjectFlowMetaDataImpl<F>(key, argumentType);
			this.flows.add(flow);
			return flow;
		}

		/**
		 * Adds an extension.
		 * 
		 * @param extensionType
		 *            Extension type.
		 * @param extensionFactory
		 *            {@link ExtensionFactory}.
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
		public ManagedObjectExtensionMetaData<?>[] getExtensionInterfacesMetaData() {
			this.isBuilt = true;
			return this.extensions.toArray(new ManagedObjectExtensionMetaData[this.extensions.size()]);
		}
	}

	/**
	 * Builder for the {@link RawManagingOfficeMetaData}.
	 */
	public static class MockRawManagingOfficeMetaDataBuilder<F extends Enum<F>> {

		/**
		 * Name of the managing {@link Office}.
		 */
		private final String managingOfficeName;

		/**
		 * {@link ManagingOfficeConfiguration}.
		 */
		private final ManagingOfficeBuilderImpl<F> managingOfficeConfiguration;

		/**
		 * {@link MockManagedObjectSourceMetaDataBuilder}.
		 */
		private final MockManagedObjectSourceMetaDataBuilder<?, F> managedObjectSourceMetDataBuilder;

		/**
		 * Recycle {@link ManagedFunction} name.
		 */
		private String recycleFunctionName = null;

		/**
		 * {@link InputManagedObjectConfiguration}.
		 */
		private DependencyMappingBuilderImpl<F> inputConfiguration = null;

		/**
		 * Built {@link RawManagedObjectMetaData}.
		 */
		private RawManagingOfficeMetaData<F> built = null;

		/**
		 * Instantiate.
		 * 
		 * @param managingOfficeName
		 *            Name of the managing {@link Office}.
		 * @param managedObjectSourceName
		 *            Name of the {@link ManagedObjectSource}.
		 */
		private MockRawManagingOfficeMetaDataBuilder(String managingOfficeName, String managedObjectSourceName) {
			this(managingOfficeName, managedObjectSourceName, new MockManagedObjectSourceMetaDataBuilder<>());
		}

		/**
		 * Instantiate.
		 * 
		 * @param managingOfficeName
		 *            Name of the managing {@link Office}.
		 * @param managedObjectSourceName
		 *            Name of the {@link ManagedObjectSource}.
		 * @param managedObjectSourceMetaDataBuilder
		 *            {@link MockManagedObjectSourceMetaDataBuilder}.
		 */
		private MockRawManagingOfficeMetaDataBuilder(String managingOfficeName, String managedObjectSourceName,
				MockManagedObjectSourceMetaDataBuilder<?, F> managedObjectSourceMetaDataBuilder) {
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
		 * @param recycleFunctionName
		 *            Recycle {@link ManagedFunction} name.
		 * @return <code>this</code>.
		 */
		public MockRawManagingOfficeMetaDataBuilder<F> recycle(String recycleFunctionName) {
			this.assetNotBuilt();
			this.recycleFunctionName = recycleFunctionName;
			return this;
		}

		/**
		 * Provides the {@link InputManagedObjectConfiguration}.
		 * 
		 * @param boundManagedObjectName
		 *            Name to bind the input {@link ManagedObject}.
		 * @return {@link DependencyMappingBuilder} to configure the
		 *         {@link InputManagedObjectConfiguration}.
		 */
		public DependencyMappingBuilder setInputManagedObject(String boundManagedObjectName) {
			this.assetNotBuilt();
			Assert.assertNull("Should only specify one input managed object", this.inputConfiguration);
			this.inputConfiguration = new DependencyMappingBuilderImpl<>(boundManagedObjectName);
			return this.inputConfiguration;
		}

		/**
		 * Obtains the {@link MockManagedObjectSourceMetaDataBuilder} to
		 * configure the {@link Flow} instances.
		 * 
		 * @return {@link MockManagedObjectSourceMetaDataBuilder} to configure
		 *         the {@link Flow} instances.
		 */
		public MockManagedObjectSourceMetaDataBuilder<?, F> getFlowBuilder() {
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
						this.inputConfiguration, this.managedObjectSourceMetDataBuilder.getFlowMetaData(),
						this.managingOfficeConfiguration);
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

		/**
		 * Obtains the {@link InputManagedObjectConfiguration}.
		 * 
		 * @return {@link InputManagedObjectConfiguration}.
		 */
		public InputManagedObjectConfiguration<?> getInputManagedObjectConfiguration() {
			return this.inputConfiguration;
		}
	}

	/**
	 * Mock builder for the {@link RawManagedObjectMetaData}.
	 */
	public static class MockRawManagedObjectMetaDataBuilder<O extends Enum<O>, F extends Enum<F>> {

		/**
		 * Name of the {@link ManagedObjectSource}.
		 */
		private final String managedObjectSourceName;

		/**
		 * {@link ManagedObjectConfiguration}.
		 */
		private final ManagedObjectBuilderImpl<O, F, MockConstructManagedObjectSource<O, F>> managedObjectConfiguration;

		/**
		 * {@link MockManagedObjectSourceMetaDataBuilder}.
		 */
		private final MockManagedObjectSourceMetaDataBuilder<O, F> managedObjectSourceMetaDataBuilder = new MockManagedObjectSourceMetaDataBuilder<>();

		/**
		 * Timeout for the {@link ManagedObjectSource}.
		 */
		private long timeout = 0;

		/**
		 * {@link ManagedObjectPool}.
		 */
		private ManagedObjectPool managedObjectPool = null;

		/**
		 * {@link ThreadCompletionListener} instances.
		 */
		private final List<ThreadCompletionListener> threadCompletionListeners = new LinkedList<>();

		/**
		 * Indicates if {@link ProcessAwareManagedObject}.
		 */
		private boolean isProcessAware = false;

		/**
		 * Indicates if {@link NameAwareManagedObject}.
		 */
		private boolean isNameAware = false;

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
		 * @param managedObjectSourceName
		 *            {@link ManagedObjectSource} name.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private MockRawManagedObjectMetaDataBuilder(String managedObjectSourceName) {
			this.managedObjectSourceName = managedObjectSourceName;
			this.managedObjectConfiguration = new ManagedObjectBuilderImpl(this.managedObjectSourceName,
					MockConstructManagedObjectSource.class);
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
		 * Obtains the {@link MockManagedObjectSourceMetaDataBuilder}.
		 * 
		 * @return {@link MockManagedObjectSourceMetaDataBuilder}.
		 */
		public MockManagedObjectSourceMetaDataBuilder<O, F> getMetaDataBuilder() {
			this.assetNotBuilt();
			return this.managedObjectSourceMetaDataBuilder;
		}

		/**
		 * Specifies the timeout.
		 * 
		 * @param timeout
		 *            Timeout.
		 * @return <code>this</code>.
		 */
		public MockRawManagedObjectMetaDataBuilder<O, F> timeout(long timeout) {
			this.assetNotBuilt();
			this.timeout = timeout;
			return this;
		}

		/**
		 * Specifies the {@link ManagedObjectPool}.
		 * 
		 * @param managedObjectPool
		 *            {@link ManagedObjectPool}.
		 * @return <code>this</code>.
		 */
		public MockRawManagedObjectMetaDataBuilder<O, F> pool(ManagedObjectPool managedObjectPool) {
			this.assetNotBuilt();
			this.managedObjectPool = managedObjectPool;
			return this;
		}

		/**
		 * Adds a {@link ThreadCompletionListener}.
		 * 
		 * @param listener
		 *            {@link ThreadCompletionListener}.
		 * @return <code>this</code>.
		 */
		public MockRawManagedObjectMetaDataBuilder<O, F> threadCompletionListener(ThreadCompletionListener listener) {
			this.assetNotBuilt();
			this.threadCompletionListeners.add(listener);
			return this;
		}

		/**
		 * Flags as {@link ProcessAwareManagedObject}.
		 * 
		 * @return <code>this<code>.
		 */
		public MockRawManagedObjectMetaDataBuilder<O, F> processAware() {
			this.assetNotBuilt();
			this.isProcessAware = true;
			return this;
		}

		/**
		 * Flags as {@link NameAwareManagedObject}.
		 * 
		 * @return <code>this<code>.
		 */
		public MockRawManagedObjectMetaDataBuilder<O, F> nameAware() {
			this.assetNotBuilt();
			this.isNameAware = true;
			return this;
		}

		/**
		 * Flags as {@link AsynchronousManagedObject}.
		 * 
		 * @return <code>this<code>.
		 */
		public MockRawManagedObjectMetaDataBuilder<O, F> asychronous() {
			this.assetNotBuilt();
			this.isAsynchronous = true;
			return this;
		}

		/**
		 * Flags as {@link CoordinatingManagedObject}.
		 * 
		 * @return <code>this<code>.
		 */
		public MockRawManagedObjectMetaDataBuilder<O, F> coordinating() {
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
					new MockConstructManagedObjectSource<O, F>(), this.managedObjectSourceMetaDataBuilder, this.timeout,
					this.managedObjectPool,
					this.threadCompletionListeners
							.toArray(new ThreadCompletionListener[this.threadCompletionListeners.size()]),
					this.managedObjectSourceMetaDataBuilder.objectClass, this.isProcessAware, this.isNameAware,
					this.isAsynchronous, this.isCoordinating, rawManagingOfficeMetaData);
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
		 * Obtains the {@link ManagedObjectSourceConfiguration}.
		 * 
		 * @return {@link ManagedObjectSourceConfiguration}.
		 */
		public ManagedObjectSourceConfiguration<F, MockConstructManagedObjectSource<O, F>> getManagedObjectConfiguratation() {
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
	public static class MockRawBoundManagedObjectMetaDataBuilder<O extends Enum<O>, F extends Enum<F>> {

		/**
		 * {@link MockRawManagedObjectMetaDataBuilder}.
		 */
		private final RawManagedObjectMetaData<O, F> rawManagedObjectMetaData;

		/**
		 * {@link MockRawManagedObjectMetaDataBuilder} to build the
		 * {@link RawManagedObjectMetaData}.
		 */
		private final MockRawManagedObjectMetaDataBuilder<O, F> rawManagedObjectMetaDataBuilder;

		/**
		 * {@link RawBoundManagedObjectMetaData}.
		 */
		private final RawBoundManagedObjectMetaData rawBoundManagedObjectMetaData;

		/**
		 * Listing of {@link MockRawBoundManagedObjectInstanceMetaDataBuilder}.
		 */
		private final List<MockRawBoundManagedObjectInstanceMetaDataBuilder<O, F>> instances = new LinkedList<>();

		/**
		 * {@link ManagedObjectScope}.
		 */
		private ManagedObjectScope managedOjectScope = ManagedObjectScope.PROCESS;

		/**
		 * Index of this {@link ManagedObject} within the
		 * {@link ManagedObjectScope}.
		 */
		private int indexOfManagedObjectWithinScope = 0;

		/**
		 * Indicates if built.
		 */
		private boolean isBuilt = false;

		/**
		 * Instantiate.
		 * 
		 * @param boundManagedObjectName
		 *            Bound {@link ManagedObject} name.
		 * @param isInput
		 *            Indicates if input.
		 * @param rawManagedObjectMetaData
		 *            {@link RawManagedObjectMetaData}.
		 */
		private MockRawBoundManagedObjectMetaDataBuilder(String boundManagedObjectName, boolean isInput,
				RawManagedObjectMetaData<O, F> rawManagedObjectMetaData) {
			this.rawBoundManagedObjectMetaData = new RawBoundManagedObjectMetaData(boundManagedObjectName, isInput);
			this.rawManagedObjectMetaData = rawManagedObjectMetaData;
			this.rawManagedObjectMetaDataBuilder = null;
		}

		/**
		 * Instantiate.
		 * 
		 * @param boundManagedObjectName
		 *            Bound {@link ManagedObject} name.
		 * @param isInput
		 *            Indicates if input.
		 * @param rawManagedObjectMetaData
		 *            {@link MockRawManagedObjectMetaDataBuilder}.
		 */
		private MockRawBoundManagedObjectMetaDataBuilder(String boundManagedObjectName, boolean isInput,
				MockRawManagedObjectMetaDataBuilder<O, F> rawManagedObjectMetaData) {
			this.rawBoundManagedObjectMetaData = new RawBoundManagedObjectMetaData(boundManagedObjectName, isInput);
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
		 * Specifies the {@link ManagedObjectIndex}.
		 * 
		 * @param managedObjectScope
		 *            {@link ManagedObjectScope}.
		 * @param indexOfManagedObjectWithinScope
		 *            Index of {@link ManagedObject} within the scope.
		 */
		public void setManagedObjectIndex(ManagedObjectScope managedObjectScope, int indexOfManagedObjectWithinScope) {
			this.assetNotBuilt();
			this.managedOjectScope = managedObjectScope;
			this.indexOfManagedObjectWithinScope = indexOfManagedObjectWithinScope;
		}

		/**
		 * Adds a {@link RawBoundManagedObjectInstanceMetaData}.
		 * 
		 * @return {@link MockRawBoundManagedObjectInstanceMetaDataBuilder}.
		 */
		public MockRawBoundManagedObjectInstanceMetaDataBuilder<O, F> addRawBoundManagedObjectInstanceMetaData() {
			this.assetNotBuilt();
			MockRawBoundManagedObjectInstanceMetaDataBuilder<O, F> instance = new MockRawBoundManagedObjectInstanceMetaDataBuilder<>(
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
				for (MockRawBoundManagedObjectInstanceMetaDataBuilder<O, F> instance : this.instances) {
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
	public static class MockRawBoundManagedObjectInstanceMetaDataBuilder<O extends Enum<O>, F extends Enum<F>> {

		/**
		 * {@link MockRawBoundManagedObjectMetaDataBuilder}.
		 */
		private final MockRawBoundManagedObjectMetaDataBuilder<O, F> rawBoundManagedObjectMetaData;

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
		 * @param rawBoundManagedObjectMetaData
		 *            {@link MockRawBoundManagedObjectMetaDataBuilder}.
		 */
		private MockRawBoundManagedObjectInstanceMetaDataBuilder(
				MockRawBoundManagedObjectMetaDataBuilder<O, F> rawBoundManagedObjectMetaData) {
			this.rawBoundManagedObjectMetaData = rawBoundManagedObjectMetaData;
			this.configuration = new DependencyMappingBuilderImpl<>(
					rawBoundManagedObjectMetaData.rawBoundManagedObjectMetaData.getBoundManagedObjectName());
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
				RawManagedObjectMetaData<O, F> rawManagedObjectMetaData = (this.rawBoundManagedObjectMetaData.rawManagedObjectMetaData != null)
						? this.rawBoundManagedObjectMetaData.rawManagedObjectMetaData
						: this.rawBoundManagedObjectMetaData.rawManagedObjectMetaDataBuilder.getBuilt();

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
	 * Builder to build {@link OfficeMetaData}.
	 */
	public static class MockOfficeMetaDataBuilder {

		/**
		 * Name of the {@link Office}.
		 */
		private final String officeName;

		/**
		 * {@link OfficeConfiguration}.
		 */
		private final OfficeBuilderImpl builder;

		/**
		 * {@link ManagedFunctionMetaData} instances.
		 */
		private final List<ManagedFunctionMetaData<?, ?>> functions = new LinkedList<>();

		/**
		 * Built {@link OfficeMetaData}.
		 */
		private OfficeMetaData built = null;

		/**
		 * Instantiate.
		 * 
		 * @param officeName
		 *            Name of the {@link Office}.
		 */
		private MockOfficeMetaDataBuilder(String officeName) {
			this.officeName = officeName;
			this.builder = new OfficeBuilderImpl(officeName);
		}

		/**
		 * Obtains the {@link OfficeBuilder}.
		 * 
		 * @return {@link OfficeBuilder}.
		 */
		public OfficeBuilder getBuilder() {
			return this.builder;
		}

		/**
		 * Adds a {@link ManagedFunction} to the {@link OfficeMetaData}.
		 * 
		 * @param functionName
		 *            Name of the {@link ManagedFunction}.
		 * @param parameterType
		 *            to the {@link ManagedFunction}.
		 */
		public void addManagedFunction(String functionName, Class<?> parameterType) {
			ManagedFunctionMetaData<?, ?> function = new ManagedFunctionMetaDataImpl<>(functionName, null, null,
					parameterType, null, null, null, null);
			this.functions.add(function);
		}

		/**
		 * Builds the {@link OfficeMetaData}.
		 * 
		 * @return {@link OfficeMetaData}.
		 */
		public OfficeMetaData build() {
			if (this.built == null) {

				// Load the managed functions
				List<ManagedFunctionMetaData<?, ?>> functions = new LinkedList<>();

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
							null, null));
				}

				// Load the convenience functions
				for (ManagedFunctionMetaData<?, ?> function : this.functions) {
					functions.add(function);
				}

				// Load the convenience functions
				ManagedFunctionLocator functionLocator = new ManagedFunctionLocatorImpl(
						functions.toArray(new ManagedFunctionMetaData[functions.size()]));
				this.built = new OfficeMetaDataImpl(this.officeName, null, null, null, null, null, null, null,
						functionLocator, null, null, null);
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