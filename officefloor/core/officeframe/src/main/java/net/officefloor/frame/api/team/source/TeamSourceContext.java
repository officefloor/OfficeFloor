/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.api.team.source;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;

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
	 * Obtains the size of the {@link Team}.
	 * <p>
	 * Typically this is the maximum number of {@link Thread} instances for the
	 * {@link Team}. However, for some {@link Team} implementations it may not be
	 * used (e.g. {@link PassiveTeamSource}).
	 * <p>
	 * It is provided to allow the {@link Executive} to have some control over
	 * {@link Team} sizes.
	 * 
	 * @return {@link Team} size.
	 */
	int getTeamSize();

	/**
	 * Allows obtaining the size of the {@link Team}, without forcing it to be
	 * configured.
	 * 
	 * @param defaultSize Default size of the {@link Team}, if no size configured.
	 * @return {@link Team} size.
	 */
	int getTeamSize(int defaultSize);

	/**
	 * <p>
	 * Obtains the {@link ThreadFactory} for the {@link Team}.
	 * <p>
	 * It is encouraged for {@link Team} implementations to use this in creating
	 * {@link Thread} instances. This is to enable performance improvements by
	 * {@link OfficeFloor}, such as {@link ThreadLocal} {@link ManagedObjectPool}
	 * solutions to reduce pool locking overheads.
	 * 
	 * @return {@link ThreadFactory} for the {@link Team}.
	 */
	ThreadFactory getThreadFactory();

}
