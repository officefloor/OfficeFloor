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

package net.officefloor.frame.internal.configuration;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.officefloor.frame.api.build.OfficeVisitor;
import net.officefloor.frame.api.clock.ClockFactory;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSourceContext;

/**
 * Configuration for an {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorConfiguration {

	/**
	 * Obtains the name of the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	String getOfficeFloorName();

	/**
	 * Obtains the maximum time in milliseconds to wait for {@link OfficeFloor} to
	 * start.
	 * 
	 * @return Maximum time in milliseconds to wait for {@link OfficeFloor} to
	 *         start.
	 */
	long getMaxStartupWaitTime();

	/**
	 * Obtains the profiles.
	 * 
	 * @return Profiles.
	 */
	String[] getProfiles();

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @param sourceName           Name of source.
	 * @param clockFactoryProvider Provides {@link ClockFactory} if one not
	 *                             configured.
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext(String sourceName, Supplier<ClockFactory> clockFactoryProvider);

	/**
	 * Obtains the decorator of {@link Thread} instances created by the
	 * {@link TeamSourceContext}.
	 * 
	 * @return Decorator of {@link Thread} instances created by the
	 *         {@link TeamSourceContext}. May be <code>null</code>.
	 */
	Consumer<Thread> getThreadDecorator();

	/**
	 * Obtains the configuration of the {@link ManagedObjectSource} instances.
	 * 
	 * @return {@link ManagedObjectSource} configuration.
	 */
	ManagedObjectSourceConfiguration<?, ?>[] getManagedObjectSourceConfiguration();

	/**
	 * Obtains the configuration of the {@link Team} instances on the
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link TeamConfiguration} instances.
	 */
	TeamConfiguration<?>[] getTeamConfiguration();

	/**
	 * Obtains the configuration of the {@link Executive} for the
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link ExecutiveConfiguration}.
	 */
	ExecutiveConfiguration<?> getExecutiveConfiguration();

	/**
	 * Obtains the configuration of the {@link Office} instances on the
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeConfiguration} instances.
	 */
	OfficeConfiguration[] getOfficeConfiguration();

	/**
	 * Obtains the {@link OfficeVisitor} instances.
	 * 
	 * @return {@link OfficeVisitor} instances.
	 */
	OfficeVisitor[] getOfficeVisitors();

	/**
	 * Obtains the {@link EscalationHandler} for issues escalating out of the
	 * {@link Office} instances.
	 * 
	 * @return {@link EscalationHandler} for issues escalating out of the
	 *         {@link Office} instances. May be <code>null</code>.
	 */
	EscalationHandler getEscalationHandler();

}
