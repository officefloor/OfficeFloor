/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.compile.executive;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.TeamOversight;

/**
 * <code>Type definition</code> of an {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveType {

	/**
	 * Obtains the {@link ExecutionStrategyType} definitions for the
	 * {@link ExecutionStrategy} instances available from the {@link Executive}.
	 * 
	 * @return {@link ExecutionStrategyType} definitions for the
	 *         {@link ExecutionStrategy} instances available from the
	 *         {@link Executive}.
	 */
	ExecutionStrategyType[] getExecutionStrategyTypes();

	/**
	 * Obtains the {@link TeamOversightType} definitions for the
	 * {@link TeamOversight} instances available from the {@link Executive}.
	 * 
	 * @return {@link TeamOversightType} definitions for the {@link TeamOversight}
	 *         instances available from the {@link Executive}.
	 */
	TeamOversightType[] getTeamOversightTypes();
}