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
package net.officefloor.compile.impl.structure;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.internal.structure.OptionalThreadLocalReceiver;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.thread.OptionalThreadLocal;

/**
 * Links the {@link OptionalThreadLocal} to the
 * {@link OptionalThreadLocalReceiver} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class OptionalThreadLocalLinker {

	/**
	 * Listing of {@link OptionalThreadLocalReceiver} instances interested in the
	 * {@link OptionalThreadLocal}.
	 */
	private final List<OptionalThreadLocalReceiver> receivers = new LinkedList<>();

	/**
	 * {@link ThreadDependencyMappingBuilder}.
	 */
	private ThreadDependencyMappingBuilder dependencyMapper = null;

	/**
	 * {@link OptionalThreadLocal}.
	 */
	private OptionalThreadLocal<?> optionalThreadLocal = null;

	/**
	 * Specifies the {@link OptionalThreadLocal}.
	 * 
	 * @param dependencyMapper {@link ThreadDependencyMappingBuilder} to obtain the
	 *                         {@link OptionalThreadLocal} if required.
	 */
	public void setThreadDependencyMappingBuilder(ThreadDependencyMappingBuilder dependencyMapper) {
		this.dependencyMapper = dependencyMapper;

		// Add null to trigger receiving the optional thread local
		this.addOptionalThreadLocalReceiver(null);
	}

	/**
	 * Adds an {@link OptionalThreadLocalReceiver}.
	 * 
	 * @param optionalThreadLocalReceiver {@link OptionalThreadLocalReceiver}.
	 */
	public void addOptionalThreadLocalReceiver(OptionalThreadLocalReceiver optionalThreadLocalReceiver) {

		// Add the optional thread local receiver
		if (optionalThreadLocalReceiver != null) {
			this.receivers.add(optionalThreadLocalReceiver);
		}

		// Ensure have at least one receiver
		if (this.receivers.size() == 0) {
			return; // nothing yet to load
		}

		// Obtain the optional thread local
		if (this.optionalThreadLocal == null) {

			// Not obtain so determine if available
			if (this.dependencyMapper == null) {
				return; // not yet available
			}

			// Available, so obtain
			this.optionalThreadLocal = this.dependencyMapper.getOptionalThreadLocal();
		}

		// Load the optional thread local receivers
		for (OptionalThreadLocalReceiver receiver : this.receivers) {
			receiver.setOptionalThreadLocal(optionalThreadLocal);
		}

		// All received (so clear to avoid adding again)
		this.receivers.clear();
	}

}