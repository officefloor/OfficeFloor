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
package net.officefloor.frame.util;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.escalate.SourceManagedObjectTimedOutEscalation;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.AsynchronousOperation;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.ProcessSafeOperation;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;

/**
 * Implementation of a {@link ManagedObjectUser} to source an object from a
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ManagedObjectUserStandAlone
		implements ManagedObjectUser, ProcessAwareContext, AsynchronousContext, ObjectRegistry {

	/**
	 * Object to synchronise sourcing the {@link ManagedObject}.
	 */
	private final Object lock = new Object();

	/**
	 * Sourced {@link ManagedObject}.
	 */
	private ManagedObject managedObject;

	/**
	 * Failure in sourcing the {@link ManagedObject}.
	 */
	private Throwable failure;

	/**
	 * Timeout in waiting to source the {@link ManagedObject}. Default of
	 * retrieve immediately.
	 */
	private long sourceTimeout = -1;

	/**
	 * {@link ProcessAwareContext}.
	 */
	private ProcessAwareContext processAwareContext = this;

	/**
	 * Bound name for a {@link NameAwareManagedObject}.
	 */
	private String boundManagedObjectName = "STAND_ALONE";

	/**
	 * {@link AsynchronousContext} for an {@link AsynchronousManagedObject}.
	 */
	private AsynchronousContext asynchronousListener = this;

	/**
	 * {@link ObjectRegistry} for a {@link CoordinatingManagedObject}.
	 */
	private ObjectRegistry objectRegistry = this;

	/**
	 * Dependencies for the {@link CoordinatingManagedObject}.
	 */
	private final Map<Integer, Object> dependencies = new HashMap<Integer, Object>();

	/**
	 * Specifies the timeout to source the {@link ManagedObject}.
	 * 
	 * @param sourceTimeout
	 *            Timeout to source the {@link ManagedObject}.
	 */
	public void setSourceTimeout(long sourceTimeout) {
		this.sourceTimeout = sourceTimeout;
	}

	/**
	 * Allows overriding the {@link ProcessAwareContext}. to initialise a
	 * {@link ProcessAwareManagedObject}.
	 * 
	 * @param processAwareContext
	 *            {@link ProcessAwareContext}.
	 */
	public void setProcessAwareContext(ProcessAwareContext processAwareContext) {
		this.processAwareContext = processAwareContext;
	}

	/**
	 * Specifies the bound name for a {@link NameAwareManagedObject}.
	 * 
	 * @param boundManagedObjectName
	 *            Bound name for a {@link NameAwareManagedObject}.
	 */
	public void setBoundManagedObjectName(String boundManagedObjectName) {
		this.boundManagedObjectName = boundManagedObjectName;
	}

	/**
	 * Allows overriding the {@link AsynchronousContext} to initialise an
	 * {@link AsynchronousManagedObject}.
	 * 
	 * @param listener
	 *            {@link AsynchronousContext}.
	 */
	public void setAsynchronousListener(AsynchronousContext listener) {
		this.asynchronousListener = listener;
	}

	/**
	 * Allows overriding the {@link ObjectRegistry} to initialise a
	 * {@link CoordinatingManagedObject}.
	 * 
	 * @param objectRegistry
	 *            {@link ObjectRegistry}.
	 */
	public void setObjectRegistry(ObjectRegistry<?> objectRegistry) {
		this.objectRegistry = objectRegistry;
	}

	/**
	 * Maps the dependency for the {@link CoordinatingManagedObject}.
	 * 
	 * @param index
	 *            Index of the dependency.
	 * @param dependency
	 *            Dependency.
	 */
	public void mapDependency(int index, Object dependency) {
		this.dependencies.put(new Integer(index), dependency);
	}

	/**
	 * Maps the dependency for the {@link CoordinatingManagedObject}.
	 * 
	 * @param key
	 *            Key of the dependency.
	 * @param dependency
	 *            Dependency.
	 */
	public void mapDependency(Enum<?> key, Object dependency) {
		this.mapDependency(key.ordinal(), dependency);
	}

	/**
	 * Sources the {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * 
	 * @param source
	 *            {@link ManagedObjectSource}.
	 * @return {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * @throws Throwable
	 *             If fails to source the {@link ManagedObject}.
	 */
	public ManagedObject sourceManagedObject(ManagedObjectSource<?, ?> source) throws Throwable {
		return sourceManagedObject(source, true);
	}

	/**
	 * Sources the {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * 
	 * @param source
	 *            {@link ManagedObjectSource}.
	 * @param isWait
	 *            Flags to wait for {@link ManagedObjectSource} to provide the
	 *            {@link ManagedObject}.
	 * @return {@link ManagedObject} from the {@link ManagedObjectSource} or
	 *         <code>null</code> if not waiting and not sourced immediately.
	 * @throws Throwable
	 *             If fails to source the {@link ManagedObject}.
	 */
	public synchronized ManagedObject sourceManagedObject(ManagedObjectSource<?, ?> source, boolean isWait)
			throws Throwable {

		// Method synchronised so can only load one at time.
		try {

			// Source the object
			long sourceStartTime = System.currentTimeMillis();
			source.sourceManagedObject(this);

			// Wait until the managed object is sourced
			ManagedObject mo = null;
			do {

				// Obtain managed object
				synchronized (this.lock) {

					// Propagate if failure
					if (this.failure != null) {
						throw this.failure;
					}

					// Obtain the managed object
					mo = this.managedObject;
				}

				// Determine if have managed object
				if ((mo == null) && (isWait)) {
					// Determine if timed out
					long currentTime = System.currentTimeMillis();
					if ((currentTime - sourceStartTime) > this.sourceTimeout) {
						// Taken too long to source
						throw new SourceManagedObjectTimedOutEscalation(Object.class);
					}

					// Not timed out, so wait some time for sourcing
					Thread.sleep(100);
				}

			} while ((mo == null) && (isWait));

			// Determine if have managed object
			if (mo == null) {
				return null; // no managed object sourced
			}

			// Have managed object so determine if process aware
			if (mo instanceof ProcessAwareManagedObject) {
				((ProcessAwareManagedObject) mo).setProcessAwareContext(this.processAwareContext);
			}

			// Have managed object so determine if name aware
			if (mo instanceof NameAwareManagedObject) {
				// Provide the bound name
				((NameAwareManagedObject) mo).setBoundManagedObjectName(this.boundManagedObjectName);
			}

			// Provide asynchronous listener
			if (mo instanceof AsynchronousManagedObject) {
				((AsynchronousManagedObject) mo).setAsynchronousContext(this.asynchronousListener);
			}

			// Coordinate if required
			if (mo instanceof CoordinatingManagedObject) {
				((CoordinatingManagedObject) mo).loadObjects(this.objectRegistry);
			}

			// Return the managed object
			return mo;

		} finally {
			// Reset
			this.managedObject = null;
			this.failure = null;
		}
	}

	/*
	 * ==================== ManagedObjectUser =============================
	 */

	@Override
	public void setManagedObject(ManagedObject managedObject) {
		synchronized (this.lock) {
			this.managedObject = managedObject;
		}
	}

	@Override
	public void setFailure(Throwable cause) {
		synchronized (this.lock) {
			this.failure = cause;
		}
	}

	/*
	 * ==================== ProcessAwareContext ==========================
	 */

	@Override
	public <R, T extends Throwable> R run(ProcessSafeOperation<R, T> operation) throws T {
		return operation.run();
	}

	/*
	 * ===================== AsynchronousContext =========================
	 */

	@Override
	public <T extends Throwable> void start(AsynchronousOperation<T> operation) {
		// Provide output to hint nothing happens
		System.out.println(this.getClass().getSimpleName() + ": start");
		try {
			operation.run();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public <T extends Throwable> void complete(AsynchronousOperation<T> operation) {
		// Provide output to hint nothing happens
		System.out.println(this.getClass().getSimpleName() + ": complete");
		try {
			operation.run();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * ===================== ObjectRegistry ===============================
	 */

	@Override
	public Object getObject(Enum key) {
		return this.getObject(key.ordinal());
	}

	@Override
	public Object getObject(int index) {

		// Obtain the dependency
		Object dependency = this.dependencies.get(new Integer(index));
		if (dependency == null) {
			throw new IllegalStateException("No dependency configured for index " + index);
		}

		// Return the dependency
		return dependency;
	}

}