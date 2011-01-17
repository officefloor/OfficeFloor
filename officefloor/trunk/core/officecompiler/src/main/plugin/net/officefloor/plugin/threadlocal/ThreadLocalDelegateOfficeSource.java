/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link ThreadLocal} {@link OfficeSource} implementation that utilises a
 * delegate {@link OfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadLocalDelegateOfficeSource extends AbstractOfficeSource {

	/**
	 * Property name to obtain the instance identifier of the delegate
	 * {@link OfficeSource}.
	 */
	public static final String PROPERTY_INSTANCE_IDENTIFIER = "instance.identifier";

	/**
	 * Delegate {@link OfficeSource}.
	 */
	private static final ThreadLocal<Delegates> threadLocalDelegates = new ThreadLocal<Delegates>();

	/**
	 * Unbinds the delegate {@link OfficeSource} instances.
	 */
	public static void unbindDelegates() {
		Delegates delegates = threadLocalDelegates.get();
		if (delegates != null) {
			delegates.sources.clear();
		}
	}

	/**
	 * Binds the delegate {@link OfficeSource} for the {@link Thread}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office}.
	 * @param delegate
	 *            Delegate {@link OfficeSource}.
	 * @param deployer
	 *            {@link OfficeFloorDeployer}.
	 * @return {@link DeployedOffice} for the bound {@link Office}.
	 */
	public static DeployedOffice bindDelegate(String officeName,
			OfficeSource delegate, OfficeFloorDeployer deployer) {

		// Ensure bind delegate to thread
		Delegates delegates = threadLocalDelegates.get();
		if (delegates == null) {
			delegates = new Delegates();
			threadLocalDelegates.set(delegates);
		}
		int identifier = delegates.bindDelegate(delegate);

		// Deploy the office to utilise delegate
		DeployedOffice office = deployer.addDeployedOffice(officeName,
				ThreadLocalDelegateOfficeSource.class.getName(), "");
		office.addProperty(PROPERTY_INSTANCE_IDENTIFIER, String
				.valueOf(identifier));

		// Return the office
		return office;
	}

	/*
	 * ===================== OfficeSource ================================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification as delegate already configured
	}

	@Override
	public void sourceOffice(OfficeArchitect officeArchitect,
			OfficeSourceContext context) throws Exception {

		// Ensure delegates available
		Delegates delegates = ThreadLocalDelegateOfficeSource.threadLocalDelegates
				.get();
		if (delegates == null) {
			throw new IllegalStateException("No "
					+ OfficeSource.class.getSimpleName() + " bound");
		}

		// Obtain the delegate
		int identifier = Integer.parseInt(context
				.getProperty(PROPERTY_INSTANCE_IDENTIFIER));
		OfficeSource delegate = delegates.getDelegate(identifier);

		// Delegate to source the office
		delegate.sourceOffice(officeArchitect, context);
	}

	/**
	 * Delegates for the {@link Thread}.
	 */
	private static class Delegates {

		/**
		 * Delegate {@link OfficeSource} instances.
		 */
		private final List<OfficeSource> sources = new LinkedList<OfficeSource>();

		/**
		 * Binds the delegate {@link OfficeSource} returning its identifier.
		 * 
		 * @param source
		 *            {@link OfficeSource} to bind.
		 * @return Identifier for the binding.
		 */
		public int bindDelegate(OfficeSource source) {
			int index = this.sources.size();
			this.sources.add(source);
			return index;
		}

		/**
		 * Obtains the delegate {@link OfficeSource}.
		 * 
		 * @param identifier
		 *            Identifier for the {@link OfficeSource}.
		 * @return {@link OfficeSource}.
		 */
		public OfficeSource getDelegate(int identifier) {
			return this.sources.get(identifier);
		}
	}

}