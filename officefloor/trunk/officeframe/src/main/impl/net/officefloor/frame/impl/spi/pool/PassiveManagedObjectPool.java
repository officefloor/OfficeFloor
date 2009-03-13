/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.spi.pool;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.spi.managedobject.pool.ManagedObjectPoolContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

/**
 * {@link net.officefloor.frame.spi.managedobject.pool.ManagedObjectPool} for passive
 * management of {@link net.officefloor.frame.spi.managedobject.ManagedObject}
 * instances.
 * 
 * @author Daniel
 */
public class PassiveManagedObjectPool implements ManagedObjectPool {

	/**
	 * Pool of {@link ManagedObject} instances.
	 */
	private final List<ManagedObject> pool = new LinkedList<ManagedObject>();

	/**
	 * Listing of {@link ManagedObjectUser} instances waiting for a
	 * {@link ManagedObject}.
	 */
	private final List<ManagedObjectUser> waitingUsers = new LinkedList<ManagedObjectUser>();

	/**
	 * Maximum {@link ManagedObject} instances within pool.
	 */
	private final int max;

	/**
	 * Available number of {@link ManagedObject} instances.
	 */
	private int available = 0;

	/**
	 * {@link ManagedObjectPoolContext} for this {@link ManagedObjectPool}.
	 */
	private ManagedObjectPoolContext context;

	/**
	 * Initiate.
	 * 
	 * @param max
	 *            Maximum {@link ManagedObject} instances within pool.
	 */
	public PassiveManagedObjectPool(int max) {
		this.max = max;
	}

	/*
	 * ====================================================================
	 * ManagedObjectPool
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.pool.ManagedObjectPool#init(net.officefloor.frame.spi.pool.ManagedObjectPoolContext)
	 */
	public void init(ManagedObjectPoolContext context) throws Exception {
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectPool#sourceManagedObject(net.officefloor.frame.spi.managedobject.source.ManagedObjectUser)
	 */
	public void sourceManagedObject(ManagedObjectUser user) {
		synchronized (this.pool) {

			// If others waiting be polite and get in line
			if (this.waitingUsers.size() > 0) {
				this.waitingUsers.add(user);
				return;
			}

			// Determine if pooled Managed Object is available
			if (this.pool.size() > 0) {
				// Available from pool
				user.setManagedObject(this.pool.remove(0));

			} else if (this.available < this.max) {
				// Increment number of available managed objects
				// (Assume successful unless otherwise)
				this.available++;

				// Create a new Managed Object
				// (Wrapper will decrement available if not created)
				this.context.getManagedObjectSource().sourceManagedObject(
						new ManagedObjectUserWrapper(user));

			} else {
				// Add to listing waiting on a Managed Object
				this.waitingUsers.add(user);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectPool#returnManagedObject(net.officefloor.frame.spi.managedobject.ManagedObject)
	 */
	public void returnManagedObject(ManagedObject managedObject) {
		synchronized (this.pool) {
			
			// Determine if waiting user
			if (this.waitingUsers.size() > 0) {
				// Provide to first waiting user
				this.waitingUsers.remove(0).setManagedObject(managedObject);
			}
			
			// No waiting users, therefore pool
			this.pool.add(managedObject);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.internal.structure.ManagedObjectPool#lostManagedObject(net.officefloor.frame.spi.managedobject.ManagedObject)
	 */
	public void lostManagedObject(ManagedObject managedObject) {
		// Decrement number of available Managed Objects
		synchronized (this.pool) {
			this.available--;
		}
	}

	/**
	 * Wrapper around the {@link ManagedObjectUser} to interact with this
	 * {@link ManagedObjectPool}.
	 */
	private class ManagedObjectUserWrapper implements ManagedObjectUser {

		/**
		 * {@link ManagedObjectUser} delegate.
		 */
		private final ManagedObjectUser delegate;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate {@link ManagedObjectUser}.
		 */
		public ManagedObjectUserWrapper(ManagedObjectUser delegate) {
			this.delegate = delegate;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectUser#setManagedObject(net.officefloor.frame.spi.managedobject.ManagedObject)
		 */
		public void setManagedObject(ManagedObject managedObject) {
			// Delegate
			this.delegate.setManagedObject(managedObject);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see net.officefloor.frame.spi.managedobject.source.ManagedObjectUser#setFailure(java.lang.Throwable)
		 */
		public void setFailure(Throwable cause) {
			// Indicate not available
			synchronized (PassiveManagedObjectPool.this.pool) {
				PassiveManagedObjectPool.this.available--;
			}
			
			// Delegate
			this.delegate.setFailure(cause);
		}
	}

}
