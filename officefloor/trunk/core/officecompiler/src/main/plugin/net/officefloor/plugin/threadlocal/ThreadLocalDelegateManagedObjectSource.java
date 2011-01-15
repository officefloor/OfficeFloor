/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.threadlocal;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

/**
 * {@link ThreadLocal} {@link ManagedObjectSource} implementation that utilises
 * a delegate {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ThreadLocalDelegateManagedObjectSource implements
		ManagedObjectSource {

	/**
	 * Property name to obtain the instance identifier of the delegate
	 * {@link ManagedObjectSource}.
	 */
	public static final String PROPERTY_INSTANCE_IDENTIFIER = "instance.identifier";

	/**
	 * Delegate {@link ManagedObjectSource}.
	 */
	private static final ThreadLocal<Delegates> threadLocalDelegates = new ThreadLocal<Delegates>();

	/**
	 * Unbinds the delegate {@link ManagedObjectSource} instances.
	 */
	public static void unbindDelegates() {
		Delegates delegates = threadLocalDelegates.get();
		if (delegates != null) {
			delegates.sources.clear();
		}
	}

	/**
	 * Binds the delegate {@link ManagedObjectSource} for the {@link Thread}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 * @param delegate
	 *            Delegate {@link ManagedObjectSource}.
	 * @param deployer
	 *            {@link OfficeFloorDeployer}.
	 * @return {@link OfficeFloorManagedObjectSource} for the bound
	 *         {@link ManagedObjectSource}.
	 */
	public static OfficeFloorManagedObjectSource bindDelegate(
			String managedObjectSourceName, ManagedObjectSource<?, ?> delegate,
			OfficeFloorDeployer deployer) {

		// Ensure bind delegate to thread
		Delegates delegates = threadLocalDelegates.get();
		if (delegates == null) {
			delegates = new Delegates();
			threadLocalDelegates.set(delegates);
		}
		int identifier = delegates.bindDelegate(delegate);

		// Deploy the managed object source to utilise delegate
		OfficeFloorManagedObjectSource source = deployer
				.addManagedObjectSource(managedObjectSourceName,
						ThreadLocalDelegateManagedObjectSource.class.getName());
		source.addProperty(PROPERTY_INSTANCE_IDENTIFIER,
				String.valueOf(identifier));

		// Return the managed object source
		return source;
	}

	/**
	 * Delegate {@link ManagedObjectSource}.
	 */
	private ManagedObjectSource<?, ?> delegate;

	/*
	 * ================== ManagedObjectSource =============================
	 */

	@Override
	public ManagedObjectSourceSpecification getSpecification() {
		return new ManagedObjectSourceSpecification() {
			@Override
			public ManagedObjectSourceProperty[] getProperties() {
				return new ManagedObjectSourceProperty[0];
			}
		};
	}

	@Override
	public void init(ManagedObjectSourceContext context) throws Exception {

		// Ensure delegates available
		Delegates delegates = ThreadLocalDelegateManagedObjectSource.threadLocalDelegates
				.get();
		if (delegates == null) {
			throw new IllegalStateException("No "
					+ ManagedObjectSource.class.getSimpleName() + " bound");
		}

		// Obtain the delegate
		int identifier = Integer.parseInt(context
				.getProperty(PROPERTY_INSTANCE_IDENTIFIER));
		this.delegate = delegates.getDelegate(identifier);

		// Allow delegate to specify meta-data
		this.delegate.init(context);
	}

	@Override
	public ManagedObjectSourceMetaData getMetaData() {
		return this.delegate.getMetaData();
	}

	@Override
	public void start(ManagedObjectExecuteContext context) throws Exception {
		this.delegate.start(context);
	}

	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		this.delegate.sourceManagedObject(user);
	}

	@Override
	public void stop() {
		this.delegate.stop();
	}

	/**
	 * Delegates for the {@link Thread}.
	 */
	private static class Delegates {

		/**
		 * Delegate {@link ManagedObjectSource} instances.
		 */
		private final List<ManagedObjectSource<?, ?>> sources = new LinkedList<ManagedObjectSource<?, ?>>();

		/**
		 * Binds the delegate {@link ManagedObjectSource} returning its
		 * identifier.
		 * 
		 * @param source
		 *            {@link ManagedObjectSource} to bind.
		 * @return Identifier for the binding.
		 */
		public int bindDelegate(ManagedObjectSource<?, ?> source) {
			int index = this.sources.size();
			this.sources.add(source);
			return index;
		}

		/**
		 * Obtains the delegate {@link ManagedObjectSource}.
		 * 
		 * @param identifier
		 *            Identifier for the {@link ManagedObjectSource}.
		 * @return {@link ManagedObjectSource}.
		 */
		public ManagedObjectSource<?, ?> getDelegate(int identifier) {
			return this.sources.get(identifier);
		}
	}

}