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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.team.Team;

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
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

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
	 * Obtains the configuration of the {@link Office} instances on the
	 * {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeConfiguration} instances.
	 */
	OfficeConfiguration[] getOfficeConfiguration();

	/**
	 * Obtains the {@link EscalationHandler} for issues escalating out of the
	 * {@link Office} instances.
	 * 
	 * @return {@link EscalationHandler} for issues escalating out of the
	 *         {@link Office} instances. May be <code>null</code>.
	 */
	EscalationHandler getEscalationHandler();

}