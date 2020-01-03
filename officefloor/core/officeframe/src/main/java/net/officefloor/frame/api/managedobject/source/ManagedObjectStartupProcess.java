/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Start up {@link ProcessState} registered via a
 * {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectStartupProcess {

	/**
	 * <p>
	 * Flags for the {@link ProcessState} to be invoked concurrently.
	 * <p>
	 * The start up {@link ProcessState} is executed with on same {@link Thread}
	 * building the {@link OfficeFloor}. This allows for the start up
	 * {@link ProcessState} instances to complete before the {@link OfficeFloor} is
	 * opened (unless using another {@link Team}).
	 * <p>
	 * By flagging concurrent, it allows another {@link Thread} to concurrently
	 * invoke the start up {@link ProcessState}. This allows concurrent start up,
	 * albeit requiring concurrency handling due to using multiple {@link Thread}
	 * instances.
	 * <p>
	 * Furthermore, as order of execution of the start up {@link ProcessState}
	 * instances is based on the {@link OfficeFrame}, it allows any dependency
	 * ordering to be resolved. This is because they both can be executed
	 * concurrently and co-ordinate themselves together.
	 * 
	 * @param isConcurrent Flags whether to undertake the start up
	 *                     {@link ProcessState} concurrently.
	 */
	void setConcurrent(boolean isConcurrent);

}
