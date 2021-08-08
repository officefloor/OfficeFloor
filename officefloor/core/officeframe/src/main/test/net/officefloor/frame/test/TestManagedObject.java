/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.test;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Assert;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.impl.execute.service.SafeManagedObjectService;

/**
 * Test {@link ManagedObjectPool}.
 *
 * @author Daniel Sagenschneider
 */
public class TestManagedObject<O extends Enum<O>, F extends Enum<F>>
		implements ContextAwareManagedObject, AsynchronousManagedObject, CoordinatingManagedObject<O>, ManagedObject {

	/*
	 * ================= Setup parameters ================
	 */

	/**
	 * {@link ManagedObjectBuilder}.
	 */
	public final ManagedObjectBuilder<F> managedObjectBuilder;

	/**
	 * {@link ManagingOfficeBuilder}.
	 */
	public final ManagingOfficeBuilder<F> managingOfficeBuilder;

	/**
	 * Indicates if {@link ContextAwareManagedObject}.
	 */
	public boolean isContextAwareManagedObject = false;

	/**
	 * Indicates if {@link AsynchronousManagedObject}.
	 */
	public boolean isAsynchronousManagedObject = false;

	/**
	 * Indicates if {@link CoordinatingManagedObject}.
	 */
	public boolean isCoordinatingManagedObject = false;

	/**
	 * Optional {@link Consumer} to enhance the {@link ManagedObjectSourceMetaData}.
	 */
	public Consumer<MetaDataContext<O, F>> enhanceMetaData = null;

	/**
	 * Possible propagation failure in sourcing the {@link ManagedObject}.
	 */
	public RuntimeException sourcePropagateFailure = null;

	/**
	 * Indicates whether to delay sourcing the {@link ManagedObject}.
	 */
	public boolean isDelaySource = false;

	/**
	 * Possible failure in sourcing the {@link ManagedObject}.
	 */
	public Throwable sourceFailure = null;

	/**
	 * Possible failure in loading the {@link ManagedObjectContext}.
	 */
	public RuntimeException contextAwareFailure = null;

	/**
	 * Possible failure in registering the {@link AsynchronousContext}.
	 */
	public RuntimeException registerAsynchronousListenerFailure = null;

	/**
	 * Possible failure in loading the {@link ObjectRegistry}.
	 */
	public RuntimeException loadObjectsFailure = null;

	/**
	 * Indicates whether to provide a recycle {@link ManagedFunction}.
	 */
	public boolean isRecycleFunction = false;

	/**
	 * Flags whether to recycle the {@link ManagedObject}.
	 */
	public boolean isRecycle = true;

	/**
	 * {@link Consumer} to be provided the {@link RecycleManagedObjectParameter} on
	 * recycling.
	 */
	public Consumer<RecycleManagedObjectParameter<?>> recycleConsumer = null;

	/**
	 * Possible failure in recycling the {@link ManagedObject}.
	 */
	public Throwable recycleFailure = null;

	/*
	 * =================== Control =========================
	 */

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	public ManagedObjectExecuteContext<F> managedObjectExecuteContext;

	/**
	 * {@link ManagedObjectServiceContext}.
	 */
	public ManagedObjectServiceContext<F> managedObjectServiceContext;

	/**
	 * {@link ManagedObjectUser}.
	 */
	public volatile ManagedObjectUser managedObjectUser;

	/**
	 * {@link ManagedObjectContext}.
	 */
	public ManagedObjectContext managedObjectContext = null;

	/**
	 * {@link AsynchronousContext}.
	 */
	public AsynchronousContext asynchronousContext = null;

	/**
	 * {@link ObjectRegistry}.
	 */
	public ObjectRegistry<O> objectRegistry = null;

	/*
	 * =================== Results =========================
	 */

	/**
	 * {@link ManagedObject} provided to the recycle {@link ManagedFunction}.
	 */
	public ManagedObject recycledManagedObject = null;

	/**
	 * Sourced {@link ManagedObject} via {@link ManagedObjectPool}.
	 */
	public ManagedObject pooledSourcedManagedObject = null;

	/**
	 * Failure in sourcing {@link ManagedObject} via {@link ManagedObjectPool}.
	 */
	public Throwable pooledSourceFailure = null;

	/**
	 * Returned {@link ManagedObject} to the {@link ManagedObjectPool}.
	 */
	public ManagedObject pooledReturnedManagedObject = null;

	/**
	 * Lost {@link ManagedObject} to the {@link ManagedObjectPool}.
	 */
	public ManagedObject pooledLostManagedObject = null;

	/**
	 * Lost cause of {@link ManagedObject} for the {@link ManagedObjectPool}.
	 */
	public Throwable pooledLostCause = null;

	/**
	 * Indicates if the {@link ManagedObjectPool} has been emptied.
	 */
	public boolean poolEmptied = false;

	/**
	 * Instantiate and setup.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObject}.
	 * @param testCase          {@link AbstractOfficeConstructTestCase}.
	 */
	public TestManagedObject(String managedObjectName, AbstractOfficeConstructTestCase testCase) {
		this(managedObjectName, testCase, false);
	}

	/**
	 * Instantiate and setup.
	 * 
	 * @param managedObjectName Name for the {@link ManagedObject}.
	 * @param testCase          {@link AbstractOfficeConstructTestCase}.
	 * @param isPool            Indicates if pool the {@link ManagedObject}.
	 */
	public TestManagedObject(String managedObjectName, AbstractOfficeConstructTestCase testCase, boolean isPool) {
		this.managedObjectBuilder = (ManagedObjectBuilder<F>) testCase.constructManagedObject(managedObjectName,
				new TestManagedObjectSource(), null);
		this.managingOfficeBuilder = this.managedObjectBuilder.setManagingOffice(testCase.getOfficeName());
		if (isPool) {
			this.managedObjectBuilder
					.setManagedObjectPool((context) -> new TestManagedObjectPool(context.getManagedObjectSource()));
		}
	}

	/**
	 * Instantiate and setup.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObject}.
	 * @param construct         {@link ConstructTestSupport}.
	 */
	public TestManagedObject(String managedObjectName, ConstructTestSupport construct) {
		this(managedObjectName, construct, false);
	}

	/**
	 * Instantiate and setup.
	 * 
	 * @param managedObjectName Name for the {@link ManagedObject}.
	 * @param construct         {@link ConstructTestSupport}.
	 * @param isPool            Indicates if pool the {@link ManagedObject}.
	 */
	public TestManagedObject(String managedObjectName, ConstructTestSupport construct, boolean isPool) {
		this.managedObjectBuilder = (ManagedObjectBuilder<F>) construct.constructManagedObject(managedObjectName,
				new TestManagedObjectSource(), null);
		this.managingOfficeBuilder = this.managedObjectBuilder.setManagingOffice(construct.getOfficeName());
		if (isPool) {
			this.managedObjectBuilder
					.setManagedObjectPool((context) -> new TestManagedObjectPool(context.getManagedObjectSource()));
		}
	}

	/*
	 * ================== ContextAwareManagedObject ====================
	 */

	@Override
	public void setManagedObjectContext(ManagedObjectContext context) {
		this.managedObjectContext = context;

		// Propagate possible failure
		if (this.contextAwareFailure != null) {
			throw this.contextAwareFailure;
		}
	}

	/**
	 * ================== AsynchronousManagedObject ==================
	 */

	@Override
	public void setAsynchronousContext(AsynchronousContext listener) {
		this.asynchronousContext = listener;

		// Propagate possible failure
		if (this.registerAsynchronousListenerFailure != null) {
			throw this.registerAsynchronousListenerFailure;
		}
	}

	/**
	 * =================== CoordinatingManagedObject ==================
	 */

	@Override
	public void loadObjects(ObjectRegistry<O> registry) throws Throwable {
		this.objectRegistry = registry;

		// Propagate possible failure
		if (this.loadObjectsFailure != null) {
			throw this.loadObjectsFailure;
		}
	}

	/*
	 * ====================== ManagedObject ===========================
	 */

	@Override
	public Object getObject() throws Throwable {
		return TestManagedObject.this;
	}

	/**
	 * Test {@link ManagedObjectSource}.
	 */
	@TestSource
	public class TestManagedObjectSource extends AbstractAsyncManagedObjectSource<O, F> {

		/*
		 * =================== ManagedObjectSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No configuration
		}

		@Override
		protected void loadMetaData(MetaDataContext<O, F> context) throws Exception {

			// Create the managed object class
			List<Class<?>> interfaces = new ArrayList<>(4);
			interfaces.add(ManagedObject.class);
			if (TestManagedObject.this.isContextAwareManagedObject) {
				interfaces.add(ContextAwareManagedObject.class);
			}
			if (TestManagedObject.this.isAsynchronousManagedObject) {
				interfaces.add(AsynchronousManagedObject.class);
			}
			if (TestManagedObject.this.isCoordinatingManagedObject) {
				interfaces.add(CoordinatingManagedObject.class);
			}
			Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces.toArray(new Class[0]),
					new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							throw new IllegalStateException("Just created for class");
						}
					});

			@SuppressWarnings("unchecked")
			Class<? extends ManagedObject> managedObjectClass = (Class<? extends ManagedObject>) proxy.getClass();
			context.setManagedObjectClass(managedObjectClass);
			context.setObjectClass(TestManagedObject.this.getClass());

			// Provide the recycle function
			if (TestManagedObject.this.isRecycleFunction) {
				ManagedObjectFunctionBuilder<O, F> recycleBuilder = context.getManagedObjectSourceContext()
						.getRecycleFunction(new TestRecycle());
				recycleBuilder.linkParameter(0, RecycleManagedObjectParameter.class);
			}

			// Provide possible enhancing of the managed object
			if (TestManagedObject.this.enhanceMetaData != null) {
				TestManagedObject.this.enhanceMetaData.accept(context);
			}
		}

		@Override
		public void start(ManagedObjectExecuteContext<F> context) throws Exception {
			TestManagedObject.this.managedObjectExecuteContext = context;
			TestManagedObject.this.managedObjectServiceContext = new SafeManagedObjectService<>(context);
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {

			// Capture the user
			TestManagedObject.this.managedObjectUser = user;

			// Propagate the failure
			if (TestManagedObject.this.sourcePropagateFailure != null) {
				throw TestManagedObject.this.sourcePropagateFailure;
			}

			// Determine if delay sourcing
			if (TestManagedObject.this.isDelaySource) {
				return;
			}

			// Indicate failure in sourcing the object
			if (TestManagedObject.this.sourceFailure != null) {
				user.setFailure(TestManagedObject.this.sourceFailure);
			} else {
				// Return the object
				user.setManagedObject(TestManagedObject.this);
			}
		}
	}

	/**
	 * Recycles the {@link ManagedObject}.
	 */
	public class TestRecycle extends StaticManagedFunction<O, F> {

		@Override
		public void execute(ManagedFunctionContext<O, F> context) throws Throwable {

			// Obtain the recycle parameter and managed object
			RecycleManagedObjectParameter<TestManagedObject<O, F>> parameter = RecycleManagedObjectParameter
					.getRecycleManagedObjectParameter(context);
			TestManagedObject.this.recycledManagedObject = parameter.getManagedObject();

			// Consume parameter
			if (TestManagedObject.this.recycleConsumer != null) {
				TestManagedObject.this.recycleConsumer.accept(parameter);
			}

			// Indicate failure in recycling the object
			if (TestManagedObject.this.recycleFailure != null) {
				throw TestManagedObject.this.recycleFailure;
			}

			// Indicate whether to recycle the managed object
			if (TestManagedObject.this.isRecycle) {
				parameter.reuseManagedObject();
			}
		}
	}

	/**
	 * Test {@link ManagedObjectPool}.
	 */
	private class TestManagedObjectPool implements ManagedObjectPool {

		/**
		 * {@link ManagedObjectSource}.
		 */
		private ManagedObjectSource<?, ?> managedObjectSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectSource {@link ManagedObjectSource}.
		 */
		public TestManagedObjectPool(ManagedObjectSource<?, ?> managedObjectSource) {
			this.managedObjectSource = managedObjectSource;
		}

		/*
		 * ================ ManagedObjectPool ===================
		 */

		@Override
		public void sourceManagedObject(final ManagedObjectUser user) {
			this.managedObjectSource.sourceManagedObject(new ManagedObjectUser() {

				@Override
				public void setManagedObject(ManagedObject managedObject) {
					Assert.assertNull("Should not have already sourced the managed object",
							TestManagedObject.this.pooledSourcedManagedObject);
					Assert.assertNull("Should not have failure if providing managed object",
							TestManagedObject.this.pooledSourceFailure);
					TestManagedObject.this.pooledSourcedManagedObject = managedObject;
					user.setManagedObject(managedObject);
				}

				@Override
				public void setFailure(Throwable cause) {
					Assert.assertNull("Should not have failure if already failed",
							TestManagedObject.this.pooledSourceFailure);
					Assert.assertNull("Should not have sourced the managed object if failing",
							TestManagedObject.this.pooledSourcedManagedObject);
					TestManagedObject.this.pooledSourceFailure = cause;
					user.setFailure(cause);
				}
			});
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			Assert.assertNull("Should only return the managed object once",
					TestManagedObject.this.pooledReturnedManagedObject);
			Assert.assertNull("Should not lose if returning the managed object",
					TestManagedObject.this.pooledLostManagedObject);
			TestManagedObject.this.pooledReturnedManagedObject = managedObject;
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			Assert.assertNull("Should only loose the managed object once",
					TestManagedObject.this.pooledLostManagedObject);
			Assert.assertNull("Should not return if lost the managed object",
					TestManagedObject.this.pooledReturnedManagedObject);
			TestManagedObject.this.pooledLostManagedObject = managedObject;
			TestManagedObject.this.pooledLostCause = cause;
		}

		@Override
		public void empty() {
			assertFalse("Should only empty pool once", TestManagedObject.this.poolEmptied);
			TestManagedObject.this.poolEmptied = true;
		}
	}

}
