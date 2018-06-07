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
package net.officefloor.building.process;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Factory for the creation of {@link OutputStream} instances for the
 * {@link ManagedProcess}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessOutputStreamFactory {

	/**
	 * <p>
	 * Creates the {@link OutputStream} for <code>stdout</code> of the
	 * {@link ManagedProcess}.
	 * <p>
	 * The {@link OutputStream#close()} is invoked when the
	 * {@link ManagedProcess} completes.
	 * 
	 * @param processNamespace
	 *            Name space for the {@link ManagedProcess}.
	 * @param command
	 *            Command to start the {@link ManagedProcess}.
	 * @return {@link OutputStream}.
	 * @throws IOException
	 *             If fails to create the {@link OutputStream}.
	 */
	OutputStream createStandardProcessOutputStream(String processNamespace,
			String[] command) throws IOException;

	/**
	 * <p>
	 * Creates the {@link OutputStream} for <code>stderr</code> of the
	 * {@link ManagedProcess}.
	 * <p>
	 * The {@link OutputStream#close()} is invoked when the
	 * {@link ManagedProcess} completes.
	 * 
	 * @param processNamespace
	 *            Name space for the {@link ManagedProcess}.
	 * @return {@link OutputStream}.
	 * @throws IOException
	 *             If fails to create the {@link OutputStream}.
	 */
	OutputStream createErrorProcessOutputStream(String processNamespace)
			throws IOException;

}