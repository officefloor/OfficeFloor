/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.team.source;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;

/**
 * Context for the {@link TeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamSourceContext extends SourceContext {

	/**
	 * <p>
	 * Obtains the name of the {@link Team} to be created from the
	 * {@link TeamSource}.
	 * <p>
	 * This enables naming the {@link Thread} instances for the {@link Team} to be
	 * specific to the {@link Team}.
	 * 
	 * @return Name of the {@link Team} to be created from the {@link TeamSource}.
	 */
	String getTeamName();

	/**
	 * <p>
	 * Obtains the {@link ThreadFactory} for the {@link Team}.
	 * <p>
	 * It is encouraged for {@link Team} implementations to use this in creating
	 * {@link Thread} instances. This is to enable performance improvements by
	 * {@link OfficeFloor}, such as {@link ThreadLocal} {@link ManagedObjectPool}
	 * solutions to reduce pool locking overheads.
	 * 
	 * @param threadPriority Priority for the created {@link Thread} instances.
	 * @return {@link ThreadFactory} for the {@link Team}.
	 */
	@Deprecated // remove threadPriority parameter (Runnable wrapper achieves this plus other)
	ThreadFactory getThreadFactory(int threadPriority);

}