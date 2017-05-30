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
/*
 * Created on Jan 10, 2006
 */
package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Source to obtain a particular type of {@link ManagedObject}.
 * <p>
 * Implemented by the {@link ManagedObject} provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSource<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	ManagedObjectSourceSpecification getSpecification();

	/**
	 * Called only once after the {@link ManagedObjectSource} is instantiated.
	 * 
	 * @param context
	 *            {@link ManagedObjectSourceContext} to use in initialising.
	 * @throws Exception
	 *             Should the {@link ManagedObjectSource} fail to configure
	 *             itself from the input properties.
	 */
	void init(ManagedObjectSourceContext<F> context) throws Exception;

	/**
	 * <p>
	 * Obtains the meta-data to describe this.
	 * <p>
	 * This is called after the {@link #init(ManagedObjectSourceContext)} method
	 * and therefore may use the configuration.
	 * <p>
	 * This should always return non-null. If there is a problem due to
	 * incorrect configuration, the {@link #init(ManagedObjectSourceContext)}
	 * should indicate this via an exception.
	 * 
	 * @return Meta-data to describe this.
	 */
	@Deprecated // make return of init
	ManagedObjectSourceMetaData<O, F> getMetaData();

	/**
	 * <p>
	 * Called once after {@link #init(ManagedObjectSourceContext)} to indicate
	 * this {@link ManagedObjectSource} should start execution.
	 * <p>
	 * On invocation of this method, {@link ProcessState} instances may be
	 * invoked via the {@link ManagedObjectExecuteContext}.
	 * 
	 * @param context
	 *            {@link ManagedObjectExecuteContext} to use in starting this
	 *            {@link ManagedObjectSource}.
	 * @throws Exception
	 *             Should the {@link ManagedObjectSource} fail to start
	 *             execution.
	 */
	void start(ManagedObjectExecuteContext<F> context) throws Exception;

	/**
	 * Sources a {@link ManagedObject} from this {@link ManagedObjectSource}.
	 * 
	 * @param user
	 *            {@link ManagedObjectUser} interested in using the
	 *            {@link ManagedObject}.
	 */
	void sourceManagedObject(ManagedObjectUser user);

	/**
	 * <p>
	 * Called to notify that the {@link OfficeFloor} is being closed.
	 * <p>
	 * On return from this method, no further {@link ProcessState} instances may
	 * be invoked.
	 */
	void stop();

}