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
package net.officefloor.frame.api.executive;

import java.util.concurrent.ThreadFactory;

/**
 * Strategy of execution.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionStrategy {

	/**
	 * Obtains the name of the {@link ExecutionStrategy}.
	 * 
	 * @return Name of the {@link ExecutionStrategy}.
	 */
	String getExecutionStrategyName();

	/**
	 * Obtains the {@link ThreadFactory} instances for the various {@link Thread}
	 * instances of this {@link ExecutionStrategy}.
	 * 
	 * @return {@link ThreadFactory} instances.
	 */
	ThreadFactory[] getThreadFactories();

}