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
package net.officefloor.frame.internal.construct;

import java.util.function.Consumer;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.internal.configuration.TeamConfiguration;
import net.officefloor.frame.internal.structure.ManagedExecutionFactory;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * Factory for the construction of {@link RawTeamMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawTeamMetaDataFactory {

	/**
	 * Constructs the {@link RawTeamMetaData}.
	 * 
	 * @param <TS>
	 *            {@link TeamSource} type.
	 * @param configuration
	 *            {@link TeamConfiguration}.
	 * @param threadDecorator
	 *            Decorator for the created {@link Thread} instances. May be
	 *            <code>null</code>.
	 * @param sourceContext
	 *            {@link SourceContext}.
	 * @param threadLocalAwareExecutor
	 *            {@link ThreadLocalAwareExecutor}.
	 * @param managedExecutionFactory
	 *            {@link ManagedExecutionFactory}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link RawTeamMetaData} or <code>null</code> if fails to
	 *         construct.
	 */
	<TS extends TeamSource> RawTeamMetaData constructRawTeamMetaData(TeamConfiguration<TS> configuration,
			SourceContext sourceContext, Consumer<Thread> threadDecorator,
			ThreadLocalAwareExecutor threadLocalAwareExecutor, ManagedExecutionFactory managedExecutionFactory,
			OfficeFloorIssues issues);

}