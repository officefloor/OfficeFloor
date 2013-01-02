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
package net.officefloor.frame.api.manage;

import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Future token to indicate a {@link ProcessState} has completed.
 * <p>
 * On completion of the {@link ProcessState} a <code>{@link #notifyAll()}</code>
 * will also be invoked on the object implementing this interface to allow
 * &quot;immediate&quot; awareness of the completion of the {@link ProcessState}.
 * <p>
 * The reason for the <code>{@link #notifyAll()}</code> is to allow blocking
 * {@link Thread} instances waiting on the {@link ProcessState} to be complete
 * to be notified rather than having to poll the {@link #isComplete()} method.
 * <p>
 * Please note that <code>{@link #notifyAll()}</code> is only run once on
 * completion of the {@link ProcessState} and blocking {@link Thread} instances
 * should not wait indefinitely for this to occur.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessFuture {

	/**
	 * Indicates whether the {@link JobSequence} has completed.
	 * 
	 * @return <code>true</code> if the {@link JobSequence} has completed.
	 */
	boolean isComplete();

}