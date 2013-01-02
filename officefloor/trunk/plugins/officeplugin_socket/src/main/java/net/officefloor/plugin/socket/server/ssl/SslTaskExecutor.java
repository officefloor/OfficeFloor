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
package net.officefloor.plugin.socket.server.ssl;

/**
 * Executor to execute the SSL task {@link Runnable} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface SslTaskExecutor {

	/**
	 * <p>
	 * Begins executing the SSL task.
	 * <p>
	 * As this method likely passes the {@link Runnable} to another
	 * {@link Thread} this method will generally return before the
	 * {@link Runnable} is complete.
	 *
	 * @param task
	 *            {@link Runnable} SSL task to complete.
	 */
	void beginTask(Runnable task);

}