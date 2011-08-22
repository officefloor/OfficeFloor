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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.officefloor.source.RequiredProperties;
import net.officefloor.compile.spi.officefloor.source.impl.AbstractOfficeFloorSource;

/**
 * {@link ThreadLocal} {@link OfficeFloorSource} implementation that utilises a
 * delegate {@link OfficeFloorSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated
public class ThreadLocalDelegateOfficeFloorSource extends
		AbstractOfficeFloorSource {

	/**
	 * Property name to obtain the instance identifier of the delegate
	 * {@link OfficeFloorSource}.
	 */
	public static final String PROPERTY_INSTANCE_IDENTIFIER = "instance.identifier";

	/**
	 * Delegate {@link OfficeFloorSource}.
	 */
	private static final ThreadLocal<Delegates> threadLocalDelegates = new ThreadLocal<Delegates>();

	/**
	 * Unbinds the delegate {@link OfficeFloorSource} instances.
	 */
	public static void unbindDelegates() {
		Delegates delegates = threadLocalDelegates.get();
		if (delegates != null) {
			delegates.sources.clear();
		}
	}

	/**
	 * Binds the delegate {@link OfficeFloorSource} for the {@link Thread}.
	 * 
	 * @param delegate
	 *            Delegate {@link OfficeFloorSource}.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 */
	public static void bindDelegate(OfficeFloorSource delegate,
			OfficeFloorCompiler compiler) {

		// Ensure bind delegate to thread
		Delegates delegates = threadLocalDelegates.get();
		if (delegates == null) {
			delegates = new Delegates();
			threadLocalDelegates.set(delegates);
		}
		int identifier = delegates.bindDelegate(delegate);

		// Bind to compiler
		compiler
				.setOfficeFloorSourceClass(ThreadLocalDelegateOfficeFloorSource.class);
		compiler.addProperty(PROPERTY_INSTANCE_IDENTIFIER, String
				.valueOf(identifier));
	}

	/*
	 * ===================== OfficeFloorSource ================================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification as delegate already configured
	}

	@Override
	public void specifyConfigurationProperties(
			RequiredProperties requiredProperties,
			OfficeFloorSourceContext context) throws Exception {
		// No required properties as delegate already configured
	}

	@Override
	public void sourceOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception {

		// Ensure delegates available
		Delegates delegates = ThreadLocalDelegateOfficeFloorSource.threadLocalDelegates
				.get();
		if (delegates == null) {
			throw new IllegalStateException("No "
					+ OfficeFloorSource.class.getSimpleName() + " bound");
		}

		// Obtain the delegate
		int identifier = Integer.parseInt(context
				.getProperty(PROPERTY_INSTANCE_IDENTIFIER));
		OfficeFloorSource delegate = delegates.getDelegate(identifier);

		// Delegate to source the OfficeFloor
		delegate.sourceOfficeFloor(deployer, context);
	}

	/**
	 * Delegates for the {@link Thread}.
	 */
	private static class Delegates {

		/**
		 * Delegate {@link OfficeFloorSource} instances.
		 */
		private final List<OfficeFloorSource> sources = new LinkedList<OfficeFloorSource>();

		/**
		 * Binds the delegate {@link OfficeFloorSource} returning its
		 * identifier.
		 * 
		 * @param source
		 *            {@link OfficeFloorSource} to bind.
		 * @return Identifier for the binding.
		 */
		public int bindDelegate(OfficeFloorSource source) {
			int index = this.sources.size();
			this.sources.add(source);
			return index;
		}

		/**
		 * Obtains the delegate {@link OfficeFloorSource}.
		 * 
		 * @param identifier
		 *            Identifier for the {@link OfficeFloorSource}.
		 * @return {@link OfficeFloorSource}.
		 */
		public OfficeFloorSource getDelegate(int identifier) {
			return this.sources.get(identifier);
		}
	}

}