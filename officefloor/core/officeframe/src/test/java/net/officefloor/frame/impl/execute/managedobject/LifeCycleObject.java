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
package net.officefloor.frame.impl.execute.managedobject;

import org.junit.Assert;

import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.spi.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Life-cycle {@link ManagedObjectPool}.
 *
 * @author Daniel Sagenschneider
 */
public class LifeCycleObject implements ManagedObject {

	/*
	 * ================= Setup parameters ================
	 */

	/**
	 * Indicates failure in sourcing the {@link ManagedObject}.
	 */
	public Throwable sourceFailure = null;

	/**
	 * Indicates whether to provide a recycle {@link ManagedFunction}.
	 */
	public boolean isRecycleFunction = false;

	/**
	 * Flags whether to recycle the {@link ManagedObject}.
	 */
	public boolean isRecycle = true;

	/**
	 * Indicates failure in recycling the {@link ManagedObject}.
	 */
	public Throwable recycleFailure = null;

	/*
	 * =================== Results =========================
	 */

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
	public LifeCycleObject(String managedObjectName, AbstractOfficeConstructTestCase testCase) {
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
	public LifeCycleObject(String managedObjectName, AbstractOfficeConstructTestCase testCase, boolean isPool) {
		ManagedObjectBuilder<?> builder = testCase.constructManagedObject(managedObjectName,
				new LifeCycleManagedObjectSource());
		if (isPool) {
			builder.setManagedObjectPool(new LifeCyclePool());
		}
	}

	/*
	 * ====================== ManagedObject =============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return LifeCycleObject.this;
	}

	/**
	 * Life-cycle {@link ManagedObjectSource}.
	 */
	@TestSource
	public class LifeCycleManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		/*
		 * =================== ManagedObjectSource =========================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No configuration
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setManagedObjectClass(LifeCycleObject.class);
			context.setObjectClass(LifeCycleObject.class);

			// Provide the recycle function
			if (LifeCycleObject.this.isRecycleFunction) {
				context.getManagedObjectSourceContext().getRecycleFunction(new LifeCycleRecycle());
			}
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {

			// Indicate failure in sourcing the object
			if (LifeCycleObject.this.sourceFailure != null) {
				throw LifeCycleObject.this.sourceFailure;
			}

			// Return the object
			return LifeCycleObject.this;
		}
	}

	/**
	 * Recycles the {@link ManagedObject}.
	 */
	public class LifeCycleRecycle implements ManagedFunctionFactory<None, None>, ManagedFunction<None, None> {

		@Override
		public ManagedFunction<None, None> createManagedFunction() {
			return this;
		}

		@Override
		public Object execute(ManagedFunctionContext<None, None> context) throws Throwable {

			// Indicate failure in recylcing the object
			if (LifeCycleObject.this.recycleFailure != null) {
				throw LifeCycleObject.this.recycleFailure;
			}

			// Obtain the recycle parameter
			@SuppressWarnings("unchecked")
			RecycleManagedObjectParameter<LifeCycleObject> parameter = (RecycleManagedObjectParameter<LifeCycleObject>) context
					.getObject(0);
			if (LifeCycleObject.this.isRecycle) {
				LifeCycleObject managedObject = parameter.getManagedObject();
				parameter.reuseManagedObject(managedObject);
			}

			// No further functionality
			return null;
		}
	}

	/**
	 * Life-cycle {@link ManagedObjectPool}.
	 */
	private class LifeCyclePool implements ManagedObjectPool {

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
							LifeCycleObject.this.pooledSourcedManagedObject);
					Assert.assertNull("Should not have failure if providing managed object",
							LifeCycleObject.this.pooledSourceFailure);
					LifeCycleObject.this.pooledSourcedManagedObject = managedObject;
					user.setManagedObject(managedObject);
				}

				@Override
				public void setFailure(Throwable cause) {
					Assert.assertNull("Should not have failure if already failed",
							LifeCycleObject.this.pooledSourceFailure);
					Assert.assertNull("Should not have sourced the managed object if failing",
							LifeCycleObject.this.pooledSourcedManagedObject);
					LifeCycleObject.this.pooledSourceFailure = cause;
					user.setFailure(cause);
				}
			});
		}

		@Override
		public void returnManagedObject(ManagedObject managedObject) {
			Assert.assertNull("Should only return the managed object once",
					LifeCycleObject.this.pooledReturnedManagedObject);
			Assert.assertNull("Should not lose if returning the managed object",
					LifeCycleObject.this.pooledLostManagedObject);
			LifeCycleObject.this.pooledReturnedManagedObject = managedObject;
		}

		@Override
		public void lostManagedObject(ManagedObject managedObject, Throwable cause) {
			Assert.assertNull("Should only loose the managed object once",
					LifeCycleObject.this.pooledLostManagedObject);
			Assert.assertNull("Should not return if lost the managed object",
					LifeCycleObject.this.pooledReturnedManagedObject);
			LifeCycleObject.this.pooledLostManagedObject = managedObject;
			LifeCycleObject.this.pooledLostCause = cause;
		}
	}

}