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
package net.officefloor.frame.test;

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
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionBuilder;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.source.TestSource;

/**
 * Test {@link ManagedObjectPool}.
 *
 * @author Daniel Sagenschneider
 */
public class TestManagedObject<O extends Enum<O>, F extends Enum<F>> implements NameAwareManagedObject,
		AsynchronousManagedObject, CoordinatingManagedObject<O>, ProcessAwareManagedObject, ManagedObject {

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
	 * Indicates if {@link NameAwareManagedObject}.
	 */
	public boolean isNameAwareManagedObject = false;

	/**
	 * Indicates if {@link AsynchronousManagedObject}.
	 */
	public boolean isAsynchronousManagedObject = false;

	/**
	 * Indicates if {@link CoordinatingManagedObject}.
	 */
	public boolean isCoordinatingManagedObject = false;

	/**
	 * Indicates if {@link ProcessAwareManagedObject}.
	 */
	public boolean isProcessAwareManagedObject = false;

	/**
	 * Optional {@link Consumer} to enhance the
	 * {@link ManagedObjectSourceMetaData}.
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
	 * Possible failure in binding the {@link NameAwareManagedObject} name.
	 */
	public RuntimeException bindNameAwareFailure = null;

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
	 * {@link Consumer} to be provided the {@link RecycleManagedObjectParameter}
	 * on recycling.
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
	 * {@link ManagedObjectUser}.
	 */
	public ManagedObjectUser managedObjectUser;

	/**
	 * {@link NameAwareManagedObject} bound name.
	 */
	public String boundManagedObjectName = null;

	/**
	 * {@link AsynchronousContext}.
	 */
	public AsynchronousContext asynchronousContext = null;

	/**
	 * {@link ObjectRegistry}.
	 */
	public ObjectRegistry<O> objectRegistry = null;

	/**
	 * {@link ProcessAwareContext}.
	 */
	public ProcessAwareContext processAwareContext = null;

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
	 * Instantiate and setup.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param testCase
	 *            {@link AbstractOfficeConstructTestCase}.
	 */
	public TestManagedObject(String managedObjectName, AbstractOfficeConstructTestCase testCase) {
		this(managedObjectName, testCase, false);
	}

	/**
	 * Instantiate and setup.
	 * 
	 * @param managedObjectName
	 *            Name for the {@link ManagedObject}.
	 * @param testCase
	 *            {@link AbstractOfficeConstructTestCase}.
	 * @param isPool
	 *            Indicates if pool the {@link ManagedObject}.
	 */
	public TestManagedObject(String managedObjectName, AbstractOfficeConstructTestCase testCase, boolean isPool) {
		this.managedObjectBuilder = (ManagedObjectBuilder<F>) testCase.constructManagedObject(managedObjectName,
				new TestManagedObjectSource(), null);
		this.managingOfficeBuilder = this.managedObjectBuilder.setManagingOffice(testCase.getOfficeName());
		if (isPool) {
			this.managedObjectBuilder.setManagedObjectPool(new TestManagedObjectPool());
		}
	}

	/**
	 * ================== NameAwareManagedObject =====================
	 */

	@Override
	public void setBoundManagedObjectName(String boundManagedObjectName) {
		this.boundManagedObjectName = boundManagedObjectName;

		// Propagate possible failure
		if (this.bindNameAwareFailure != null) {
			throw this.bindNameAwareFailure;
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
	 * ================== ProcessAwareManagedObject ====================
	 */

	@Override
	public void setProcessAwareContext(ProcessAwareContext context) {
		this.processAwareContext = context;
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
			if (TestManagedObject.this.isNameAwareManagedObject) {
				interfaces.add(NameAwareManagedObject.class);
			}
			if (TestManagedObject.this.isAsynchronousManagedObject) {
				interfaces.add(AsynchronousManagedObject.class);
			}
			if (TestManagedObject.this.isCoordinatingManagedObject) {
				interfaces.add(CoordinatingManagedObject.class);
			}
			if (TestManagedObject.this.isProcessAwareManagedObject) {
				interfaces.add(ProcessAwareManagedObject.class);
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
	public class TestRecycle implements ManagedFunctionFactory<O, F>, ManagedFunction<O, F> {

		@Override
		public ManagedFunction<O, F> createManagedFunction() {
			return this;
		}

		@Override
		public Object execute(ManagedFunctionContext<O, F> context) throws Throwable {

			// Obtain the recycle parameter and managed object
			@SuppressWarnings("unchecked")
			RecycleManagedObjectParameter<TestManagedObject<O, F>> parameter = (RecycleManagedObjectParameter<TestManagedObject<O, F>>) context
					.getObject(0);
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
				TestManagedObject<O, F> managedObject = parameter.getManagedObject();
				parameter.reuseManagedObject(managedObject);
			}

			// No further functionality
			return null;
		}
	}

	/**
	 * Test {@link ManagedObjectPool}.
	 */
	private class TestManagedObjectPool implements ManagedObjectPool {

		/**
		 * {@link ManagedObjectPoolContext}.
		 */
		private ManagedObjectPoolContext context;

		@Override
		public void init(ManagedObjectPoolContext context) throws Exception {
			this.context = context;
		}

		@Override
		public void sourceManagedObject(final ManagedObjectUser user) {
			this.context.getManagedObjectSource().sourceManagedObject(new ManagedObjectUser() {

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
	}

}